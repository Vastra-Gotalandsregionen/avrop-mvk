package se._1177.lmn.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.AddressType;
import riv.crm.selfservice.medicalsupply._1.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._1.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.HomeDeliveryNotificationModel;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.service.util.Util;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum.BREV;
import static se._1177.lmn.controller.HomeDeliveryController.NotificationOrDoorDelivery.DOOR;
import static se._1177.lmn.controller.HomeDeliveryController.NotificationOrDoorDelivery.NOTIFICATION;
import static se._1177.lmn.controller.HomeDeliveryController.NotificationVariant.BOTH_WITH_AND_WITHOUT_NOTIFICATION;
import static se._1177.lmn.controller.HomeDeliveryController.NotificationVariant.WITHOUT_NOTIFICATION;
import static se._1177.lmn.controller.HomeDeliveryController.NotificationVariant.WITH_NOTIFICATION;
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

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

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

    private NotificationOrDoorDelivery notificationOrDoorDelivery;

    private List<PrescriptionItemType> notificationOptional;
    private List<PrescriptionItemType> notificationMandatory;
    private List<PrescriptionItemType> notificationUnavailable;

    private HomeDeliveryNotificationModel notificationOptionalModel;
    private HomeDeliveryNotificationModel notificationMandatoryModel;

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
                preferredDeliveryNotificationMethod = BREV;
            }

            smsNumber = userProfile.getMobilePhoneNumber();
            email = userProfile.getEmail();

        } else {
            preferredDeliveryNotificationMethod = BREV;
        }

        initNotificationGroups();
    }

    public void initNotificationGroups() {
        // Group the PrescriptionItemTypes into different groups - those requiring choice of notification or not,
        // those requiring choice of notification, and those without notification.

        List<PrescriptionItemType> notificationOptional = new ArrayList<>();
        List<PrescriptionItemType> notificationMandatory = new ArrayList<>();
        List<PrescriptionItemType> notificationUnavailable = new ArrayList<>();

        Map<PrescriptionItemType, String> deliveryMethodForEachItem = deliveryController
                .getDeliveryMethodForEachItem();

        deliveryMethodForEachItem.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(DeliveryMethodEnum.HEMLEVERANS.name()))
                .map(Map.Entry::getKey)
                .forEach(prescriptionItemType -> {
                    switch (hasWithAndWithoutNotification(prescriptionItemType)) {
                        case BOTH_WITH_AND_WITHOUT_NOTIFICATION:
                            notificationOptional.add(prescriptionItemType);
                            break;
                        case WITH_NOTIFICATION:
                            notificationMandatory.add(prescriptionItemType);
                            break;
                        case WITHOUT_NOTIFICATION:
                            notificationUnavailable.add(prescriptionItemType);
                            break;
                    }
                });

        this.notificationOptional = notificationOptional;
        this.notificationMandatory = notificationMandatory;
        this.notificationUnavailable = notificationUnavailable;

        notificationOptionalModel =
                new HomeDeliveryNotificationModel(notificationOptional, preferredDeliveryNotificationMethod, smsNumber,
                        email);

        notificationMandatoryModel =
                new HomeDeliveryNotificationModel(notificationMandatory, preferredDeliveryNotificationMethod, smsNumber,
                        email);
    }


    private void addMessage(String summary, String componentId, int count) {
        FacesMessage msg = new FacesMessage(summary);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);

        FacesContext fc = FacesContext.getCurrentInstance();

        fc.addMessage("homeDeliveryForm:notificationMethodRepeat:" + count + ":" + componentId, msg);
    }

    public String toVerifyDelivery() {
        boolean doorOrValidated = DOOR.equals(notificationOrDoorDelivery)
                || notificationOptionalModel.validateNotificationInput();

        boolean success = doorOrValidated
                && notificationMandatoryModel.validateNotificationInput();

        if (!success) {
            return "homeDelivery";
        }

        List<OrderRowType> orderRowsWithHomeDelivery = cart.getOrderRows().stream()
                .filter(orderRowType -> homeDeliveryChosen(orderRowType)).collect(Collectors.toList());

        // Add info from this step to the order rows
        orderRowsWithHomeDelivery.forEach(orderRowType -> {
            // Add info from this step to the order rows.
            PrescriptionItemType item = prescriptionItemInfo.getPrescriptionItem(orderRowType);

            // Take the first deliveryAlternative with matching deliveryMethod and service point provider. This
            // assumes no two deliveryAlternatives share the same deliveryMethod and service point provider. That
            // would lead to arbitrary result.
            NotificationVariant notificationVariant = hasWithAndWithoutNotification(item);
            DeliveryChoiceType deliveryChoice = orderRowType.getDeliveryChoice();

            String deliveryMethodId = findTheDeliveryMethodId(item, notificationVariant, deliveryChoice);

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

            String notificationMethod;
            String smsNumber;
            String email;
            String phoneNumber;

            switch (notificationVariant) {
                case BOTH_WITH_AND_WITHOUT_NOTIFICATION:
                    if (notificationOrDoorDelivery.equals(NOTIFICATION)) {
                        notificationMethod = notificationOptionalModel.getChosenDeliveryNotificationMethod().get(item);
                        smsNumber = notificationOptionalModel.getSmsNumber();
                        email = notificationOptionalModel.getEmail();
                        phoneNumber = notificationOptionalModel.getPhoneNumber();
                    } else {
                        notificationMethod = null;
                        smsNumber = null;
                        email = null;
                        phoneNumber = null;
                    }
                    break;
                case WITH_NOTIFICATION:
                    notificationMethod = notificationMandatoryModel.getChosenDeliveryNotificationMethod().get(item);
                    smsNumber = notificationOptionalModel.getSmsNumber();
                    email = notificationOptionalModel.getEmail();
                    phoneNumber = notificationOptionalModel.getPhoneNumber();
                    break;
                case WITHOUT_NOTIFICATION:
                    notificationMethod = null;
                    smsNumber = null;
                    email = null;
                    phoneNumber = null;
                    break;
                default:
                    throw new IllegalStateException("Should never get here.");
            }

            if (notificationMethod != null) {
                DeliveryNotificationMethodEnum method = DeliveryNotificationMethodEnum.fromValue(notificationMethod);
                JAXBElement<DeliveryNotificationMethodEnum> value = Util.wrapInJAXBElement(method);
                deliveryChoice.setDeliveryNotificationMethod(value);

                switch (method) {
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
            }
        });


        if (nextViewIsCollectDelivery) {
            return "collectDelivery" + ACTION_SUFFIX;
        } else {
            return "verifyDelivery" + ACTION_SUFFIX;
        }
    }

    private String findTheDeliveryMethodId(PrescriptionItemType prescriptionItem,
                                           NotificationVariant notificationVariant,
                                           DeliveryChoiceType deliveryChoice) {

        for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {

            DeliveryMethodEnum deliveryMethod = deliveryAlternative.getDeliveryMethod();

            List<DeliveryNotificationMethodEnum> deliveryNotificationMethod =
                    deliveryAlternative.getDeliveryNotificationMethod();

            int size = deliveryNotificationMethod.size();

            boolean deliveryMethodMatches = deliveryMethod.equals(deliveryChoice.getDeliveryMethod());
            boolean notificationUnavailableMatches = notificationVariant.equals(WITHOUT_NOTIFICATION) && size == 0;
            boolean notificationMandatoryMatches = notificationVariant.equals(WITH_NOTIFICATION) && size > 0;

            boolean notificationOptionalWithNotificationMatches =
                    notificationVariant.equals(BOTH_WITH_AND_WITHOUT_NOTIFICATION)
                            && notificationOrDoorDelivery.equals(NOTIFICATION) && size > 0;

            boolean notificationOptionalWithoutNotificationMatches =
                    notificationVariant.equals(BOTH_WITH_AND_WITHOUT_NOTIFICATION)
                            && notificationOrDoorDelivery.equals(DOOR) && size == 0;

            boolean notificationMatches = notificationUnavailableMatches
                    || notificationMandatoryMatches
                    || notificationOptionalWithNotificationMatches
                    || notificationOptionalWithoutNotificationMatches;

            if (deliveryMethodMatches && notificationMatches) {
                return deliveryAlternative.getDeliveryMethodId();
            }
        }

        throw new IllegalStateException("Couldn't find the delivery method id.");
    }

    private boolean homeDeliveryChosen(OrderRowType orderRowType) {
        return orderRowType.getDeliveryChoice().getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS);
    }

    /** TODO Move and change this floating javadoc.
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


    public Map<PrescriptionItemType, String> getChosenDeliveryNotificationMethod() {
        Map<PrescriptionItemType, String> aggregated = new HashMap<>();

        if (NOTIFICATION.equals(notificationOrDoorDelivery)) {
            aggregated.putAll(notificationOptionalModel.getChosenDeliveryNotificationMethod());
        }

        aggregated.putAll(notificationMandatoryModel.getChosenDeliveryNotificationMethod());

        return aggregated;
    }

    void resetChoices() {
        initNotificationGroups();
        notificationOrDoorDelivery = null;
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

    public NotificationOrDoorDelivery getNotificationOrDoorDelivery() {
        return notificationOrDoorDelivery;
    }

    public void setNotificationOrDoorDelivery(NotificationOrDoorDelivery notificationOrDoorDelivery) {
        this.notificationOrDoorDelivery = notificationOrDoorDelivery;
    }

    public NotificationVariant hasWithAndWithoutNotification(PrescriptionItemType prescriptionItemType) {
        if (prescriptionItemType == null || prescriptionItemType.getDeliveryAlternative() == null
                || prescriptionItemType.getDeliveryAlternative().size() == 0) {

            throw new IllegalArgumentException("At least one delivery alternative must be available.");
        }

        final boolean[] hasWithNotification = {false};
        final boolean[] hasWithoutNotification = {false};

        prescriptionItemType.getDeliveryAlternative().forEach(deliveryAlternativeType -> {
            if (deliveryAlternativeType.getDeliveryNotificationMethod() == null
                    || deliveryAlternativeType.getDeliveryNotificationMethod().size() == 0) {

                hasWithoutNotification[0] = true;
            } else {
                hasWithNotification[0] = true;
            }
        });


        if (hasWithNotification[0] && hasWithoutNotification[0]) {
            return BOTH_WITH_AND_WITHOUT_NOTIFICATION;
        } else if (hasWithNotification[0]) {
            return WITH_NOTIFICATION;
        } else if (hasWithoutNotification[0]) {
            return WITHOUT_NOTIFICATION;
        } else {
            throw new RuntimeException("Should never get here");
        }
    }

    public List<PrescriptionItemType> getNotificationOptional() {
        return notificationOptional;
    }

    public List<PrescriptionItemType> getNotificationMandatory() {
        return notificationMandatory;
    }

    public List<PrescriptionItemType> getNotificationUnavailable() {
        return notificationUnavailable;
    }

    public HomeDeliveryNotificationModel getNotificationOptionalModel() {
        return notificationOptionalModel;
    }

    public void setNotificationOptionalModel(HomeDeliveryNotificationModel notificationOptionalModel) {
        this.notificationOptionalModel = notificationOptionalModel;
    }

    public HomeDeliveryNotificationModel getNotificationMandatoryModel() {
        return notificationMandatoryModel;
    }

    public void setNotificationMandatoryModel(HomeDeliveryNotificationModel notificationMandatoryModel) {
        this.notificationMandatoryModel = notificationMandatoryModel;
    }

    static enum NotificationVariant {
        WITH_NOTIFICATION, WITHOUT_NOTIFICATION, BOTH_WITH_AND_WITHOUT_NOTIFICATION
    }

    static enum NotificationOrDoorDelivery {
        NOTIFICATION, DOOR
    }
}
