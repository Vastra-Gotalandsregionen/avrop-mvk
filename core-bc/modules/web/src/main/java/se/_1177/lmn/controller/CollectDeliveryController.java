package se._1177.lmn.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._2.AddressType;
import riv.crm.selfservice.medicalsupply._2.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._2.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._2.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._2.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._2.OrderRowType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._2.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._2.GetMedicalSupplyDeliveryPointsResponseType;
import se._1177.lmn.controller.model.AddressModel;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.model.ServicePointProvider;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.service.ThreadLocalStore;
import se._1177.lmn.service.concurrent.BackgroundExecutor;
import se._1177.lmn.service.mock.MockUtil;
import se._1177.lmn.service.util.Util;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.*;
import java.util.stream.Collectors;

import static riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum.HEMLEVERANS;
import static riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum.UTLÄMNINGSSTÄLLE;
import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * Controller class which handles the model for the view where collect delivery is chosen. The complex part comes when
 * no single service point provider fits all {@link PrescriptionItemType}s. Then
 *
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CollectDeliveryController {

    public static final String VIEW_NAME = "Utlämningsställe";

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectDeliveryController.class);

    @Autowired
    private LmnService lmnService;

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private DeliveryController deliveryController;

    @Autowired
    private UtilController utilController;

    @Autowired
    private Cart cart;

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

    @Autowired
    private BackgroundExecutor backgroundExecutor;

    @Autowired
    private NavigationController navigationController;

    private AddressModel addressModel;

    private String zip;
    private Map<ServicePointProvider, String> deliveryPointIdsMap = new HashMap<>();
    private DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod; // These gets stored in session memory
    private String email;
    private String smsNumber;
    private Map<ServicePointProvider, List<DeliveryPointType>> deliveryPointsPerProvider = new HashMap<>();
    private Map<ServicePointProvider, Set<DeliveryNotificationMethodEnum>>
            possibleCollectCombinationsFittingAllWithNotificationMethods;
    private Map<ServicePointProvider, String> chosenDeliveryNotificationMethod;
    private String phoneNumber;
    private String contactPerson;

    /**
     * Called from view in order to update the selects with delivery points for each {@link ServicePointProvider}.
     *
     * @param ajaxBehaviorEvent
     */
    public void updateDeliverySelectItems(AjaxBehaviorEvent ajaxBehaviorEvent) {
        // Just reset deliveryPoints, making them load again when they are requested.
        deliveryPointsPerProvider = null;

        Set<ServicePointProvider> allRelevantProviders = new HashSet<>();

        // Find service point providers for all order rows with UTLÄMNINGSSTÄLLE as delivery method for all delivery
        // alternatives where isAllowChioceOfDeliveryPoints is true.
        cart.getOrderRows().stream()
                .filter(or -> or.getDeliveryChoice().getDeliveryMethod().equals(UTLÄMNINGSSTÄLLE))
                .map(or -> prescriptionItemInfo.getPrescriptionItem(or).getDeliveryAlternative())
                .flatMap(Collection::stream)
                .filter(DeliveryAlternativeType::isAllowChioceOfDeliveryPoints)
                .filter(da -> !ServicePointProvider.from(da.getServicePointProvider()).equals(ServicePointProvider.INGEN))
                .forEach(da -> allRelevantProviders.add(ServicePointProvider.from(da.getServicePointProvider())));

        loadDeliveryPointsForRelevantSuppliers(zip, allRelevantProviders);
    }

    /**
     * Initializes the class by fetching the user profile to populate the model with user properties such as zip,
     * preferred delivery notification method, sms telephone number and email.
     */
    @PostConstruct
    public void init() {
        addressModel = new AddressModel(userProfileController);
        addressModel.init();

        // Default zip is from user profile. It may be overridden if user chooses so.
        UserProfileType userProfile = userProfileController.getUserProfile();

        if (userProfile != null) {
            zip = userProfile.getZip();

            if (userProfile.isHasSmsNotification() != null && userProfile.isHasSmsNotification()) {
                preferredDeliveryNotificationMethod = DeliveryNotificationMethodEnum.SMS;
            } else if (userProfile.isHasMailNotification() != null && userProfile.isHasMailNotification()) {
                preferredDeliveryNotificationMethod = DeliveryNotificationMethodEnum.E_POST;
            } else {
                preferredDeliveryNotificationMethod = DeliveryNotificationMethodEnum.BREV;
            }

            smsNumber = userProfile.getMobilePhoneNumber();
            email = userProfile.getEmail();

        } else {
            preferredDeliveryNotificationMethod = DeliveryNotificationMethodEnum.BREV;
        }
    }

    /**
     * This methods iterates through all {@link PrescriptionItemType}s with collect delivery in the {@link Cart},
     * getting the {@link ServicePointProvider} (assuming there's only one by taking the first) and the
     * {@link DeliveryNotificationMethodEnum}s which are present for the
     * {@link ServicePointProvider} for all {@link PrescriptionItemType}s. If a
     * {@link DeliveryNotificationMethodEnum} isn't available for a {@link ServicePointProvider} for all
     * {@link PrescriptionItemType}s it is not included in the result.
     *
     * @return the collected map with {@link ServicePointProvider}s mapped to a list of strings of the names of the
     * {@link DeliveryNotificationMethodEnum}s
     */
    public Map<ServicePointProvider, List<String>> getDeliveryNotificationMethodsPerProvider() {

        Map<ServicePointProvider, List<String>> result = new TreeMap<>();

        List<PrescriptionItemType> collectPrescriptionItems = getCollectPrescriptionItems();

        for (PrescriptionItemType item : collectPrescriptionItems) {
            ServicePointProvider servicePointProviderForItem = getServicePointProviderForItem(item);

            // Make a list where the number of first level entries will be equal to the number of delivery
            // alternatives where the service point provider equals servicePointProviderForItem. This will almost
            // certainly be a one-entry-list at the top level, except if there are multiple delivery alternatives with
            // the same ServicePointProvider.
            List<List<String>> listOfListsWithNotificationMethodNames = item.getDeliveryAlternative()
                    .stream()
                    .filter(alternative -> ServicePointProvider.from(alternative.getServicePointProvider())
                            .equals(servicePointProviderForItem))
                    .map(alternative -> alternative.getDeliveryNotificationMethod().stream().map(Enum::name)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

            // We will only have more than one iteration here if an item has more than one delivery alternative
            // with the same provider. Very unlikely but we support it.
            for (List<String> listWithNotificationMethodName : listOfListsWithNotificationMethodNames) {

                if (listWithNotificationMethodName.size() == 0) {
                    continue;
                }

                if (!result.containsKey(servicePointProviderForItem)) {
                    result.put(servicePointProviderForItem, listWithNotificationMethodName);
                } else {
                    result.get(servicePointProviderForItem).retainAll(listWithNotificationMethodName);
                }

            }
        }

        return result;
    }

    // All items which are chosen to be delivered by collect delivery. Note that the items may be chosen for delivery
    // methods on an individual basis if no single delivery method suits all items.
    private List<PrescriptionItemType> getCollectPrescriptionItems() {

        List<OrderRowType> orderRows = cart.getOrderRows();

        List<PrescriptionItemType> prescriptionItems = prescriptionItemInfo.getPrescriptionItems(orderRows);

        return prescriptionItems
                .stream()
                .filter(item -> deliveryController.getDeliveryMethodForEachItem().get(item)
                        .equals(UTLÄMNINGSSTÄLLE.name()))
                .collect(Collectors.toList());
    }

    /**
     * Method called to list one or multiple delivery point selects - one for each provider.
     *
     * @return
     */
    public Map<ServicePointProvider, List<SelectItemGroup>> getDeliverySelectItems() {

        Map<ServicePointProvider, List<SelectItemGroup>> selectOneMenuLists = new HashMap<>();

        Map<ServicePointProvider, List<PrescriptionItemType>> servicePointProvidersForItems =
                getServicePointProvidersForDeliveryPointChoice();

        for (ServicePointProvider servicePointProviderForItem : servicePointProvidersForItems.keySet()) {
            List<SelectItemGroup> singleSelectMenuItems = getSingleSelectMenuItems(servicePointProviderForItem);

            selectOneMenuLists.put(servicePointProviderForItem, singleSelectMenuItems);
        }

        return selectOneMenuLists;
    }

    public boolean isAnyItemWhereAllowChoiceOfDeliveryPointIsFalse() {
        return isAnyItemWhereAllowCollectIs(false);
    }

    public boolean isAnyItemWhereAllowChoiceOfDeliveryPointIsTrue() {
        return isAnyItemWhereAllowCollectIs(true);
    }

    boolean isAnyItemWhereAllowCollectIs(boolean findWherePropertyIs) {
        return cart.getOrderRows().stream()
                .filter(row -> row.getDeliveryChoice().getDeliveryMethod().equals(UTLÄMNINGSSTÄLLE))
                .map(row -> prescriptionItemInfo.getPrescriptionItem(row))
                .distinct()
                .flatMap(item -> deliveryController.getPossibleDeliveryAlternatives(item).stream())
                .filter(alternative -> alternative.getDeliveryMethod().equals(UTLÄMNINGSSTÄLLE))
                .anyMatch(alternative -> alternative.isAllowChioceOfDeliveryPoints() == findWherePropertyIs);
    }

    /**
     * This method makes a map of all {@link ServicePointProvider}s mapped to the {@link PrescriptionItemType}s
     * which have that {@link ServicePointProvider}. The map enables lookup of which {@link PrescriptionItemType}s
     * have a specific {@link ServicePointProvider}. The method assumes only one {@link DeliveryAlternativeType}
     * with {@link DeliveryMethodEnum} UTLÄMNINGSSTÄLLE. Otherwise an exception will be thrown.
     *
     * @return
     */
    public Map<ServicePointProvider, List<PrescriptionItemType>> getServicePointProvidersForDeliveryPointChoice() {
        Map<ServicePointProvider, List<PrescriptionItemType>> servicePointProvidersForItems = new TreeMap<>();

        cart.getOrderRows()
                .stream()
                .filter(row -> row.getDeliveryChoice().getDeliveryMethod().equals(UTLÄMNINGSSTÄLLE))
                .map(row -> prescriptionItemInfo.getPrescriptionItem(row))
                .distinct() // Since multiple order rows may have the same prescription item if they are sub-articles.
                .forEach(item -> {

                    List<DeliveryAlternativeType> deliveryAlternatives = item.getDeliveryAlternative()
                            .stream()
                            .filter(alternative -> alternative.getDeliveryMethod().equals(UTLÄMNINGSSTÄLLE))
                            .collect(Collectors.toList());

                    if (deliveryAlternatives.size() > 1) {
                        String message = "Only one delivery alternative with UTLÄMNINGSSTÄLLE is expected.";
                        throw new IllegalStateException(message);
                    }

                    if (deliveryAlternatives.get(0).isAllowChioceOfDeliveryPoints()) {

                        ServicePointProvider servicePointProviderForItem = getServicePointProviderForItem(item);

                        if (servicePointProvidersForItems.containsKey(servicePointProviderForItem)) {
                            servicePointProvidersForItems.get(servicePointProviderForItem).add(item);
                        } else {
                            List<PrescriptionItemType> list = new ArrayList<>();
                            list.add(item);
                            servicePointProvidersForItems.put(servicePointProviderForItem, list);
                        }
                    }
                });

        return servicePointProvidersForItems;
    }

    /**
     * Finds the {@link ServicePointProvider} for the first
     * {@link DeliveryAlternativeType} with UTLÄMNINGSSTÄLLE as {@link DeliveryMethodEnum}.
     *
     * @param item the {@link PrescriptionItemType} to determine {@link ServicePointProvider} for
     * @return The determined {@link ServicePointProvider}.
     */
    public ServicePointProvider getServicePointProviderForItem(PrescriptionItemType item) {
        return item.getDeliveryAlternative().stream()
                .filter(da -> da.getDeliveryMethod().equals(UTLÄMNINGSSTÄLLE))
                .findFirst() // We only allow one delivery method with UTLÄMNINGSSTÄLLE so we take the first one.
                .map(dat -> ServicePointProvider.from(dat.getServicePointProvider()))
                .orElseThrow(() -> new IllegalStateException("A prescription item without UTLÄMNINGSSTÄLLE should not" +
                        "treated here."));
    }

    private List<SelectItemGroup> getSingleSelectMenuItems(ServicePointProvider provider) {
        SelectItemGroup group1 = new SelectItemGroup("Närmaste ombud");
        SelectItemGroup group2 = new SelectItemGroup("Övriga ombud till ditt postnummer");

        List<SelectItem> toGroup1 = new ArrayList<>();
        List<SelectItem> toGroup2 = new ArrayList<>();

        int count = 0;
        if (deliveryPointsPerProvider.get(provider) != null) {

            for (DeliveryPointType deliveryPoint : deliveryPointsPerProvider.get(provider)) {

                String label = deliveryPoint.getDeliveryPointAddress()
                        + ", " + deliveryPoint.getDeliveryPointName()
                        + ", " + deliveryPoint.getDeliveryPointCity();

                SelectItem selectItem = new SelectItem(deliveryPoint.getDeliveryPointId(), label);

                // First one is closest.
                if (count++ == 0) {
                    toGroup1.add(selectItem);
                } else {
                    toGroup2.add(selectItem);
                }
            }
        }

        group1.setSelectItems(toGroup1.toArray(new SelectItem[0]));
        group2.setSelectItems(toGroup2.toArray(new SelectItem[0]));

        List<SelectItemGroup> toReturn = new ArrayList<>();

        toReturn.add(group1);
        toReturn.add(group2);

        return toReturn;
    }

    public Map<ServicePointProvider, String> getChosenDeliveryNotificationMethod() {

        if (chosenDeliveryNotificationMethod == null) {
            initChosenDeliveryNotificationMethod();
        }

        // If there are remaining entries from when the user had chosen more items to order.
//        chosenDeliveryNotificationMethod.keySet().retainAll(getServicePointProvidersForDeliveryPointChoice().keySet());

        return chosenDeliveryNotificationMethod;
    }

    void initChosenDeliveryNotificationMethod() {
        chosenDeliveryNotificationMethod = new HashMap<>();

        Map<ServicePointProvider, List<String>> deliveryNotificationMethodsPerProvider =
                getDeliveryNotificationMethodsPerProvider();

        for (Map.Entry<ServicePointProvider, List<String>> entry :
                deliveryNotificationMethodsPerProvider.entrySet()) {

            if (entry.getValue().contains(preferredDeliveryNotificationMethod.name())) {
                chosenDeliveryNotificationMethod.put(entry.getKey(), preferredDeliveryNotificationMethod.name());
            } else {
                // In case the preferred one isn't available.
                String defaultNotificationMethod = entry.getValue().size() > 0 ? entry.getValue().get(0) : null;
                chosenDeliveryNotificationMethod.put(entry.getKey(), defaultNotificationMethod);
            }
        }
    }

    public Map<ServicePointProvider, String> getDeliveryPointIdsMap() {
        deliveryPointIdsMap.keySet().retainAll(getServicePointProvidersForDeliveryPointChoice().keySet());

        return deliveryPointIdsMap;
    }

    public String toVerifyDelivery() {
        final boolean[] success = {validateCollectDeliveryPoint()};
        success[0] = success[0] && validateNotificationInput();

        if (!success[0]) {
            return "collectDelivery";
        }

        // Add info from this step
        List<OrderRowType> orderRowsWithCollectDelivery = cart.getOrderRows().stream()
                .filter(this::collectDeliveryChosen).collect(Collectors.toList());

        orderRowsWithCollectDelivery.forEach(orderRowType -> {
            DeliveryChoiceType deliveryChoice = orderRowType.getDeliveryChoice();

            DeliveryMethodEnum deliveryMethod = orderRowType.getDeliveryChoice().getDeliveryMethod();

            if (deliveryMethod.equals(UTLÄMNINGSSTÄLLE)) {

                String deliveryMethodId = null;
                Boolean allowChoiceOfDeliveryPoints = null;

                // Take the first deliveryAlternative with matching deliveryMethod and service point provider. This
                // assumes no two deliveryAlternatives share the same deliveryMethod and service point provider. That
                // would lead to arbitrary result.
                PrescriptionItemType prescriptionItem = prescriptionItemInfo.getPrescriptionItem(orderRowType);
                DeliveryAlternativeType deliveryAlternativeForThisOrderRow = getDeliveryAlternativeForOrderRow(orderRowType);

                if (deliveryAlternativeForThisOrderRow != null) {
                    deliveryMethodId = deliveryAlternativeForThisOrderRow.getDeliveryMethodId();
                        allowChoiceOfDeliveryPoints = deliveryAlternativeForThisOrderRow.isAllowChioceOfDeliveryPoints();
                }

                if (deliveryMethodId == null) {
                    String msg = "Kunde inte genomföra beställning. Försök senare.";
                    utilController.addErrorMessageWithCustomerServiceInfo(msg);

                    success[0] = false;
                }

                deliveryChoice.setDeliveryMethodId(deliveryMethodId);

                ServicePointProvider provider = getServicePointProviderForItem(prescriptionItem);

                if (allowChoiceOfDeliveryPoints) {
                    String deliveryPointId = getDeliveryPointIdsMap().get(provider);
                    deliveryChoice.setDeliveryPoint(lmnService.getDeliveryPointById(deliveryPointId));
                } else {
                    AddressType value = new AddressType();
                    value.setCity(addressModel.getCity());
                    value.setPostalCode(addressModel.getZip());
                    value.setReceiver(addressModel.getFullName());
                    value.setStreet(addressModel.getAddress());
                    value.setCareOfAddress(addressModel.getCoAddress());
                    value.setPhone(addressModel.getPhoneNumber());

                    deliveryChoice.setHomeDeliveryAddress(value);
                }

                String notificationMethodString = getChosenDeliveryNotificationMethod().get(provider);

                if (notificationMethodString != null) {
                    DeliveryNotificationMethodEnum notificationMethod = DeliveryNotificationMethodEnum
                            .valueOf(notificationMethodString);

                    // Assert the notification method is available for the prescription item.
                    boolean found = false;
                    for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {
                        if (deliveryAlternative.getDeliveryNotificationMethod().contains(notificationMethod)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        throw new IllegalStateException("A notification method not available for the given prescription " +
                                "item has been chosen. That shouldn't be possible so it's a bug.");
                    }

                    deliveryChoice.setDeliveryNotificationMethod(Util.wrapInJAXBElement(notificationMethod));

                    String notificationReceiver;

                    switch (notificationMethod) {
                        case BREV:
                            notificationReceiver = null;

                            /* This is only relevant for the inbox message. Home address is not relevant for collect
                            delivery but it does no harm if it's included in the web service message. */
                            UserProfileType userProfile = userProfileController.getUserProfile();
                            if (userProfile != null) {

                                AddressType address = new AddressType();
                                address.setCity(userProfile.getCity());
                                address.setPostalCode(userProfile.getZip());
                                address.setReceiver(userProfile.getFirstName() + " " + userProfile.getLastName());
                                address.setStreet(userProfile.getStreetAddress());

                                deliveryChoice.setHomeDeliveryAddress(address);
                            }
                            break;
                        case E_POST:
                            notificationReceiver = getEmail();
                            break;
                        case SMS:
                            notificationReceiver = getSmsNumber();
                            break;
                        case TELEFON:
                            notificationReceiver = getPhoneNumber();
                            break;
                        default:
                            throw new RuntimeException("Unexpected notificationMethod: " + notificationMethod);
                    }

                    deliveryChoice.setDeliveryNotificationReceiver(notificationReceiver);
                }
            }
        });

        if (this.contactPerson != null) {
            // We only set contact person on those order rows where the chosen delivery alternative allows so.
            orderRowsWithCollectDelivery.stream()
                    .filter(orderRowType -> getDeliveryAlternativeForOrderRow(orderRowType).isAllowContactPerson())
                    .forEach(orderRowType -> orderRowType.getDeliveryChoice().setContactPerson(this.contactPerson));
        }

        if (anyItemHasAllowOtherInvoiceAddress()) {
            return navigationController.gotoView("invoiceAddress" + ACTION_SUFFIX, InvoiceAddressController.VIEW_NAME);
        } else {
            return navigationController.gotoView("verifyDelivery" + ACTION_SUFFIX, VerifyDeliveryController.VIEW_NAME);
        }
    }

    DeliveryAlternativeType getDeliveryAlternativeForOrderRow(OrderRowType orderRowType) {
        PrescriptionItemType prescriptionItem = prescriptionItemInfo.getPrescriptionItem(orderRowType);
        DeliveryAlternativeType deliveryAlternativeForThisOrderRow = null;
        for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {

            // If deliveryMethod and servicePointProvider match we assume it is the one.
            if (deliveryAlternative.getDeliveryMethod().equals(orderRowType.getDeliveryChoice().getDeliveryMethod())
                    && getServicePointProviderForItem(prescriptionItem)
                    .equals(ServicePointProvider.from(deliveryAlternative.getServicePointProvider()))) {

                deliveryAlternativeForThisOrderRow = deliveryAlternative;
                break;
            }
        }

        return deliveryAlternativeForThisOrderRow;
    }

    boolean anyItemHasAllowOtherInvoiceAddress() {
        return prescriptionItemInfo.getChosenPrescriptionItemInfoList()
                .stream()
                .anyMatch(item -> BooleanUtils.isTrue(item.isAllowOtherInvoiceAddress()));

    }

    boolean anyItemHasAllowContactPerson() {
        return cart.getOrderRows().stream()
                .filter(this::collectDeliveryChosen)
                .map(orderRowType -> prescriptionItemInfo.getPrescriptionItem(orderRowType))
                .flatMap(prescriptionItemType -> prescriptionItemType.getDeliveryAlternative().stream())
                .anyMatch(deliveryAlternative -> BooleanUtils.isTrue(deliveryAlternative.isAllowContactPerson()));
    }

    private boolean collectDeliveryChosen(OrderRowType orderRowType) {
        return orderRowType.getDeliveryChoice().getDeliveryMethod().equals(UTLÄMNINGSSTÄLLE);
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public DeliveryNotificationMethodEnum getPreferredDeliveryNotificationMethod() {
        return preferredDeliveryNotificationMethod;
    }

    public void setPreferredDeliveryNotificationMethod(
            DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod) {
        this.preferredDeliveryNotificationMethod = preferredDeliveryNotificationMethod;
    }

    public DeliveryNotificationMethodEnum getBrevValue() {
        return DeliveryNotificationMethodEnum.BREV;
    }

    public DeliveryNotificationMethodEnum getEpostValue() {
        return DeliveryNotificationMethodEnum.E_POST;
    }

    public DeliveryNotificationMethodEnum getSmsValue() {
        return DeliveryNotificationMethodEnum.SMS;
    }

    public DeliveryNotificationMethodEnum getTelefonValue() {
        return DeliveryNotificationMethodEnum.TELEFON;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    /**
     * This method aims to find combinations of {@link ServicePointProvider}s with possible
     * {@link DeliveryNotificationMethodEnum}s. The algorithm is to start with all {@link ServicePointProvider}s and
     * "fill" all entries with the {@link DeliveryNotificationMethodEnum}s of the first {@link PrescriptionItemType}
     * iterated. As more {@link PrescriptionItemType}s are iterated both the {@link DeliveryNotificationMethodEnum}s,
     * for each {@link ServicePointProvider}, as well as {@link ServicePointProvider} are removed as they are
     * determined as unavailable for all {@link PrescriptionItemType}s.
     */
    public void initPossibleCollectCombinationsFittingAllWithNotificationMethods() {

        if (possibleCollectCombinationsFittingAllWithNotificationMethods == null) {
            Map<ServicePointProvider, Set<DeliveryNotificationMethodEnum>> result = new TreeMap<>();

            List<ServicePointProvider> remainingAvailableProvidersCommonForAllWithCollectDelivery = new ArrayList<>(
                    deliveryPointsPerProvider.keySet()
            );

            List<OrderRowType> filteredOrderRows = cart.getOrderRows().stream()
                    .filter(orderRowType -> orderRowType.getDeliveryChoice().getDeliveryMethod().equals(
                            UTLÄMNINGSSTÄLLE))
                    .collect(Collectors.toList());

            List<PrescriptionItemType> prescriptionItemsInCart = prescriptionItemInfo
                    .getPrescriptionItems(filteredOrderRows);

            for (PrescriptionItemType item : prescriptionItemsInCart) {

                List<DeliveryAlternativeType> deliveryAlternatives = deliveryController
                        .getPossibleDeliveryAlternatives(item);

                List<ServicePointProvider> providersForItem = new ArrayList<>();

                for (DeliveryAlternativeType deliveryAlternative : deliveryAlternatives) {

                    if (deliveryAlternative.getDeliveryMethod().equals(HEMLEVERANS)) {
                        continue; // We're only interested in collect items.
                    }

                    ServicePointProvider provider = ServicePointProvider.from(
                            deliveryAlternative.getServicePointProvider()
                    );

                    providersForItem.add(provider);

                    List<DeliveryNotificationMethodEnum> deliveryNotificationMethods = deliveryAlternative
                            .getDeliveryNotificationMethod();

                    if (!result.containsKey(provider)) {
                        result.put(provider, new TreeSet<>());

                        result.get(provider).addAll(deliveryNotificationMethods);
                    } else {
                        result.get(provider).retainAll(deliveryNotificationMethods);
                    }
                }

                if (providersForItem.size() > 0) {
                    remainingAvailableProvidersCommonForAllWithCollectDelivery.retainAll(providersForItem);
                }
            }

            result.keySet().retainAll(remainingAvailableProvidersCommonForAllWithCollectDelivery);

            possibleCollectCombinationsFittingAllWithNotificationMethods = result;
        }
    }

    public void setPossibleCollectCombinationsFittingAllCollectItems(
            Map<ServicePointProvider, Set<DeliveryNotificationMethodEnum>> possibleDeliveryNotificationMethods) {

        this.possibleCollectCombinationsFittingAllWithNotificationMethods = possibleDeliveryNotificationMethods;
    }

    public void loadDeliveryPointsForRelevantSuppliersInBackground(final String zip,
                                                                   final Set<ServicePointProvider> allRelevantProvider,
                                                                   final String countyCode) {
        backgroundExecutor.submit(() -> {
            ThreadLocalStore.setCountyCode(countyCode);
            loadDeliveryPointsForRelevantSuppliers(zip, allRelevantProvider);
            ThreadLocalStore.setCountyCode(null);
        });
    }

    private void loadDeliveryPointsForRelevantSuppliers(String zip, Set<ServicePointProvider> providers) {

        if (deliveryPointsPerProvider == null) {
            deliveryPointsPerProvider = new HashMap<>();
        }

        for (ServicePointProvider provider : providers) {

            if (provider.equals(ServicePointProvider.INGEN)) {
                continue;
            }

            GetMedicalSupplyDeliveryPointsResponseType medicalSupplyDeliveryPoints;
            try {
                medicalSupplyDeliveryPoints = lmnService.getMedicalSupplyDeliveryPoints(MockUtil.toCvType(provider), zip);

                if (medicalSupplyDeliveryPoints.getResultCode().equals(ResultCodeEnum.OK)) {
                    deliveryPointsPerProvider.put(provider, medicalSupplyDeliveryPoints.getDeliveryPoint());
                } else {
                    utilController.addErrorMessageWithCustomerServiceInfo("Ett fel mot underliggande system inträffade. Försök senare eller kontakta kundtjänst.");
                }
            } catch (WebServiceException e) {
                LOGGER.error(e.getMessage(), e);

                utilController.addErrorMessageWithCustomerServiceInfo("Ett fel mot underliggande system inträffade. Försök senare eller kontakta kundtjänst.");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                // We don't need to add a message here since we show a message by a condition in the view.
            }
        }
    }

    public DeliveryPointType getDeliveryPointById(String deliveryPointId) {
        return lmnService.getDeliveryPointById(deliveryPointId);
    }

    public boolean notificationMethodUsedForAnyItem(DeliveryNotificationMethodEnum notificationMethod) {
        return getChosenDeliveryNotificationMethod().containsValue(notificationMethod.name());
    }

    public String providersWithNotificationMethod(String notificationMethod) {
        Map<ServicePointProvider, String> chosenDeliveryNotificationMethod =
                getChosenDeliveryNotificationMethod();

        List<String> providers = new ArrayList<>();

        for (Map.Entry<ServicePointProvider, String> entry :
                chosenDeliveryNotificationMethod.entrySet()) {

            if (entry.getValue().equals(notificationMethod)) {
                providers.add(UtilController.toProviderName(entry.getKey()));
            }
        }

        return StringUtils.join(providers, ", ");
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the collection of all {@link ServicePointProvider}s mapped to a {@link Set} of
     * {@link DeliveryNotificationMethodEnum} of which all are available for all chosen {@link PrescriptionItemType}s.
     *
     * @return The combinations of {@link ServicePointProvider}s and the {@link DeliveryNotificationMethodEnum}s
     * they are mapped to are available for all {@link PrescriptionItemType}s
     */
    public Map<ServicePointProvider, Set<DeliveryNotificationMethodEnum>>
    getPossibleCollectCombinationsFittingAllWithNotificationMethods() {

        if (possibleCollectCombinationsFittingAllWithNotificationMethods == null) {
            initPossibleCollectCombinationsFittingAllWithNotificationMethods();
        }

        return possibleCollectCombinationsFittingAllWithNotificationMethods;
    }

    public void resetChoices() {

        // I would like to retain already chosen notification methods but it turns out that it's challenging to keep up
        // with all possible combinations of providers and notifications that may occur.
        chosenDeliveryNotificationMethod = null;
        possibleCollectCombinationsFittingAllWithNotificationMethods = null;

        UserProfileType userProfile = userProfileController.getUserProfile();

        if (userProfile != null) {
            zip = userProfile.getZip();
        }
    }

    public boolean validateCollectDeliveryPoint() {

        boolean success[] = new boolean[]{true};

        deliveryPointIdsMap.entrySet().forEach(entry -> {
            if (entry.getValue() == null) {
                success[0] = false;
                addErrorMessage("Du har inte valt utlämningsställe för " + UtilController.toProviderName(entry.getKey()),
                        "collectDeliveryForm:updateDeliverySelectItemsButton");
            }
        });

        return success[0];
    }

    /**
     * Validate that all delivery notifications that should be set are set.
     *
     * @return <code>true</code> if all are set and <code>false</code> otherwise
     */
    public boolean validateNotificationInput() {

        final int[] count = {0};

        final boolean[] validationSuccess = {true};

        getDeliveryNotificationMethodsPerProvider().forEach((key, value) -> {
            String chosenDeliveryMethod = getChosenDeliveryNotificationMethod().get(key);

            if (DeliveryNotificationMethodEnum.E_POST.name().equals(chosenDeliveryMethod)) {
                String email = getEmail();

                if (email == null || "".equals(email)) {
                    addMessage("Epost för avisering saknas", "emailInput", count[0] + "");
                    validationSuccess[0] = false;
                } else if (!Util.isValidEmailAddress(email)) {
                    addMessage("Epost för avisering är ogiltig.", "emailInput", count[0] + "");
                    validationSuccess[0] = false;
                }
            } else if (DeliveryNotificationMethodEnum.BREV.name().equals(chosenDeliveryMethod)) {
                // Do nothing
            } else if (DeliveryNotificationMethodEnum.SMS.name().equals(chosenDeliveryMethod)) {
                String smsNumber = getSmsNumber();

                if (smsNumber == null || "".equals(smsNumber)) {
                    addMessage("Mobiltelefon för avisering saknas", "smsInput", count[0] + "");
                    validationSuccess[0] = false;
                } else if (smsNumber.length() < 10) {
                    addMessage("Mobiltelefon för avisering är ogiltig.", "smsInput", count[0] + "");
                    validationSuccess[0] = false;
                }
            } else if (DeliveryNotificationMethodEnum.TELEFON.name().equals(chosenDeliveryMethod)) {
                String phoneNumber = getPhoneNumber();

                if (phoneNumber == null || "".equals(phoneNumber)) {
                    addMessage("Telefon för avisering saknas", "phoneInput", count[0] + "");
                    validationSuccess[0] = false;
                } else if (phoneNumber.length() < 8) {
                    addMessage("Telefon för avisering är ogiltig.", "phoneInput", count[0] + "");
                    validationSuccess[0] = false;
                }
            } else {
                addErrorMessage("Avisering är inte korrekt angivet.", "");
                validationSuccess[0] = false;
            }

            count[0]++;
        });

        /*if (validationFailed[0]) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (!fc.isReleased()) {
                fc.renderResponse();
            }
        }*/

        return validationSuccess[0];
    }

    private void addMessage(String summary, String componentId, String count) {
        FacesMessage msg = new FacesMessage(summary);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);

        FacesContext fc = FacesContext.getCurrentInstance();

        fc.addMessage("collectDeliveryForm:notificationMethodRepeat:" + count + ":" + componentId, msg);

        if (!fc.isReleased()) {
            fc.renderResponse();
        }
    }

    private void addErrorMessage(String text, String clientId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // Don't add duplicate error messages
        for (FacesMessage facesMessage : facesContext.getMessageList()) {
            if (facesMessage.getSummary().equals(text)) {
                return;
            }
        }

        facesContext.addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, text, text));
    }

    /**
     * Determines whether the fetch of {@link DeliveryPointType}s for each {@link ServicePointProvider} was
     * successful.
     *
     * @return <code>true</code> if successful, false otherwise
     */
    public boolean isSuccessfulSelectItems() {
        Map<ServicePointProvider, List<SelectItemGroup>> deliverySelectItems = getDeliverySelectItems();

        final boolean[] successfulFetch = {true};

        deliverySelectItems.forEach((servicePointProviderEnum, selectItemGroups) -> {
            for (SelectItemGroup selectItemGroup : selectItemGroups) {
                if (selectItemGroup.getSelectItems().length < 1) {
                    successfulFetch[0] = false;
                    break;
                }
            }
        });

        return successfulFetch[0];
    }

    public Boolean getShowStandardErrorMessage() {

        // We only show standard message when we don't show any other messages.
        if (FacesContext.getCurrentInstance().getMessageList().size() > 0) {
            return false;
        }

        for (Map.Entry<ServicePointProvider, List<SelectItemGroup>> entry : getDeliverySelectItems().entrySet()) {
            if (entry.getValue().get(0).getSelectItems().length < 1) {
                // The 1 would be the closest collect delivery point. Here we don't have one since it must have failed.
                return true;
            }
        }

        return false;
    }

    public AddressModel getAddressModel() {
        return addressModel;
    }

    public void setAddressModel(AddressModel addressModel) {
        this.addressModel = addressModel;
    }

    public boolean getShowContactPerson() {
        String subjectOfCareId = userProfileController.getUserProfile().getSubjectOfCareId();

        return Util.isBetween13And18(subjectOfCareId) && anyItemHasAllowContactPerson();
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getViewName() {
        return VIEW_NAME;
    }
}
