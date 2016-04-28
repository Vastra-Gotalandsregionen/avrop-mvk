package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import se._1177.lmn.service.LmnService;

import javax.annotation.PostConstruct;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private String chosenDeliveryPoint;
    private String zip;
    private List<DeliveryPointType> deliveryPoints;
    private Map<String, DeliveryPointType> deliveryPointsMap = new HashMap<>();
    private DeliveryNotificationMethodEnum deliveryNotificationMethod; // These gets stored in session memory
    private String email;
    private String smsNumber;
    private Map<ServicePointProviderEnum, List<DeliveryPointType>> deliveryPointsPerProvider = new HashMap();
    private Set<DeliveryMethodEnum> possibleDeliveryMethods;

    public void updateDeliverySelectItems(AjaxBehaviorEvent ajaxBehaviorEvent) {
        // Just reset deliveryPoints, making them load again when they are requested.
        deliveryPoints = null;
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

        List<SelectItemGroup> deliverySelectItems = getDeliverySelectItems();

        if (deliverySelectItems != null
                && deliverySelectItems.size() > 0
                && deliverySelectItems.get(0).getSelectItems() != null
                && deliverySelectItems.get(0).getSelectItems().length > 0) {

            chosenDeliveryPoint = (String) deliverySelectItems.get(0).getSelectItems()[0].getValue();
        }
    }

    public List<DeliveryPointType> getAllDeliveryPoints() {

        // Loads these once per session. Be careful about memory consumption under load.
        if (deliveryPoints == null) {
            deliveryPoints = deliveryPointsPerProvider.get()//lmnService.getMedicalSupplyDeliveryPoints(provider, zip).getDeliveryPoint();
            for (DeliveryPointType deliveryPoint : deliveryPoints) {
                deliveryPointsMap.put(deliveryPoint.getDeliveryPointId(), deliveryPoint);
            }

        }

        return deliveryPoints;
    }

    public List<SelectItemGroup> getDeliverySelectItems() {

        SelectItemGroup group1 = new SelectItemGroup("Närmaste ombud");
        SelectItemGroup group2 = new SelectItemGroup("Övriga ombud till ditt postnummer");

        List<SelectItem> toGroup1 = new ArrayList<>();
        List<SelectItem> toGroup2 = new ArrayList<>();

        int count = 0;
        for (DeliveryPointType deliveryPoint : getAllDeliveryPoints()) {

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

    public void setPossibleCollectCombinations(Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> possibleDeliveryNotificationMethods) {
//        this.possibleDeliveryNotificationMethods = possibleDeliveryNotificationMethods;
        throw new UnsupportedOperationException("kolla detta...");
    }

    // TODO: 2016-04-28 This is just under development. This should never be run more than once per session. That'd be unnecessary.
    private int noRunLoadDeliveryPointsForAllSuppliers = 0;
    public void loadDeliveryPointsForAllSuppliers() {
        noRunLoadDeliveryPointsForAllSuppliers++;
        if (noRunLoadDeliveryPointsForAllSuppliers > 1) {
            throw new RuntimeException("");
        }

        String zip = userProfileController.getUserProfile().getUserProfile().getZip();

        ServicePointProviderEnum[] allProviders = ServicePointProviderEnum.values();

        for (ServicePointProviderEnum provider : allProviders) {

            GetMedicalSupplyDeliveryPointsResponseType medicalSupplyDeliveryPoints =
                    lmnService.getMedicalSupplyDeliveryPoints(provider, zip);

            deliveryPointsPerProvider.put(provider, medicalSupplyDeliveryPoints.getDeliveryPoint());
        }
    }

    public void setPossibleDeliveryMethods(Set<DeliveryMethodEnum> possibleDeliveryMethods) {
        this.possibleDeliveryMethods = possibleDeliveryMethods;
    }
}
