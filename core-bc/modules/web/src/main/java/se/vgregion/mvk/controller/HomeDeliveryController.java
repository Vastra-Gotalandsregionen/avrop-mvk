package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import se.vgregion.mvk.controller.model.Cart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HomeDeliveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeDeliveryController.class);

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private Cart cart;

    @Autowired
    private DeliveryController deliveryController;

    private Map<PrescriptionItemType, List<String>> deliveryNotificationMethodsPerItem;
    private Map<PrescriptionItemType, String> chosenDeliveryNotificationMethod;

    private String doorCode;
    private boolean nextViewIsCollectDelivery;

    private String fullName;
    private String coAddress;
    private String address;
    private String zip;
    private String city;

    private String smsNumber;
    private String email;
    private String phoneNumber;

    public String toVerifyDelivery() {
        if (nextViewIsCollectDelivery) {
            return "collectDelivery" + ACTION_SUFFIX;
        } else {
            return "verifyDelivery" + ACTION_SUFFIX;
        }
    }

    public Map<PrescriptionItemType, List<String>> getDeliveryNotificationMethodsPerItem() {
        if (deliveryNotificationMethodsPerItem == null) {
            List<DeliveryNotificationMethodEnum> overlapping = findOverlappingDeliveryNotificationMethods();

            if (overlapping.size() > 0) {
                // We make a fictional item with null as name.
                ArticleType fictionalArticle = new ArticleType();
                fictionalArticle.setArticleName(null);

                PrescriptionItemType fictionalItem = new PrescriptionItemType();
                fictionalItem.setArticle(fictionalArticle);

                List<String> commonDeliveryNotificationMethods = overlapping
                        .stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());

                deliveryNotificationMethodsPerItem = new HashMap<>();
                deliveryNotificationMethodsPerItem.put(fictionalItem, commonDeliveryNotificationMethods);

            } else {

                deliveryNotificationMethodsPerItem = new HashMap<>();

                Map<PrescriptionItemType, String> deliveryMethodForEachItem = deliveryController
                        .getDeliveryMethodForEachItem();

                // We want to present all items which have HEMLEVERANS as delivery method.
                for (Map.Entry<PrescriptionItemType, String> entry : deliveryMethodForEachItem.entrySet()) {

                    if (entry.getValue().equals(DeliveryMethodEnum.HEMLEVERANS.name())) {

                        // We found the HEMLEVERANS delivery alternative (we assume there's only one).
                        // We filter out delivery alternatives where delivery method is HEMLEVERANS and also have at
                        // least one delivery notification method.
                        entry.getKey().getDeliveryAlternative()
                                .stream()
                                .filter(alternative ->
                                        alternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)
                                                && alternative.getDeliveryNotificationMethod() != null
                                                && alternative.getDeliveryNotificationMethod().size() > 0)
                                .forEach(alternative -> {

                                    // We found the HEMLEVERANS delivery alternative (we assume there's only one).
                                    deliveryNotificationMethodsPerItem.put(entry.getKey(),
                                            alternative.getDeliveryNotificationMethod()
                                                    .stream()
                                                    .map(Enum::name)
                                                    .collect(Collectors.toCollection(ArrayList<String>::new)));
                                });
                    }
                }
            }
        }

        return deliveryNotificationMethodsPerItem;
    }

    private List<DeliveryNotificationMethodEnum> findOverlappingDeliveryNotificationMethods() {

        List<DeliveryNotificationMethodEnum> remaining = new ArrayList<>(
                Arrays.asList(DeliveryNotificationMethodEnum.values()));

        Map<PrescriptionItemType, String> deliveryMethodForEachItem = deliveryController
                .getDeliveryMethodForEachItem();

        Set<Map.Entry<PrescriptionItemType, String>> entries = deliveryMethodForEachItem.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(DeliveryMethodEnum.HEMLEVERANS.name())
                        && atLeastOneHomeDeliveryMethodWithNotification(entry.getKey()))
                .collect(Collectors.toSet());

        if (entries.size() == 0) {
            remaining.clear();
        }


        // We want to present all items which have HEMLEVERANS as chosen delivery method.
        for (Map.Entry<PrescriptionItemType, String> entry : entries) {

            // Now we want to find the delivery alternative with the delivery method we are looking for (HEMLEVERANS)
            // and also only alternatives which have at least one notification method.
            // When found the intersection is retained.
            entry.getKey().getDeliveryAlternative()
                    .stream()
                    .filter(alternative -> alternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)
                    && alternative.getDeliveryNotificationMethod() != null
                    && alternative.getDeliveryNotificationMethod().size() > 0)
                    .forEach(alternative -> {

                // We found the HEMLEVERANS delivery alternative (we assume there's only one).
                remaining.retainAll(alternative.getDeliveryNotificationMethod());
            });
        }

        return remaining;
    }

    private boolean atLeastOneHomeDeliveryMethodWithNotification(PrescriptionItemType item) {

        for (DeliveryAlternativeType deliveryAlternative : item.getDeliveryAlternative()) {
            if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)) {
                if (deliveryAlternative.getDeliveryNotificationMethod().size() > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public Map<PrescriptionItemType, String> getChosenDeliveryNotificationMethod() {
        if (chosenDeliveryNotificationMethod == null) {
            initChosenDeliveryNotificationMethod();
        }

        // If there are remaining entries from when the user had chosen more items to order.
        chosenDeliveryNotificationMethod.keySet().retainAll(getDeliveryNotificationMethodsPerItem().keySet());

        return chosenDeliveryNotificationMethod;
    }

    private void initChosenDeliveryNotificationMethod() {
        chosenDeliveryNotificationMethod = new HashMap<>();

        Map<PrescriptionItemType, List<String>> deliveryNotificationMethodsPerProvider =
                getDeliveryNotificationMethodsPerItem();

        for (Map.Entry<PrescriptionItemType, List<String>> entry :
                deliveryNotificationMethodsPerProvider.entrySet()) {

            if (entry.getValue() != null && entry.getValue().size() > 0) {
                chosenDeliveryNotificationMethod.put(entry.getKey(), entry.getValue().get(0));
            }
        }
    }

    void resetChoices() {
        deliveryNotificationMethodsPerItem = null;
        chosenDeliveryNotificationMethod = null;
    }

    public void setDoorCode(String doorCode) {
        this.doorCode = doorCode;
    }

    public String getDoorCode() {
        return doorCode;
    }

    void setNextViewIsCollectDelivery(boolean nextViewIsCollectDelivery) {
        this.nextViewIsCollectDelivery = nextViewIsCollectDelivery;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {

        if (fullName == null) {
            UserProfileType userProfile = userProfileController.getUserProfile().getUserProfile();

            fullName = userProfile.getFirstName() + " " + userProfile.getLastName();
        }

        return fullName;
    }

    public String getCoAddress() {
        return coAddress;
    }

    public void setCoAddress(String coAddress) {
        this.coAddress = coAddress;
    }

    public String getAddress() {
        if (address == null) {
            UserProfileType userProfile = userProfileController.getUserProfile().getUserProfile();

            address = userProfile.getStreetAddress();
        }

        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        if (zip == null) {
            UserProfileType userProfile = userProfileController.getUserProfile().getUserProfile();

            zip = userProfile.getZip();
        }

        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        if (city == null) {
            UserProfileType userProfile = userProfileController.getUserProfile().getUserProfile();

            city = userProfile.getCity();
        }

        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        if (phoneNumber == null) {
            UserProfileType userProfile = userProfileController.getUserProfile().getUserProfile();

            phoneNumber = userProfile.getPhoneNumber();

            if (phoneNumber == null || "".equals(phoneNumber)) {
                phoneNumber = userProfile.getMobilePhoneNumber();
            }
        }

        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
