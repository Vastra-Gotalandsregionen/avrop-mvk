package se.vgregion.mvk.controller;

import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageResponseType;
import mvk.itintegration.userprofile._2.SubjectOfCareType;
import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.AddressType;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.service.MvkInboxService;
import se._1177.lmn.service.MvkInboxServiceException;
import se._1177.lmn.service.util.Util;
import se.vgregion.mvk.controller.model.Cart;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

import static riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum.E_POST;
import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VerifyDeliveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyDeliveryController.class);

    @Autowired
    private LmnService lmnService;

    @Autowired
    private OrderController orderController;

    @Autowired
    private DeliveryController deliveryController;

    @Autowired
    private CollectDeliveryController collectDeliveryController;

    @Autowired
    private HomeDeliveryController homeDeliveryController;

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private MvkInboxService mvkInboxService;

    @Autowired
    private Cart cart;

    private Boolean orderSuccess;

    public String confirmOrder() {
        UserProfileType userProfile = userProfileController.getUserProfile();

        RegisterMedicalSupplyOrderResponseType response;

        HashMap<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem = new HashMap<>();

        Map<PrescriptionItemType, String> deliveryMethodForEachItem =
                deliveryController.getDeliveryMethodForEachItem();

        for (PrescriptionItemType prescriptionItem : cart.getItemsInCart()) {

            DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
            deliveryChoicePerItem.put(prescriptionItem, deliveryChoice);

            DeliveryMethodEnum deliveryMethod = DeliveryMethodEnum.fromValue(deliveryMethodForEachItem.get(
                    prescriptionItem));

            deliveryChoice.setDeliveryMethod(deliveryMethod);

            if (deliveryMethod.equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {

                String deliveryMethodId = null;

                // Take the first deliveryAlternative with matching deliveryMethod and service point provider. This
                // assumes no two deliveryAlternatives share the same deliveryMethod and service point provider. That
                // would lead to arbitrary result.
                for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {
                    if (deliveryAlternative.getDeliveryMethod().equals(deliveryMethod)
                            &&
                            collectDeliveryController.getServicePointProviderForItem(prescriptionItem)
                                    .equals(deliveryAlternative.getServicePointProvider())) {
                        deliveryMethodId = deliveryAlternative.getDeliveryMethodId();
                        break;
                    }
                }

                if (deliveryMethodId == null) {
                    FacesContext.getCurrentInstance().addMessage("", new FacesMessage("Kunde inte genomföra " +
                            "beställning. Försök senare."));

                    return "verifyDelivery";
                }

                deliveryChoice.setDeliveryMethodId(deliveryMethodId);

                ServicePointProviderEnum provider = collectDeliveryController
                        .getServicePointProviderForItem(prescriptionItem);

                String deliveryPointId = collectDeliveryController.getDeliveryPointIdsMap().get(provider);
                deliveryChoice.setDeliveryPoint(lmnService.getDeliveryPointById(deliveryPointId));

                String notificationMethodString = collectDeliveryController
                        .getChosenDeliveryNotificationMethod().get(provider);

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
                        notificationReceiver = collectDeliveryController.getEmail();
                        break;
                    case SMS:
                        notificationReceiver = collectDeliveryController.getSmsNumber();
                        break;
                    case TELEFON:
                        notificationReceiver = collectDeliveryController.getPhoneNumber();
                        break;
                    default:
                        throw new RuntimeException("Unexpected notificationMethod: " + notificationMethod);
                }

                deliveryChoice.setDeliveryNotificationReceiver(notificationReceiver);
            } else {
                String deliveryMethodId = null;

                // Take the first deliveryAlternative with matching deliveryMethod and service point provider. This
                // assumes no two deliveryAlternatives share the same deliveryMethod and service point provider. That
                // would lead to arbitrary result.
                for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {
                    if (deliveryAlternative.getDeliveryMethod().equals(deliveryMethod)) {
                        deliveryMethodId = deliveryAlternative.getDeliveryMethodId();
                        break;
                    }
                }

                AddressType address = new AddressType();
                address.setCareOfAddress(homeDeliveryController.getCoAddress());
                address.setCity(homeDeliveryController.getCity());
                address.setDoorCode(homeDeliveryController.getDoorCode());
                address.setPhone(homeDeliveryController.getPhoneNumber());
                address.setPostalCode(homeDeliveryController.getZip());
                address.setReceiver(homeDeliveryController.getFullName());
                address.setStreet(homeDeliveryController.getAddress());

                deliveryChoice.setHomeDeliveryAddress(address);
                deliveryChoice.setDeliveryMethodId(deliveryMethodId);
            }
        }

        try {
            String subjectOfCareId = userProfileController.getUserProfile().getSubjectOfCareId();

            try {

                SubjectOfCareType loggedInUser = userProfileController.getLoggedInUser();
                String orderer = loggedInUser.getFirstName() + " " + loggedInUser.getLastName(); // // TODO: 2016-06-16 Ska man få beställa om orderer inte är tillgänglig

                response = lmnService.registerMedicalSupplyOrder(
                        subjectOfCareId,
                        userProfileController.isDelegate(),
                        orderer,
                        cart.getItemsInCart(),
                        deliveryChoicePerItem
                );
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                String msg = "Tekniskt fel. Försök senare.";
                FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg,
                        msg));
                return "verifyDelivery";
            }

            if (response.getResultCode().equals(ResultCodeEnum.OK)) {
                orderSuccess = true;

                try {
                    AddMessageResponseType addMessageResponse = mvkInboxService.sendInboxMessage(
                            userProfile.getSubjectOfCareId(), cart.getItemsInCart(), deliveryChoicePerItem.values());

                    if (!addMessageResponse.getResultCode().equals(mvk.crm.casemanagement.inbox._2.ResultCodeEnum.OK)) {
                        String msg = addMessageResponse.getResultText();
                        FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                msg,
                                msg));
                    }
                } catch (MvkInboxServiceException e) {
                    String msg = "Din beställning har utförts men tyvärr kunde inget kvitto skickas till din inkorg.";
                    FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg,
                            msg));
                }

                cart.emptyCart();
                orderController.reset();

            } else if (response.getResultCode().equals(ResultCodeEnum.ERROR)
                    || response.getResultCode().equals(ResultCodeEnum.INFO)) {
                String msg = response.getComment();
                FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg,
                        msg));
                orderSuccess = false;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            String msg = "Tekniskt fel. Försök senare.";
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            return "verifyDelivery";
        }

        return "orderConfirmation";
    }

    public Boolean getOrderSuccess() {
        return orderSuccess;
    }
}
