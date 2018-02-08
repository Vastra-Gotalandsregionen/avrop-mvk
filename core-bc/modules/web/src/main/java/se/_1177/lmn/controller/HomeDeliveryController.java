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
import se._1177.lmn.controller.model.AddressModel;
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

    private AddressModel addressModel;

    private boolean nextViewIsCollectDelivery;

    private DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod;

    private String smsNumber;
    private String email;

    private NotificationOrDoorDelivery notificationOrDoorDelivery;

    private List<PrescriptionItemType> notificationOptional;
    private List<PrescriptionItemType> notificationMandatory;
    private List<PrescriptionItemType> notificationUnavailable;

    private HomeDeliveryNotificationModel notificationOptionalModel;
    private HomeDeliveryNotificationModel notificationMandatoryModel;

    @PostConstruct
    public void init() {
        addressModel = new AddressModel(userProfileController);
        addressModel.init();

        UserProfileType userProfile = userProfileController.getUserProfile();

        if (userProfile != null) {

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
                    switch (hasWithAndWithoutNotificationForHomeDelivery(prescriptionItemType)) {
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

        String previousPhoneNumber = notificationOptionalModel != null ? notificationOptionalModel.getPhoneNumber()
                : null;

        notificationOptionalModel =
                new HomeDeliveryNotificationModel(notificationOptional, preferredDeliveryNotificationMethod, smsNumber,
                        email, previousPhoneNumber, "homeDeliveryForm:optional:notificationMethodRepeat:");

        previousPhoneNumber = notificationMandatoryModel != null ? notificationMandatoryModel.getPhoneNumber()
                : null;

        notificationMandatoryModel =
                new HomeDeliveryNotificationModel(notificationMandatory, preferredDeliveryNotificationMethod, smsNumber,
                        email, previousPhoneNumber, "homeDeliveryForm:mandatory:notificationMethodRepeat:");
    }


    private void addMessage(String summary, String componentId) {
        FacesMessage msg = new FacesMessage(summary);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);

        FacesContext fc = FacesContext.getCurrentInstance();

        fc.addMessage("homeDeliveryForm:" + componentId, msg);
    }

    public String toVerifyDelivery() {

        boolean validateOptionalModel;
        if (getNotificationOptional().size() > 0) {
            if (notificationOrDoorDelivery == null) {
                addMessage("Val av Leverans utanför dörren eller Avisering saknas", "notificationOrDoorDelivery");
                return "homeDelivery";
            }

            boolean doorOrValidated = DOOR.equals(notificationOrDoorDelivery)
                    || notificationOptionalModel.validateNotificationInput();

            validateOptionalModel = doorOrValidated;
        } else {
            validateOptionalModel = true;
        }

        boolean validateMandatoryModel = notificationMandatoryModel.validateNotificationInput();

        boolean success = validateOptionalModel
                && validateMandatoryModel;

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
            NotificationVariant notificationVariant = hasWithAndWithoutNotificationForHomeDelivery(item);
            DeliveryChoiceType deliveryChoice = orderRowType.getDeliveryChoice();

            String deliveryMethodId = findTheDeliveryMethodId(item, notificationVariant, deliveryChoice);

            AddressType address = new AddressType();
            address.setCareOfAddress(addressModel.getCoAddress());
            address.setCity(addressModel.getCity());
            address.setDoorCode(addressModel.getDoorCode());
            address.setPhone(addressModel.getPhoneNumber());
            address.setPostalCode(addressModel.getZip());
            address.setReceiver(addressModel.getFullName());
            address.setStreet(addressModel.getAddress());

            deliveryChoice.setHomeDeliveryAddress(address);
            deliveryChoice.setDeliveryMethodId(deliveryMethodId);

            String notificationMethod;
            String smsNumber;
            String email;
            String phoneNumber;

            switch (notificationVariant) {
                case BOTH_WITH_AND_WITHOUT_NOTIFICATION:
                    if (notificationOrDoorDelivery.equals(NOTIFICATION)) {
                        notificationMethod = notificationOptionalModel.getChosenDeliveryNotificationMethod(item);
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
                    notificationMethod = notificationMandatoryModel.getChosenDeliveryNotificationMethod(item);
                    smsNumber = notificationMandatoryModel.getSmsNumber();
                    email = notificationMandatoryModel.getEmail();
                    phoneNumber = notificationMandatoryModel.getPhoneNumber();
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
                DeliveryNotificationMethodEnum method = DeliveryNotificationMethodEnum.valueOf(notificationMethod);
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

    public Map<PrescriptionItemType, String> getChosenDeliveryNotificationMethod() {
        Map<PrescriptionItemType, String> aggregated = new HashMap<>();

        if (NOTIFICATION.equals(notificationOrDoorDelivery)) {
            aggregated.putAll(notificationOptionalModel.getChosenDeliveryNotificationMethod());
        }

        aggregated.putAll(notificationMandatoryModel.getChosenDeliveryNotificationMethod());

        return aggregated;
    }

    public String getNotifacationReceiver(PrescriptionItemType item) {
        String method = notificationMandatoryModel.getChosenDeliveryNotificationMethod(item);
        if (method != null) {
            // The item is on the mandatory model
            switch (method) {
                case "TELEFON":
                    return notificationMandatoryModel.getPhoneNumber();
                case "SMS":
                    return notificationMandatoryModel.getSmsNumber();
                case "E_POST":
                    return notificationMandatoryModel.getEmail();
                default:
                    throw new IllegalArgumentException("This method is not expected to be called when other notification methods are chosen.");
            }
        }

        method = notificationOptionalModel.getChosenDeliveryNotificationMethod(item);
        if (method != null) {
            // The item is on the mandatory model
            switch (method) {
                case "TELEFON":
                    return notificationOptionalModel.getPhoneNumber();
                case "SMS":
                    return notificationOptionalModel.getSmsNumber();
                case "E_POST":
                    return notificationOptionalModel.getEmail();
                default:
                    throw new IllegalArgumentException("This method is not expected to be called when other notification methods are chosen.");
            }
        }

        throw new IllegalArgumentException("Couldn't find a notification receiver.");
    }

    void resetChoices() {
        initNotificationGroups();
        notificationOrDoorDelivery = null;
    }

    void setNextViewIsCollectDelivery(boolean nextViewIsCollectDelivery) {
        this.nextViewIsCollectDelivery = nextViewIsCollectDelivery;
    }

    public AddressModel getAddressModel() {
        return addressModel;
    }

    public void setAddressModel(AddressModel addressModel) {
        this.addressModel = addressModel;
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

    public NotificationOrDoorDelivery getNotificationOrDoorDelivery() {
        return notificationOrDoorDelivery;
    }

    public void setNotificationOrDoorDelivery(NotificationOrDoorDelivery notificationOrDoorDelivery) {
        this.notificationOrDoorDelivery = notificationOrDoorDelivery;
    }

    public NotificationVariant hasWithAndWithoutNotificationForHomeDelivery(PrescriptionItemType prescriptionItemType) {
        if (prescriptionItemType == null || prescriptionItemType.getDeliveryAlternative() == null
                || prescriptionItemType.getDeliveryAlternative().size() == 0) {

            throw new IllegalArgumentException("At least one delivery alternative must be available.");
        }

        final boolean[] hasWithNotification = {false};
        final boolean[] hasWithoutNotification = {false};

        prescriptionItemType.getDeliveryAlternative().stream()
                .filter(da -> da.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS))
                .forEach(deliveryAlternativeType -> {

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

    public Boolean isMultipleGroups() {
        int numberGroups = 0;
        numberGroups += getNotificationOptional() != null && getNotificationOptional().size() > 0 ? 1 : 0;
        numberGroups += getNotificationMandatory() != null && getNotificationMandatory().size() > 0 ? 1 : 0;
        numberGroups += getNotificationUnavailable() != null && getNotificationUnavailable().size() > 0 ? 1 : 0;

        return numberGroups > 1;
    }

    static enum NotificationVariant {
        WITH_NOTIFICATION, WITHOUT_NOTIFICATION, BOTH_WITH_AND_WITHOUT_NOTIFICATION
    }

    static enum NotificationOrDoorDelivery {
        NOTIFICATION, DOOR
    }
}
