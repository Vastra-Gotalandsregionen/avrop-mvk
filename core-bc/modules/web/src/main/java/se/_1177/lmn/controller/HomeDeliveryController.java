package se._1177.lmn.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.AddressType;
import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.OrderRowType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.service.util.Util;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

// TODO May need to refactor quite a bit. Make a method which determines which situation we're in: No notification
// choice, notification choice due to at least one item needs a notification choice (with grouping of items which have
// overlapping notification choices), or notification-door choice which occurs when there are only items with both
// notification and without notifications (and potentially others without notification).

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

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

    private Map<PrescriptionItemType, List<String>> deliveryNotificationMethodsPerItem;
    private Map<PrescriptionItemType, String> chosenDeliveryNotificationMethod;

    private String doorCode;
    private boolean nextViewIsCollectDelivery;

    private String fullName;
    private String coAddress;
    private String address;
    private String zip;
    private String city;

    private DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod;

    private String smsNumber;
    private String email;
    private String phoneNumber;

    private String notificationOrDoorDelivery;

    @PostConstruct
    public void init() {
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


    private void addMessage(String summary, String componentId, int count) {
        FacesMessage msg = new FacesMessage(summary);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);

        FacesContext fc = FacesContext.getCurrentInstance();

        fc.addMessage("homeDeliveryForm:notificationMethodRepeat:" + count + ":" + componentId, msg);
    }

    public String toVerifyDelivery() {
        boolean success = validateNotificationInput();

        if (!success) {
            return "homeDelivery";
        }

        List<OrderRowType> orderRowsWithHomeDelivery = cart.getOrderRows().stream()
                .filter(orderRowType -> homeDeliveryChosen(orderRowType)).collect(Collectors.toList());

        // Add info from this step to the order rows
        orderRowsWithHomeDelivery.forEach(orderRowType -> {
            // Add info from this step to the order rows.
            String deliveryMethodId = null;

            PrescriptionItemType prescriptionItem = prescriptionItemInfo.getPrescriptionItem(orderRowType);

            // Take the first deliveryAlternative with matching deliveryMethod and service point provider. This
            // assumes no two deliveryAlternatives share the same deliveryMethod and service point provider. That
            // would lead to arbitrary result.
            DeliveryChoiceType deliveryChoice = orderRowType.getDeliveryChoice();
            for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {
                if (deliveryAlternative.getDeliveryMethod().equals(deliveryChoice.getDeliveryMethod())) {
                    deliveryMethodId = deliveryAlternative.getDeliveryMethodId();
                    break;
                }
            }

            AddressType address = new AddressType();
            address.setCareOfAddress(getCoAddress());
            address.setCity(getCity());
            address.setDoorCode(getDoorCode());
            address.setPhone(getPhoneNumber());
            address.setPostalCode(getZip());
            address.setReceiver(getFullName());
            address.setStreet(getAddress());

            deliveryChoice.setHomeDeliveryAddress(address);
            deliveryChoice.setDeliveryMethodId(deliveryMethodId);

            if (getEntriesForOverlappingDeliveryNotificationMethods().size() > 0) {
//                if (getChosenDeliveryNotificationMethod().size() != 1) {
//                    throw new RuntimeException("If there are overlapping delivery notification methods only one chosen is expected");
//                }

//                for (Map.Entry<PrescriptionItemType, String> entry : getEntriesForOverlappingDeliveryNotificationMethods()) {
//                    if (entry.getKey().equals(prescriptionItem)) {
                        String name = getChosenDeliveryNotificationMethod().values().iterator().next();
                        DeliveryNotificationMethodEnum notificationMethod = DeliveryNotificationMethodEnum.fromValue(name);
                        JAXBElement<DeliveryNotificationMethodEnum> value = Util.wrapInJAXBElement(notificationMethod);
                        deliveryChoice.setDeliveryNotificationMethod(value);

                        switch (notificationMethod) {
                            case BREV:
                                deliveryChoice.setDeliveryNotificationReceiver(null);
                                break;
                            case SMS:
                                deliveryChoice.setDeliveryNotificationReceiver(smsNumber);
                                break;
                            case E_POST:
                                deliveryChoice.setDeliveryNotificationReceiver(email);
                                break;
                            case TELEFON:
                                deliveryChoice.setDeliveryNotificationReceiver(phoneNumber);
                                break;
                            default:
                                throw new IllegalStateException("Illegal notificationMethod: " + notificationMethod);
                        }
//                    }
//                }
            }
            /*String name = getChosenDeliveryNotificationMethod().get(prescriptionItem);
            DeliveryNotificationMethodEnum notificationMethod = DeliveryNotificationMethodEnum.fromValue(name);
            JAXBElement<DeliveryNotificationMethodEnum> value = Util.wrapInJAXBElement(notificationMethod);

            deliveryChoice.setDeliveryNotificationMethod(value);*/
        });


        if (nextViewIsCollectDelivery) {
            return "collectDelivery" + ACTION_SUFFIX;
        } else {
            return "verifyDelivery" + ACTION_SUFFIX;
        }
    }

    private boolean homeDeliveryChosen(OrderRowType orderRowType) {
        return orderRowType.getDeliveryChoice().getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS);
    }

    /**
     * Initially when the project is launched, there won't be any home delivery alternatives with notifications, but
     * there may be in the future, and the web service contract says it's possible so the application must support it.
     * This method tries to find overlapping {@link DeliveryNotificationMethodEnum}s which are available for all
     * {@link PrescriptionItemType}s which have any {@link DeliveryNotificationMethodEnum} at all. Then the user is only
     * need to choose once. If no overlapping {@link DeliveryNotificationMethodEnum}s are found the user needs to choose
     * a {@link DeliveryNotificationMethodEnum} for each {@link PrescriptionItemType}.
     *
     * @return {@link PrescriptionItemType}s mapped to possible {@link DeliveryNotificationMethodEnum}s as
     * {@link String}s.
     */
    public Map<PrescriptionItemType, List<String>> getDeliveryNotificationMethodsPerItem() {
        if (deliveryNotificationMethodsPerItem == null) {
            List<DeliveryNotificationMethodEnum> overlapping = findOverlappingDeliveryNotificationMethods();

            if (overlapping.size() > 0) {
                if (!"DOOR".equals(notificationOrDoorDelivery)) {
                    // We make a fictional item with null as name. This is to present only one notification choice.
                    // If we put all items mapped to commonDeliveryNotificationMethods the user would need to choose
                    // for each item.
                    ArticleType fictionalArticle = new ArticleType();
                    fictionalArticle.setArticleName(null);

                    PrescriptionItemType fictionalItem = new PrescriptionItemType();
                    fictionalItem.setArticle(fictionalArticle);

                    List<String> commonDeliveryNotificationMethods = overlapping
                            .stream()
                            .map(Enum::name)
                            .collect(Collectors.toList());

                    deliveryNotificationMethodsPerItem = new HashMap<>();

                    /*for (Map.Entry<PrescriptionItemType, String> entry : getEntriesForOverlappingDeliveryNotificationMethods()) {
                        deliveryNotificationMethodsPerItem.put(entry.getKey(), commonDeliveryNotificationMethods);
                    }*/
                    deliveryNotificationMethodsPerItem.put(fictionalItem, commonDeliveryNotificationMethods);
                } else {
                    deliveryNotificationMethodsPerItem = new HashMap<>();
                }

            } else {

                deliveryNotificationMethodsPerItem = new HashMap<>();

                if (!"DOOR".equals(notificationOrDoorDelivery)) {
                    // "DOOR" takes precedence, so no notificationMethods if "DOOR" is chosen.
                    Map<PrescriptionItemType, String> deliveryMethodForEachItem = deliveryController
                            .getDeliveryMethodForEachItem();

                    // We want to present all items which have HEMLEVERANS as delivery method.
                    for (Map.Entry<PrescriptionItemType, String> entry : deliveryMethodForEachItem.entrySet()) {

                        if (entry.getValue().equals(DeliveryMethodEnum.HEMLEVERANS.name())) {

                            // We found the HEMLEVERANS delivery alternative (we assume there's only one).
                            // We filter out delivery alternatives where delivery method is HEMLEVERANS and also have at
                            // least one delivery notification method.

//                            long count = entry.getKey().getDeliveryAlternative().stream().map(alt -> alt.getDeliveryMethod())
//                                    .filter(deliveryMethodEnum -> deliveryMethodEnum.equals(DeliveryMethodEnum.HEMLEVERANS))
//                                    .count();

                            // We only regard those with only "notification home delivery option".
                            if (hasHomeDeliveryOnlyWithNotifications(entry.getKey())) {
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
            }
        }

        return deliveryNotificationMethodsPerItem;
    }

    private List<DeliveryNotificationMethodEnum> findOverlappingDeliveryNotificationMethods() {

        List<DeliveryNotificationMethodEnum> remaining = new ArrayList<>(
                Arrays.asList(DeliveryNotificationMethodEnum.values()));

        Set<Map.Entry<PrescriptionItemType, String>> entries =
                getEntriesForOverlappingDeliveryNotificationMethods();

        if (entries.size() == 0) {
            remaining.clear();
        }


        // We want to present all items which have HEMLEVERANS as chosen delivery method.
        for (Map.Entry<PrescriptionItemType, String> entry : entries) {

            if (!hasHomeDeliveryOnlyWithNotifications(entry.getKey())) { // todo or has made "NOTIFICATION" choice???
                // We don't care about those where we don't need to choose notification
                continue;
            }
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

    private Set<Map.Entry<PrescriptionItemType, String>> getEntriesForOverlappingDeliveryNotificationMethods() {
        Map<PrescriptionItemType, String> deliveryMethodForEachItem = deliveryController
                .getDeliveryMethodForEachItem();

        return deliveryMethodForEachItem.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(DeliveryMethodEnum.HEMLEVERANS.name())
                            && (hasHomeDeliveryOnlyWithNotifications(entry.getKey()) || getMustChooseNotificationForAnyItem()))
                    .collect(Collectors.toSet());
    }

    private boolean hasHomeDeliveryOnlyWithNotifications(PrescriptionItemType item) {

        boolean onlyWithNotifications = true;
        for (DeliveryAlternativeType deliveryAlternative : item.getDeliveryAlternative()) {
            if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)) {
                if (deliveryAlternative.getDeliveryNotificationMethod().size() > 0) {
//                    return true;
                } else {
                    onlyWithNotifications = false;
                }
            }
        }

        return onlyWithNotifications;
//        return false;
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

        deliveryNotificationMethodsPerProvider.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().size() > 0)
                .forEach(entry -> {
                    if (entry.getValue().contains(preferredDeliveryNotificationMethod.name())) {
                        chosenDeliveryNotificationMethod.put(entry.getKey(), preferredDeliveryNotificationMethod.name());
                    } else {
                        // In case the preferred one isn't available.
                        String defaultNotificationMethod = entry.getValue().size() > 0 ? entry.getValue().get(0) : null;
                        chosenDeliveryNotificationMethod.put(entry.getKey(), defaultNotificationMethod);
                    }
                });
    }

    public boolean validateNotificationInput() {

        final int[] count = {0};

        final boolean[] validationSuccess = {true};

        getDeliveryNotificationMethodsPerItem().entrySet().forEach(entry -> {
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
                    addMessage("Mobiltelefon för avisering saknas", "smsInput", count[0]);
                    validationSuccess[0] = false;
                } else if (smsNumber.length() < 10) {
                    addMessage("Mobiltelefon för avisering är ogiltig.", "smsInput", count[0]);
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

        return validationSuccess[0];
    }

    void resetChoices() {
        deliveryNotificationMethodsPerItem = null;
        chosenDeliveryNotificationMethod = null;
//        notificationOrDoorDelivery = null;
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
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                fullName = userProfile.getFirstName() + " " + userProfile.getLastName();
            }
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
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                address = userProfile.getStreetAddress();
            }
        }

        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        if (zip == null) {
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                zip = userProfile.getZip();
            }
        }

        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        if (city == null) {
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                city = userProfile.getCity();
            }
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
            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {
                phoneNumber = userProfile.getPhoneNumber();

                if (phoneNumber == null || "".equals(phoneNumber)) {
                    phoneNumber = userProfile.getMobilePhoneNumber();
                }
            }
        }

        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getWithOrWithoutNotificationChoiceRequired() {
        Map<PrescriptionItemType, String> deliveryMethodForEachItem = deliveryController
                .getDeliveryMethodForEachItem();

        Set<Map.Entry<PrescriptionItemType, String>> entriesWithHomeDelivery = deliveryMethodForEachItem.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(DeliveryMethodEnum.HEMLEVERANS.name()))
                .collect(Collectors.toSet());

        boolean allItemsHasBothWithAndWithoutNotification = true;

        for (PrescriptionItemType prescriptionItemType : deliveryMethodForEachItem.keySet()) {
            Set<DeliveryAlternativeType> homeDeliveryAlternatives = prescriptionItemType.getDeliveryAlternative()
                    .stream()
                    .filter(alternative -> alternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS))
                    .collect(Collectors.toSet());

            if (homeDeliveryAlternatives.size() < 2) {
                allItemsHasBothWithAndWithoutNotification = false;
                break;
            }

            boolean anyDeliveryMethodWithNotification = false;
            boolean anyDeliveryMethodWithoutNotification = false;

            for (DeliveryAlternativeType homeDeliveryAlternative : homeDeliveryAlternatives) {
                if (homeDeliveryAlternative.getDeliveryNotificationMethod().size() > 0) {
                    anyDeliveryMethodWithNotification = true;
                } else {
                    anyDeliveryMethodWithoutNotification = true;
                }
            }

            if (!anyDeliveryMethodWithNotification || !anyDeliveryMethodWithoutNotification) {
                allItemsHasBothWithAndWithoutNotification = false;
            }

        }

        if (!allItemsHasBothWithAndWithoutNotification) {
            notificationOrDoorDelivery = null;
        }

        return allItemsHasBothWithAndWithoutNotification;
    }

    public String getNotificationOrDoorDelivery() {
        return notificationOrDoorDelivery;
    }

    public void setNotificationOrDoorDelivery(String notificationOrDoorDelivery) {
        this.notificationOrDoorDelivery = notificationOrDoorDelivery;
    }

    public Boolean getMustChooseNotificationForAnyItem() {
        if ("NOTIFICATION".equals(notificationOrDoorDelivery)) {
            return true;
        }

        boolean anyItemWithOnlyNotificationOption = false;

        Map<PrescriptionItemType, String> deliveryMethodForEachItem = deliveryController
                .getDeliveryMethodForEachItem();

        for (PrescriptionItemType prescriptionItemType : deliveryMethodForEachItem.keySet()) {
            boolean hasOptionWithoutNotification = false;

            Set<DeliveryAlternativeType> homeDeliveryAlternatives = prescriptionItemType.getDeliveryAlternative()
                    .stream()
                    .filter(alternative -> alternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS))
                    .collect(Collectors.toSet());

            for (DeliveryAlternativeType homeDeliveryAlternative : homeDeliveryAlternatives) {
                if (homeDeliveryAlternative.getDeliveryNotificationMethod().size() == 0) {
                    hasOptionWithoutNotification = true;
                }
            }

            if (!hasOptionWithoutNotification) {
                // So all options are WITH notification
                anyItemWithOnlyNotificationOption = true;
            }

        }

        return anyItemWithOnlyNotificationOption;
    }

    public void updateNotificationOrDoorDelivery() {
        // Resetting this will force a new calculation of which deliveryNotificationMethodsPerItem are possible.
        deliveryNotificationMethodsPerItem = null;
        chosenDeliveryNotificationMethod = null;
    }
}
