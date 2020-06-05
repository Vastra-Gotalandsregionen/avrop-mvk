package se._1177.lmn.controller;

import lombok.val;
import mvk.itintegration.userprofile._2.UserProfileType;
import org.apache.commons.lang3.BooleanUtils;
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
import se._1177.lmn.controller.model.AddressModel;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.HomeDeliveryNotificationModel;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.controller.session.HomeDeliveryControllerSession;
import se._1177.lmn.model.NotificationOrDoorDelivery;
import se._1177.lmn.model.NotificationVariant;
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

import static riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum.*;
import static se._1177.lmn.model.NotificationOrDoorDelivery.DOOR;
import static se._1177.lmn.model.NotificationOrDoorDelivery.NOTIFICATION;
import static se._1177.lmn.model.NotificationVariant.*;
import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HomeDeliveryController {

    public static final String VIEW_NAME = "Hemleverans eller annan leveransadress";

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeDeliveryController.class);

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private Cart cart;

    @Autowired
    private DeliveryController deliveryController;

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

    @Autowired
    private NavigationController navigationController;

    @Autowired
    private HomeDeliveryControllerSession sessionData;

    @PostConstruct
    public void init() {
        if (!sessionData.isInited()) {
            sessionData.setAddressModel(new AddressModel());
            sessionData.getAddressModel().init(userProfileController);

            UserProfileType userProfile = userProfileController.getUserProfile();

            if (userProfile != null) {

                if (userProfile.isHasSmsNotification() != null && userProfile.isHasSmsNotification()) {
                    sessionData.setPreferredDeliveryNotificationMethod(SMS);
                } else if (userProfile.isHasMailNotification() != null && userProfile.isHasMailNotification()) {
                    sessionData.setPreferredDeliveryNotificationMethod(E_POST);
                } else {
                    sessionData.setPreferredDeliveryNotificationMethod(BREV);
                }

                sessionData.setSmsNumber(userProfile.getMobilePhoneNumber());
                sessionData.setEmail(userProfile.getEmail());

            } else {
                sessionData.setPreferredDeliveryNotificationMethod(BREV);
            }

            initNotificationGroups();

            sessionData.setInited(true);
        }
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

        sessionData.setNotificationOptional(notificationOptional);
        sessionData.setNotificationMandatory(notificationMandatory);
        sessionData.setNotificationUnavailable(notificationUnavailable);

        HomeDeliveryNotificationModel notificationOptionalModel = sessionData.getNotificationOptionalModel();
        DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod = sessionData
                .getPreferredDeliveryNotificationMethod();
        String smsNumber = sessionData.getSmsNumber();
        String email = sessionData.getEmail();
        HomeDeliveryNotificationModel notificationMandatoryModel = sessionData.getNotificationMandatoryModel();

        String previousPhoneNumber = notificationOptionalModel != null ? notificationOptionalModel.getPhoneNumber()
                : null;

        sessionData.setNotificationOptionalModel(
                new HomeDeliveryNotificationModel(notificationOptional, preferredDeliveryNotificationMethod, smsNumber,
                        email, previousPhoneNumber, "homeDeliveryForm:optional:notificationMethodRepeat:")
        );

        previousPhoneNumber = notificationMandatoryModel != null ? notificationMandatoryModel.getPhoneNumber()
                : null;

        sessionData.setNotificationMandatoryModel(
                new HomeDeliveryNotificationModel(notificationMandatory, preferredDeliveryNotificationMethod, smsNumber,
                        email, previousPhoneNumber, "homeDeliveryForm:mandatory:notificationMethodRepeat:")
        );
    }


    private void addMessage(String summary, String componentId) {
        FacesMessage msg = new FacesMessage(summary);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);

        FacesContext fc = FacesContext.getCurrentInstance();

        fc.addMessage("homeDeliveryForm:" + componentId, msg);
    }

    public String toVerifyDelivery() {

        boolean validateOptionalModel;
        if (sessionData.getNotificationOptional().size() > 0) {
            NotificationOrDoorDelivery notificationOrDoorDelivery = sessionData.getNotificationOrDoorDelivery();
            HomeDeliveryNotificationModel notificationOptionalModel = sessionData.getNotificationOptionalModel();

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

        boolean validateMandatoryModel = sessionData.getNotificationMandatoryModel().validateNotificationInput();

        boolean success = validateOptionalModel
                && validateMandatoryModel;

        if (!success) {
            return "homeDelivery";
        }

        List<OrderRowType> orderRowsWithHomeDelivery = getOrderRowsWithHomeDelivery();

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

            AddressModel addressModel = sessionData.getAddressModel();

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

            deliveryChoice.setDeliveryComment(sessionData.getDeliveryComment());

            String notificationMethod;
            String smsNumber;
            String email;
            String phoneNumber;

            switch (notificationVariant) {
                case BOTH_WITH_AND_WITHOUT_NOTIFICATION:
                    if (sessionData.getNotificationOrDoorDelivery().equals(NOTIFICATION)) {

                        HomeDeliveryNotificationModel notificationOptionalModel = sessionData
                                .getNotificationOptionalModel();

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
                    HomeDeliveryNotificationModel notificationMandatoryModel = sessionData
                            .getNotificationMandatoryModel();

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

        if (sessionData.isNextViewIsCollectDelivery()) {
            return navigationController.gotoView("collectDelivery" + ACTION_SUFFIX, CollectDeliveryController.VIEW_NAME);
        } else if (anyItemHasAllowOtherInvoiceAddress()) {
            return navigationController.gotoView("invoiceAddress" + ACTION_SUFFIX, InvoiceAddressController.VIEW_NAME);
        } else {
            return navigationController.gotoView("verifyDelivery" + ACTION_SUFFIX, VerifyDeliveryController.VIEW_NAME);
        }
    }

    private boolean anyItemHasAllowOtherInvoiceAddress() {
        return prescriptionItemInfo.getChosenPrescriptionItemInfoList()
                .stream()
                .anyMatch(item -> item.isAllowOtherInvoiceAddress() != null && item.isAllowOtherInvoiceAddress());
    }

    private DeliveryAlternativeType findDeliveryAlternative(OrderRowType orderRowType) {
        PrescriptionItemType item = prescriptionItemInfo.getPrescriptionItem(orderRowType);

        // Take the first deliveryAlternative with matching deliveryMethod and service point provider. This
        // assumes no two deliveryAlternatives share the same deliveryMethod and service point provider. That
        // would lead to arbitrary result.
        NotificationVariant notificationVariant = hasWithAndWithoutNotificationForHomeDelivery(item);
        DeliveryChoiceType deliveryChoice = orderRowType.getDeliveryChoice();

        return findTheDeliveryAlternative(item, notificationVariant, deliveryChoice);
    }

    private List<OrderRowType> getOrderRowsWithHomeDelivery() {
        return cart.getOrderRows().stream()
                    .filter(orderRowType -> homeDeliveryChosen(orderRowType)).collect(Collectors.toList());
    }

    private String findTheDeliveryMethodId(PrescriptionItemType prescriptionItem,
                                           NotificationVariant notificationVariant,
                                           DeliveryChoiceType deliveryChoice) {

        return findTheDeliveryAlternative(prescriptionItem, notificationVariant, deliveryChoice).getDeliveryMethodId();
    }

    private DeliveryAlternativeType findTheDeliveryAlternative(PrescriptionItemType prescriptionItem,
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
                            && sessionData.getNotificationOrDoorDelivery().equals(NOTIFICATION) && size > 0;

            boolean notificationOptionalWithoutNotificationMatches =
                    notificationVariant.equals(BOTH_WITH_AND_WITHOUT_NOTIFICATION)
                            && sessionData.getNotificationOrDoorDelivery().equals(DOOR) && size == 0;

            boolean notificationMatches = notificationUnavailableMatches
                    || notificationMandatoryMatches
                    || notificationOptionalWithNotificationMatches
                    || notificationOptionalWithoutNotificationMatches;

            if (deliveryMethodMatches && notificationMatches) {
                return deliveryAlternative;
            }
        }

        throw new IllegalStateException("Couldn't find the delivery method id.");
    }

    private boolean homeDeliveryChosen(OrderRowType orderRowType) {
        return orderRowType.getDeliveryChoice().getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS);
    }

    public Map<PrescriptionItemType, String> getChosenDeliveryNotificationMethod() {
        Map<PrescriptionItemType, String> aggregated = new HashMap<>();

        if (NOTIFICATION.equals(sessionData.getNotificationOrDoorDelivery())) {
            aggregated.putAll(sessionData.getNotificationOptionalModel().getChosenDeliveryNotificationMethod());
        }

        aggregated.putAll(sessionData.getNotificationMandatoryModel().getChosenDeliveryNotificationMethod());

        return aggregated;
    }

    public String getNotifacationReceiver(PrescriptionItemType item) {
        HomeDeliveryNotificationModel notificationMandatoryModel = sessionData.getNotificationMandatoryModel();
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
                    throw new IllegalArgumentException(
                            "This method is not expected to be called when other notification methods are chosen."
                    );
            }
        }

        HomeDeliveryNotificationModel notificationOptionalModel = sessionData.getNotificationOptionalModel();
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
                    throw new IllegalArgumentException(
                            "This method is not expected to be called when other notification methods are chosen."
                    );
            }
        }

        throw new IllegalArgumentException("Couldn't find a notification receiver.");
    }

    void resetChoices() {
        initNotificationGroups();
        sessionData.setNotificationOrDoorDelivery(null);
    }

    void setNextViewIsCollectDelivery(boolean nextViewIsCollectDelivery) {
        sessionData.setNextViewIsCollectDelivery(nextViewIsCollectDelivery);
    }

    public AddressModel getAddressModel() {
        return sessionData.getAddressModel();
    }

    public void setAddressModel(AddressModel addressModel) {
        sessionData.setAddressModel(addressModel);
    }

    public String getSmsNumber() {
        return sessionData.getSmsNumber();
    }

    public void setSmsNumber(String smsNumber) {
        sessionData.setSmsNumber(smsNumber);
    }

    public String getEmail() {
        return sessionData.getEmail();
    }

    public void setEmail(String email) {
        sessionData.setEmail(email);
    }

    public NotificationOrDoorDelivery getNotificationOrDoorDelivery() {
        return sessionData.getNotificationOrDoorDelivery();
    }

    public void setNotificationOrDoorDelivery(NotificationOrDoorDelivery notificationOrDoorDelivery) {
        sessionData.setNotificationOrDoorDelivery(notificationOrDoorDelivery);
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
        return sessionData.getNotificationOptional();
    }

    public List<PrescriptionItemType> getNotificationMandatory() {
        return sessionData.getNotificationMandatory();
    }

    public List<PrescriptionItemType> getNotificationUnavailable() {
        return sessionData.getNotificationUnavailable();
    }

    public HomeDeliveryNotificationModel getNotificationOptionalModel() {
        return sessionData.getNotificationOptionalModel();
    }

    public void setNotificationOptionalModel(HomeDeliveryNotificationModel notificationOptionalModel) {
        sessionData.setNotificationOptionalModel(notificationOptionalModel);
    }

    public HomeDeliveryNotificationModel getNotificationMandatoryModel() {
        return sessionData.getNotificationMandatoryModel();
    }

    public void setNotificationMandatoryModel(HomeDeliveryNotificationModel notificationMandatoryModel) {
        sessionData.setNotificationMandatoryModel(notificationMandatoryModel);
    }

    public Boolean isMultipleGroups() {
        int numberGroups = 0;
        numberGroups += getNotificationOptional() != null && getNotificationOptional().size() > 0 ? 1 : 0;
        numberGroups += getNotificationMandatory() != null && getNotificationMandatory().size() > 0 ? 1 : 0;
        numberGroups += getNotificationUnavailable() != null && getNotificationUnavailable().size() > 0 ? 1 : 0;

        return numberGroups > 1;
    }

    public boolean isShowDeliveryComment() {
        return getOrderRowsWithHomeDelivery().stream()
                .flatMap(orderRow -> {
                    PrescriptionItemType item = prescriptionItemInfo.getPrescriptionItem(orderRow);

                    // We'd like to check whether any of the chosen deliveryAlternatives has allowDeliveryComment=true
                    // but we may not have chosen notification variant yet so we don't know all chosen
                    // deliveryAlternatives yet, so we look for allowDeliveryComment in a slightly wider way.
                    return item.getDeliveryAlternative().stream();
                }).filter(alternative -> alternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS))
                .anyMatch(deliveryAlternative -> BooleanUtils.isTrue(deliveryAlternative.isAllowDeliveryComment()));
    }

    public String getDeliveryComment() {
        return sessionData.getDeliveryComment();
    }

    public void setDeliveryComment(String deliveryComment) {
        sessionData.setDeliveryComment(deliveryComment);
    }

    public String getViewName() {
        return VIEW_NAME;
    }

}
