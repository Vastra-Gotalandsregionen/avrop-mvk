package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import se._1177.lmn.service.LmnService;
import se.vgregion.mvk.controller.model.Cart;

import javax.annotation.PostConstruct;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private Cart cart;

    private String zip;
    private Map<ServicePointProviderEnum, String> deliveryPointIdsMap = new HashMap<>();
    private DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod; // These gets stored in session memory
    private String email;
    private String smsNumber;
    private Map<ServicePointProviderEnum, List<DeliveryPointType>> deliveryPointsPerProvider = new HashMap();
    private Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>>
            possibleCollectCombinationsFittingAllWithNotificationMethods = new HashMap<>();
    private Map<ServicePointProviderEnum, String> chosenDeliveryNotificationMethod;

    public void updateDeliverySelectItems(AjaxBehaviorEvent ajaxBehaviorEvent) {
        // Just reset deliveryPoints, making them load again when they are requested.
        deliveryPointsPerProvider = null;
    }

    @PostConstruct
    public void init() {
        // Default zip is from user profile. It may be overridden if user chooses so.
        UserProfileType userProfile = userProfileController.getUserProfile().getUserProfile();

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

        Map<ServicePointProviderEnum, List<String>> result = new HashMap<>();

        for (PrescriptionItemType item : cart.getItemsInCart()) {
            for (DeliveryAlternativeType deliveryAlternative : item.getDeliveryAlternative()) {

                if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)) {
                    continue; // We're only interested in collect items.
                }

                if (!getRelevantServicePointProviders().contains(deliveryAlternative.getServicePointProvider())) {
                    // Neither are we interested in any provider which isn't available for any item or which isn't available to all items when there is any that is.
                    continue;
                }

                ServicePointProviderEnum provider = deliveryAlternative.getServicePointProvider();

                List<DeliveryNotificationMethodEnum> deliveryNotificationMethods = deliveryAlternative
                        .getDeliveryNotificationMethod();

                List<String> deliveryNotificationMethodStrings = deliveryNotificationMethods
                        .stream()
                        .map(e -> e.name())
                        .collect(Collectors.toCollection(ArrayList<String>::new));

                if (!result.containsKey(provider)) {
                    result.put(provider, new ArrayList<>());

                    result.get(provider).addAll(deliveryNotificationMethodStrings);
                } else {
                    result.get(provider).retainAll(deliveryNotificationMethodStrings);
                }
            }

        }

        // Now we have all available notification methods for each provider

        return result;
    }

    public Map<ServicePointProviderEnum, List<SelectItemGroup>> getDeliverySelectItems() {

        Map<ServicePointProviderEnum, List<SelectItemGroup>> selectOneMenuLists = new HashMap<>();

        if (deliveryPointsPerProvider == null) {
            loadDeliveryPointsForAllSuppliers(zip);
        }

        List<ServicePointProviderEnum> servicePointProvidersForItems = getRelevantServicePointProviders();

        for (ServicePointProviderEnum servicePointProviderForItem : servicePointProvidersForItems) {
            List<SelectItemGroup> singleSelectMenuItems = getSingleSelectMenuItems(servicePointProviderForItem);

            selectOneMenuLists.put(servicePointProviderForItem, singleSelectMenuItems);
        }

        return selectOneMenuLists;
    }

    List<ServicePointProviderEnum> getRelevantServicePointProviders() {
        List<ServicePointProviderEnum> servicePointProvidersForItems = new ArrayList<>();
        if (possibleCollectCombinationsFittingAllWithNotificationMethods.size() > 0) {
            // We have at least one which may satisfy all prescription items. Take the first (probably there will never
            // be more than one in the collection)...

            ServicePointProviderEnum provider = possibleCollectCombinationsFittingAllWithNotificationMethods.keySet()
                    .iterator().next();

            servicePointProvidersForItems.clear();
            servicePointProvidersForItems.add(provider);
        } else {
            // We don't have any single provider satisfying all items. The user needs to chosse service point for the
            // provider of each item.
            for (PrescriptionItemType item : cart.getItemsInCart()) {
                ServicePointProviderEnum servicePointProviderForItem = getServicePointProviderForItem(item);

                if (servicePointProviderForItem == null) {
                    // If this happens the item cannot be collected so we skip this.
                    continue;
                }

                servicePointProvidersForItems.add(servicePointProviderForItem);

            }
        }
        return servicePointProvidersForItems;
    }

    public ServicePointProviderEnum getServicePointProviderForItem(PrescriptionItemType item) {
        ServicePointProviderEnum servicePointProviderForItem = null;
        for (DeliveryAlternativeType deliveryAlternative : item.getDeliveryAlternative()) {
            if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {
                servicePointProviderForItem = deliveryAlternative.getServicePointProvider();
                break;
            }
        }
        return servicePointProviderForItem;
    }

    List<SelectItemGroup> getSingleSelectMenuItems(ServicePointProviderEnum provider) {
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
        chosenDeliveryNotificationMethod.keySet().retainAll(getRelevantServicePointProviders());

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
        deliveryPointIdsMap.keySet().retainAll(getRelevantServicePointProviders());

        return deliveryPointIdsMap;
    }

    public String toVerifyDelivery() {
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

    public void setPreferredDeliveryNotificationMethod(DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod) {
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

    public void triggerInit() {
        try {
            Thread.sleep(15000);
            System.out.println("Finished sleeping...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> getPossibleCollectCombinationsFittingAllWithNotificationMethods() {
        return possibleCollectCombinationsFittingAllWithNotificationMethods;
    }

    public void setPossibleCollectCombinationsFittingAllCollectItems(Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> possibleDeliveryNotificationMethods) {
        this.possibleCollectCombinationsFittingAllWithNotificationMethods = possibleDeliveryNotificationMethods;
    }

    public void loadDeliveryPointsForAllSuppliersInBackground(final String zip) {
        getExecutor().submit(() -> {
            loadDeliveryPointsForAllSuppliers(zip);
        });
    }

    void loadDeliveryPointsForAllSuppliers(String zip) {

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

    private ExecutorService executorService; // // TODO: 2016-05-01 Make a global executorService?

    public synchronized ExecutorService getExecutor() {
        if (executorService != null) {
            return executorService;
        }
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();

        threadFactory.setDaemon(true);
        threadFactory.setThreadGroupName("backgroundTasksGroup");
        threadFactory.setThreadNamePrefix("backgroundTask");

        executorService = Executors.newCachedThreadPool(threadFactory);

        return executorService;
    }
}
