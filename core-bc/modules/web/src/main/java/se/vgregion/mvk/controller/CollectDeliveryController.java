package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.service.concurrent.BackgroundExecutor;
import se._1177.lmn.service.util.Util;
import se.vgregion.mvk.controller.model.Cart;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CollectDeliveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectDeliveryController.class);

    @Autowired
    private LmnService lmnService;

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private DeliveryController deliveryController;

    @Autowired
    private Cart cart;

    @Autowired
    private BackgroundExecutor backgroundExecutor;

    private String zip;
    private Map<ServicePointProviderEnum, String> deliveryPointIdsMap = new HashMap<>();
    private DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod; // These gets stored in session memory
    private String email;
    private String smsNumber;
    private Map<ServicePointProviderEnum, List<DeliveryPointType>> deliveryPointsPerProvider = new HashMap<>();
    private Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>>
            possibleCollectCombinationsFittingAllWithNotificationMethods;
    private Map<ServicePointProviderEnum, String> chosenDeliveryNotificationMethod;
    private String phoneNumber;

    public void updateDeliverySelectItems(AjaxBehaviorEvent ajaxBehaviorEvent) {
        // Just reset deliveryPoints, making them load again when they are requested.
        deliveryPointsPerProvider = null;
    }

    @PostConstruct
    public void init() {
        // Default zip is from user profile. It may be overridden if user chooses so.
        UserProfileType userProfile = userProfileController.getUserProfile();

        zip = userProfile.getZip();

        if (userProfile.isHasSmsNotification() != null && userProfile.isHasSmsNotification()) {
            preferredDeliveryNotificationMethod = DeliveryNotificationMethodEnum.SMS;
        } else if (userProfile.isHasMailNotification() != null && userProfile.isHasMailNotification()) {
            preferredDeliveryNotificationMethod = DeliveryNotificationMethodEnum.E_POST;
        } else {
            preferredDeliveryNotificationMethod = DeliveryNotificationMethodEnum.BREV;
        }
    }

    /**
     * By iterating through all {@link PrescriptionItemType}s in the {@link Cart}, collecting all
     * {@link ServicePointProviderEnum}s and the {@link DeliveryNotificationMethodEnum}s which are present in the
     * respective {@link ServicePointProviderEnum} for all {@link PrescriptionItemType}s. If a
     * {@link DeliveryNotificationMethodEnum} isn't available for a {@link ServicePointProviderEnum} for all
     * {@link PrescriptionItemType}s it is not included in the result.
     *
     * @return the collected map with {@link ServicePointProviderEnum}s mapped to a list of strings of the names of the
     * {@link DeliveryNotificationMethodEnum}s
     */
    public Map<ServicePointProviderEnum, List<String>> getDeliveryNotificationMethodsPerProvider() {

        Map<ServicePointProviderEnum, List<String>> result = new TreeMap<>();

        if (getPossibleCollectCombinationsFittingAllWithNotificationMethods().size() > 0) {

            // Populate result map.
            possibleCollectCombinationsFittingAllWithNotificationMethods.entrySet()
                    .forEach(e -> result.put(e.getKey(), e.getValue()
                            .stream()
                            .map(Enum::name)
                            .collect(Collectors.toList())));

            result.keySet().retainAll(getRelevantServicePointProviders().keySet());
        } else {

            List<PrescriptionItemType> collectPrescriptionItems = getCollectPrescriptionItems();


            for (PrescriptionItemType item : collectPrescriptionItems) {
                ServicePointProviderEnum servicePointProviderForItem = getServicePointProviderForItem(item);

                List<List<String>> listOfListsWithNotificationMethodNames = item.getDeliveryAlternative()
                        .stream()
                        .filter(alternative -> alternative.getServicePointProvider()
                                .equals(servicePointProviderForItem))
                        .map(alternative -> alternative.getDeliveryNotificationMethod().stream().map(Enum::name)
                                .collect(Collectors.toList()))
                        .collect(Collectors.toList());

                // We will only have more than one iteration here if an item has more than one delivery alternative
                // with the same provider. Very unlikely but we support it.
                for (List<String> listWithNotificationMethodName : listOfListsWithNotificationMethodNames) {

                    if (!result.containsKey(servicePointProviderForItem)) {
                        result.put(servicePointProviderForItem, listWithNotificationMethodName);
                    } else {
                        result.get(servicePointProviderForItem).retainAll(listWithNotificationMethodName);
                    }

                }
            }
        }

        result.keySet().retainAll(getRelevantServicePointProviders().keySet());

        return result;
    }

    private List<PrescriptionItemType> getCollectPrescriptionItems() {

        return cart.getItemsInCart()
                .stream()
                .filter(item -> deliveryController.getDeliveryMethodForEachItem().get(item)
                        .equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.name()))
                .collect(Collectors.toList());
    }

    public Map<ServicePointProviderEnum, List<SelectItemGroup>> getDeliverySelectItems() {

        Map<ServicePointProviderEnum, List<SelectItemGroup>> selectOneMenuLists = new HashMap<>();

        if (deliveryPointsPerProvider == null) {
            loadDeliveryPointsForAllSuppliers(zip);
        }

        Map<ServicePointProviderEnum, List<PrescriptionItemType>> servicePointProvidersForItems =
                getRelevantServicePointProviders();

        for (ServicePointProviderEnum servicePointProviderForItem : servicePointProvidersForItems.keySet()) {
            List<SelectItemGroup> singleSelectMenuItems = getSingleSelectMenuItems(servicePointProviderForItem);

            selectOneMenuLists.put(servicePointProviderForItem, singleSelectMenuItems);
        }

        return selectOneMenuLists;
    }

    public Map<ServicePointProviderEnum, List<PrescriptionItemType>> getRelevantServicePointProviders() {
        Map<ServicePointProviderEnum, List<PrescriptionItemType>> servicePointProvidersForItems = new TreeMap<>();

        if (this.possibleCollectCombinationsFittingAllWithNotificationMethods == null) {
            initPossibleCollectCombinationsFittingAllWithNotificationMethods();
        }

        if (this.possibleCollectCombinationsFittingAllWithNotificationMethods.size() > 0) {
            // We have at least one which may satisfy all prescription items. Take the first (probably there will never
            // be more than one in the collection)...

            ServicePointProviderEnum provider = this.possibleCollectCombinationsFittingAllWithNotificationMethods
                    .keySet().iterator().next();

            servicePointProvidersForItems.clear();
            servicePointProvidersForItems.put(provider, cart.getItemsInCart()
                    .stream()
                    .filter(item -> deliveryController.getDeliveryMethodForEachItem().get(item)
                            .equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.name())).collect(Collectors.toList()));
        } else {
            // We don't have any single provider satisfying all items. The user needs to choose service point for the
            // provider of each item.
            for (PrescriptionItemType item : cart.getItemsInCart()) {

                if (!deliveryController.getDeliveryMethodForEachItem().get(item)
                        .equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.name())) {

                    continue;
                }

                ServicePointProviderEnum servicePointProviderForItem = getServicePointProviderForItem(item);

                if (servicePointProviderForItem == null) {
                    // If this happens the item cannot be collected so we skip this.
                    continue;
                }

                if (servicePointProvidersForItems.containsKey(servicePointProviderForItem)) {
                    servicePointProvidersForItems.get(servicePointProviderForItem).add(item);
                } else {
                    List<PrescriptionItemType> list = new ArrayList<>();
                    list.add(item);
                    servicePointProvidersForItems.put(servicePointProviderForItem, list);
                }
            }
        }

        return servicePointProvidersForItems;
    }

    public ServicePointProviderEnum getServicePointProviderForItem(PrescriptionItemType item) {
        ServicePointProviderEnum servicePointProviderForItem = null;
        Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> commonDenominator =
                getPossibleCollectCombinationsFittingAllWithNotificationMethods();

        if (commonDenominator.size() > 0) {
            // We take a service provider which is available for all items.
            servicePointProviderForItem = commonDenominator.keySet().iterator().next();
        } else {

            for (DeliveryAlternativeType deliveryAlternative : item.getDeliveryAlternative()) {
                if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {

                    // If no have no common denominator we just take one.
                    servicePointProviderForItem = deliveryAlternative.getServicePointProvider();

                    break;
                }
            }
        }

        return servicePointProviderForItem;
    }

    private List<SelectItemGroup> getSingleSelectMenuItems(ServicePointProviderEnum provider) {
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

    public Map<ServicePointProviderEnum, String> getChosenDeliveryNotificationMethod() {

        if (chosenDeliveryNotificationMethod == null) {
            initChosenDeliveryNotificationMethod();
        }

        // If there are remaining entries from when the user had chosen more items to order.
        chosenDeliveryNotificationMethod.keySet().retainAll(getRelevantServicePointProviders().keySet());

        return chosenDeliveryNotificationMethod;
    }

    void initChosenDeliveryNotificationMethod() {
        chosenDeliveryNotificationMethod = new HashMap<>();

        Map<ServicePointProviderEnum, List<String>> deliveryNotificationMethodsPerProvider =
                getDeliveryNotificationMethodsPerProvider();

        for (Map.Entry<ServicePointProviderEnum, List<String>> entry :
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

    public Map<ServicePointProviderEnum, String> getDeliveryPointIdsMap() {
        deliveryPointIdsMap.keySet().retainAll(getRelevantServicePointProviders().keySet());

        return deliveryPointIdsMap;
    }

    public String toVerifyDelivery() {
        boolean success = validateNotificationInput();

        if (!success) {
            return "collectDelivery";
        }

        return "verifyDelivery" + ACTION_SUFFIX;
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

    public void initPossibleCollectCombinationsFittingAllWithNotificationMethods() {

        if (possibleCollectCombinationsFittingAllWithNotificationMethods == null) {
            Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> result = new TreeMap<>();

            List<ServicePointProviderEnum> remainingAvailableProvidersCommonForAllWithCollectDelivery = new ArrayList<>(
                    Arrays.asList(ServicePointProviderEnum.values()));

            for (PrescriptionItemType item : cart.getItemsInCart()) {

                List<DeliveryAlternativeType> deliveryAlternatives = deliveryController
                        .getPossibleDeliveryAlternatives(item);

                List<ServicePointProviderEnum> providersForItem = new ArrayList<>();

                for (DeliveryAlternativeType deliveryAlternative : deliveryAlternatives) {

                    if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)) {
                        continue; // We're only interested in collect items.
                    }

                    ServicePointProviderEnum provider = deliveryAlternative.getServicePointProvider();

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
            Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> possibleDeliveryNotificationMethods) {

        this.possibleCollectCombinationsFittingAllWithNotificationMethods = possibleDeliveryNotificationMethods;
    }

    public void loadDeliveryPointsForAllSuppliersInBackground(final String zip) {
        backgroundExecutor.submit(() -> {
            loadDeliveryPointsForAllSuppliers(zip);
        });
    }

    private void loadDeliveryPointsForAllSuppliers(String zip) {

        if (deliveryPointsPerProvider == null) {
            deliveryPointsPerProvider = new HashMap<>();
        }

        ServicePointProviderEnum[] allProviders = ServicePointProviderEnum.values();

        for (ServicePointProviderEnum provider : allProviders) {

            if (provider.equals(ServicePointProviderEnum.INGEN)) {
                continue;
            }

            GetMedicalSupplyDeliveryPointsResponseType medicalSupplyDeliveryPoints;
            try {
                medicalSupplyDeliveryPoints = lmnService.getMedicalSupplyDeliveryPoints(provider, zip);
                deliveryPointsPerProvider.put(provider, medicalSupplyDeliveryPoints.getDeliveryPoint());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public DeliveryPointType getDeliveryPointById(String deliveryPointId) {
        return lmnService.getDeliveryPointById(deliveryPointId);
    }

    public boolean notificationMethodUsedForAnyItem(DeliveryNotificationMethodEnum notificationMethod) {
        return getChosenDeliveryNotificationMethod().values().contains(notificationMethod.name());
    }

    public String providersWithNotificationMethod(String notificationMethod) {
        Map<ServicePointProviderEnum, String> chosenDeliveryNotificationMethod =
                getChosenDeliveryNotificationMethod();

        List<String> providers = new ArrayList<>();

        for (Map.Entry<ServicePointProviderEnum, String> entry :
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

    public Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>>
    getPossibleCollectCombinationsFittingAllWithNotificationMethods() {

        if (possibleCollectCombinationsFittingAllWithNotificationMethods == null) {
            initPossibleCollectCombinationsFittingAllWithNotificationMethods();
        }

        return possibleCollectCombinationsFittingAllWithNotificationMethods;
    }

    public void resetChoices() {
        chosenDeliveryNotificationMethod = null;
        possibleCollectCombinationsFittingAllWithNotificationMethods = null;

        UserProfileType userProfile = userProfileController.getUserProfile();
        zip = userProfile.getZip();
    }

    public boolean validateNotificationInput() {

        final int[] count = {0};

        final boolean[] validationSuccess = {true};

        getDeliveryNotificationMethodsPerProvider().entrySet().forEach(entry -> {
            String chosenDeliveryMethod = getChosenDeliveryNotificationMethod().get(entry.getKey());

            if (DeliveryNotificationMethodEnum.E_POST.name().equals(chosenDeliveryMethod)) {
                String email = getEmail();

                if (email == null || "".equals(email)) {
                    addMessage("Epost för avisering saknas", "emailInput", count[0]);
                    validationSuccess[0] = false;
                } else if (!Util.isValidEmailAddress(email)) {
                    addMessage("Epost för avisering är ogiltig.", "emailInput", count[0]);
                    validationSuccess[0] = false;
                }
            } else if (DeliveryNotificationMethodEnum.BREV.name().equals(chosenDeliveryMethod)) {
                // Do nothing
            } else if (DeliveryNotificationMethodEnum.SMS.name().equals(chosenDeliveryMethod)) {
                String smsNumber = getSmsNumber();

                if (smsNumber == null || "".equals(smsNumber)) {
                    addMessage("SMS för avisering saknas", "smsInput", count[0]);
                    validationSuccess[0] = false;
                } else if (smsNumber.length() < 10) {
                    addMessage("SMS för avisering är ogiltig.", "smsInput", count[0]);
                    validationSuccess[0] = false;
                }
            } else if (DeliveryNotificationMethodEnum.TELEFON.name().equals(chosenDeliveryMethod)) {
                String phoneNumber = getPhoneNumber();

                if (phoneNumber == null || "".equals(phoneNumber)) {
                    addMessage("Telefon för avisering saknas", "phoneInput", count[0]);
                    validationSuccess[0] = false;
                } else if (phoneNumber.length() < 8) {
                    addMessage("Telefon för avisering är ogiltig.", "phoneInput", count[0]);
                    validationSuccess[0] = false;
                }
            } else {
                throw new IllegalStateException("No match for chosen notification method found.");
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

    private void addMessage(String summary, String componentId, int count) {
        FacesMessage msg = new FacesMessage(summary);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);

        FacesContext fc = FacesContext.getCurrentInstance();

        fc.addMessage("collectDeliveryForm:notificationMethodRepeat:" + count + ":" + componentId, msg);

        if (!fc.isReleased()) {
            fc.renderResponse();
        }
    }

    public boolean isSuccessfulSelectItems() {
        Map<ServicePointProviderEnum, List<SelectItemGroup>> deliverySelectItems = getDeliverySelectItems();

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
}
