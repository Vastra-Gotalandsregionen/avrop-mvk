package se._1177.lmn.controller;

import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageResponseType;
import mvk.itintegration.userprofile._2.SubjectOfCareType;
import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._1.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._1.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.service.MvkInboxService;
import se._1177.lmn.service.MvkInboxServiceException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.HashMap;

/**
 * This class corresponds to the verifyDelivery view. The main concern is to confirm/register the order.
 *
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
    private HomeDeliveryController homeDeliveryController;

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private UtilController utilController;

    @Autowired
    private MvkInboxService mvkInboxService;

    @Autowired
    private Cart cart;

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

    private Boolean orderSuccess;

    /**
     * This method's main purpose is to register the order and send an inbox message if successful.
     *
     * @return the action outcome
     */
    public String confirmOrder() {
        UserProfileType userProfile = userProfileController.getUserProfile();

        RegisterMedicalSupplyOrderResponseType response;

        HashMap<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem = new HashMap<>();

        try {
            String subjectOfCareId = userProfileController.getUserProfile().getSubjectOfCareId();

            try {

                SubjectOfCareType loggedInUser = userProfileController.getLoggedInUser();
                String orderer = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();

                // Register the order
                response = lmnService.registerMedicalSupplyOrder(
                        subjectOfCareId,
                        userProfileController.isDelegate(),
                        orderer,
                        cart.getOrderRows(),
                        deliveryChoicePerItem
                );
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);

                String msg = "Tekniskt fel. Försök senare. Systemet fick inte någon bekräftelse från bakomliggande " +
                        "system och kan därför inte veta om beställningen har gått igenom. Om dina produkter inte " +
                        "längre är beställningsbara har beställningen troligen gått igenom.";

                utilController.addErrorMessageWithCustomerServiceInfo(msg);

                // We don't know whether the order actually got registered. Even though an exception occurred it may
                // have been registered. To play safe we reset the fetched prescriptions if the numbers left to order
                // has changed.
                resetCartAndRelated();

                return "verifyDelivery";
            }

            // Handle result
            if (response.getResultCode().equals(ResultCodeEnum.OK)) {
                orderSuccess = true;

                try {
                    AddMessageResponseType addMessageResponse = mvkInboxService.sendInboxMessage(
                            userProfile.getSubjectOfCareId(), cart.getOrderRows(), lmnService.getReceptionHsaId());

                    if (!addMessageResponse.getResultCode().equals(mvk.crm.casemanagement.inbox._2.ResultCodeEnum.OK)) {
                        String msg = addMessageResponse.getResultText();
                        FacesContext.getCurrentInstance().addMessage("", new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                msg,
                                msg));
                    }
                } catch (MvkInboxServiceException e) {
                    String msg = "Din beställning har utförts men tyvärr kunde inget kvitto skickas till din inkorg.";
                    utilController.addErrorMessageWithCustomerServiceInfo(msg);
                }

                resetCartAndRelated();

            } else if (response.getResultCode().equals(ResultCodeEnum.ERROR)
                    || response.getResultCode().equals(ResultCodeEnum.INFO)) {
                String msg = response.getComment();

                utilController.addErrorMessageWithCustomerServiceInfo("Ett tekniskt fel inträffade när din " +
                        "beställning skulle bekräftas. Beställningen kommer inte att kunna genomföras eller sparas. " +
                        "Försök senare eller kontakta kundtjänst.");
                utilController.addErrorMessageWithCustomerServiceInfo(msg);

                orderSuccess = false;

                resetCartAndRelated();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            String msg = "Tekniskt fel. Försök senare.";
            utilController.addErrorMessageWithCustomerServiceInfo(msg);

            return "verifyDelivery";
        }

        return "orderConfirmation";
    }

    private void resetCartAndRelated() {
        cart.emptyCart();
        prescriptionItemInfo.emptyChosenPrescriptionItems();
        orderController.reset();
        homeDeliveryController.resetChoices();
    }

    public Boolean getOrderSuccess() {
        return orderSuccess;
    }
}
