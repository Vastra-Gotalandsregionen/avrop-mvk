package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
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

    private String chosenDeliveryPoint;
    private String zip;
    private Map<String, DeliveryPointType> deliveryPointsMap = new HashMap<>();
    private DeliveryNotificationMethodEnum deliveryNotificationMethod; // These gets stored in session memory
    private String email;
    private String smsNumber;
    private Map<ServicePointProviderEnum, List<DeliveryPointType>> deliveryPointsPerProvider = new HashMap();
    private Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>>
            possibleCollectCombinationsFittingAllWithNotificationMethods = new HashMap<>();

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
            deliveryNotificationMethod = DeliveryNotificationMethodEnum.SMS;
        } else if (userProfile.isHasMailNotification() != null && userProfile.isHasMailNotification()) {
            deliveryNotificationMethod = DeliveryNotificationMethodEnum.E_POST;
        } else {
            deliveryNotificationMethod = DeliveryNotificationMethodEnum.BREV;
        }
    }

    public Map<ServicePointProviderEnum, List<SelectItemGroup>> getDeliverySelectItems() {

        Map<ServicePointProviderEnum, List<SelectItemGroup>> selectOneMenuLists = new HashMap<>();

        if (deliveryPointsPerProvider == null) {
            loadDeliveryPointsForAllSuppliers(zip);
        }

        if (possibleCollectCombinationsFittingAllWithNotificationMethods.size() > 0) {
            // We have at least one which may satisfy all prescription items. Take the first (probably there will never
            // be more than one in the collection)...

            ServicePointProviderEnum provider = possibleCollectCombinationsFittingAllWithNotificationMethods.keySet()
                    .iterator().next();

            List<SelectItemGroup> singleSelectMenuItems = getSingleSelectMenuItems(provider);

            selectOneMenuLists.put(provider, singleSelectMenuItems);
        } else {
            // We don't have any single provider satisfying all items. The user needs to chosse service point for the
            // provider of each item.
            for (PrescriptionItemType item : cart.getItemsInCart()) {
                ServicePointProviderEnum servicePointProviderForItem = null;
                for (DeliveryAlternativeType deliveryAlternative : item.getDeliveryAlternative()) {
                    if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {
                        servicePointProviderForItem = deliveryAlternative.getServicePointProvider();
                        break;
                    }
                }

                if (servicePointProviderForItem == null) {
                    // If this happens the item cannot be collected so we skip this.
                    continue;
                }

                List<SelectItemGroup> singleSelectMenuItems = getSingleSelectMenuItems(servicePointProviderForItem);

                selectOneMenuLists.put(servicePointProviderForItem, singleSelectMenuItems);
            }

        }

        return selectOneMenuLists;
    }

    List<SelectItemGroup> getSingleSelectMenuItems(ServicePointProviderEnum provider) {
        SelectItemGroup group1 = new SelectItemGroup("Närmaste ombud");
        SelectItemGroup group2 = new SelectItemGroup("Övriga ombud till ditt postnummer");

        List<SelectItem> toGroup1 = new ArrayList<>();
        List<SelectItem> toGroup2 = new ArrayList<>();

        int count = 0;
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

        group1.setSelectItems(toGroup1.toArray(new SelectItem[0]));
        group2.setSelectItems(toGroup2.toArray(new SelectItem[0]));

        List<SelectItemGroup> toReturn = new ArrayList<>();

        toReturn.add(group1);
        toReturn.add(group2);

        return toReturn;
    }

    public Map<String, DeliveryPointType> getDeliveryPointsMap() {
        return deliveryPointsMap;
    }

    public String getChosenDeliveryPoint() {
        return chosenDeliveryPoint;
    }

    public void setChosenDeliveryPoint(String chosenDeliveryPoint) {
        this.chosenDeliveryPoint = chosenDeliveryPoint;
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

    public DeliveryNotificationMethodEnum getDeliveryNotificationMethod() {
        return deliveryNotificationMethod;
    }

    public void setDeliveryNotificationMethod(DeliveryNotificationMethodEnum deliveryNotificationMethod) {
        this.deliveryNotificationMethod = deliveryNotificationMethod;
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

            GetMedicalSupplyDeliveryPointsResponseType medicalSupplyDeliveryPoints =
                    lmnService.getMedicalSupplyDeliveryPoints(provider, zip);

            deliveryPointsPerProvider.put(provider, medicalSupplyDeliveryPoints.getDeliveryPoint());
        }
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
