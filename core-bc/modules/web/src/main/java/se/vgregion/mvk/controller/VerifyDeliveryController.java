package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.service.LmnService;
import se.vgregion.mvk.controller.model.Cart;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

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

        if (deliveryController.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.value())) {

            response = lmnService.registerMedicalSupplyOrderCollectDelivery(
                    collectDeliveryController.getDeliveryPointsMap().get(collectDeliveryController.getChosenDeliveryPoint()),
                    collectDeliveryController.getDeliveryNotificationMethod(),
                    userProfile.getSubjectOfCareId(),
                    false, // todo Implementera delegat!
                    userProfile.getFirstName()
                            + " " + userProfile.getLastName(),
                    cart.getItemsInCart()
            );

        } else if (deliveryController.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS.value())) {
            response = lmnService.registerMedicalSupplyOrderHomeDelivery(
                    "Ombud eller subjekt?",
                    userProfile.getPhoneNumber(),
                    userProfile.getZip(),
                    userProfile.getStreetAddress(),
                    homeDeliveryController.getDoorCode(),
                    userProfile.getCity(),
                    "", // // TODO: 2016-04-25
                    userProfile.getSubjectOfCareId(),
                    false, // // TODO: 2016-04-25 Ta reda på hur jag vet detta.
                    userProfile.getFirstName() + " " + userProfile.getLastName(),
                    cart.getItemsInCart()
            );
        } else {
            throw new IllegalStateException("Delivery method must be set.");
        }

        if (response.getResultCode().equals(ResultCodeEnum.OK)) {
            orderSuccess = true;

            cart.emptyCart();
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
