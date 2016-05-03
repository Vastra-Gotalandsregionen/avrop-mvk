package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.AdressType;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.service.LmnService;
import se.vgregion.mvk.controller.model.Cart;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

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
    private DeliveryController deliveryController;

    @Autowired
    private CollectDeliveryController collectDeliveryController;

    @Autowired
    private HomeDeliveryController homeDeliveryController;

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private Cart cart;

    private Boolean orderSuccess;

    public String confirmOrder() {
        UserProfileType userProfile = userProfileController.getUserProfile().getUserProfile();

        userProfile.getSubjectOfCareId();

        RegisterMedicalSupplyOrderResponseType response;

        HashMap<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem = new HashMap<>();

        for (PrescriptionItemType prescriptionItem : cart.getItemsInCart()) {
            Map<PrescriptionItemType, String> deliveryMethodForEachItem =
                    deliveryController.getDeliveryMethodForEachItem();

            DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();

            DeliveryMethodEnum deliveryMethod = DeliveryMethodEnum.fromValue(deliveryMethodForEachItem.get(
                    prescriptionItem));

            deliveryChoice.setDeliveryMethod(deliveryMethod);

            if (deliveryMethod.equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {

                deliveryChoice.setDeliveryMethodId(lmnService.getDeliveryMethodId(deliveryMethod));

                ServicePointProviderEnum provider = collectDeliveryController
                        .getServicePointProviderForItem(prescriptionItem);

                String deliveryPointId = collectDeliveryController.getDeliveryPointIdsMap().get(provider);
                deliveryChoice.setDeliveryPoint(lmnService.getDeliveryPointById(deliveryPointId));

                String notificationMethodString = collectDeliveryController
                        .getChosenDeliveryNotificationMethod().get(provider);

                DeliveryNotificationMethodEnum notificationMethod = DeliveryNotificationMethodEnum
                        .valueOf(notificationMethodString);

                deliveryChoice.getDeliveryNotificationMethod().add(notificationMethod);

                String notificationReceiver;

                switch (notificationMethod) {
                    case BREV:
                        notificationReceiver = null;
                        break;
                    case E_POST:
                        notificationReceiver = collectDeliveryController.getEmail();
                        break;
                    case SMS:
                        notificationReceiver = collectDeliveryController.getSmsNumber();
                        break;
                    default:
                        throw new RuntimeException("Unexpected notificationMethod: " + notificationMethod);
                }

                deliveryChoice.setDeliveryNotificationReceiver(notificationReceiver);
            } else {
                AdressType address = new AdressType();
                address.setCareOfAddress(""); // // TODO: 2016-05-02
                address.setCity(userProfile.getCity());
                address.setDoorCode(homeDeliveryController.getDoorCode());
                address.setPhone(userProfile.getPhoneNumber());
                address.setPostalCode(userProfile.getZip());
                address.setReciever(userProfile.getFirstName() + " " + userProfile.getLastName()); // todo Korrekt att detta är mottagarens namn?
                address.setStreet(userProfile.getStreetAddress());

                deliveryChoice.setHomeDeliveryAdress(address);
            }
        }

        response = lmnService.registerMedicalSupplyOrder(
                userProfile.getSubjectOfCareId(),
                false, // todo Implementera delegat!
                userProfile.getFirstName()
                        + " " + userProfile.getLastName(),
                cart.getItemsInCart(),
                deliveryChoicePerItem
        );

        if (response.getResultCode().equals(ResultCodeEnum.OK)) {
            orderSuccess = true;

            cart.emptyCart();

            ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).invalidate(); // todo Verkligen?
        } else if (response.getResultCode().equals(ResultCodeEnum.ERROR)) {
            String msg = "Tekniskt fel. Försök senare.";
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            orderSuccess = false;
        } else if (response.getResultCode().equals(ResultCodeEnum.INFO)) {
            String msg = response.getComment();
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            orderSuccess = false;
        }

        return "orderConfirmation" + ACTION_SUFFIX;
    }
}
