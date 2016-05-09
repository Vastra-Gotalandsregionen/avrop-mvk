package se.vgregion.mvk.controller;

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
    private MvkInboxService mvkInboxService;

    @Autowired
    private Cart cart;

    private Boolean orderSuccess;

    public String confirmOrder() {
        UserProfileType userProfile = userProfileController.getUserProfile().getUserProfile();

        userProfile.getSubjectOfCareId();

        RegisterMedicalSupplyOrderResponseType response;

        HashMap<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem = new HashMap<>();

        Map<PrescriptionItemType, String> deliveryMethodForEachItem =
                deliveryController.getDeliveryMethodForEachItem();

        for (PrescriptionItemType prescriptionItem : cart.getItemsInCart()) {

            DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();

            DeliveryMethodEnum deliveryMethod = DeliveryMethodEnum.fromValue(deliveryMethodForEachItem.get(
                    prescriptionItem));

            deliveryChoice.setDeliveryMethod(deliveryMethod);

            if (deliveryMethod.equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {

                String deliveryMethodId = null;

                // Take the first deliveryAlternative with matching deliveryMethod. This assumes no two
                // deliveryAlternatives share the same deliveryMethod. That would lead to arbitrary result.
                for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {
                    if (deliveryAlternative.getDeliveryMethod().equals(deliveryMethod)) {
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

                deliveryChoice.setDeliveryNotificationMethod(notificationMethod);

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
                AddressType address = new AddressType();
                address.setCareOfAddress(""); // // TODO: 2016-05-02
                address.setCity(userProfile.getCity());
                address.setDoorCode(homeDeliveryController.getDoorCode());
                address.setPhone(userProfile.getPhoneNumber());
                address.setPostalCode(userProfile.getZip());
                address.setReceiver(userProfile.getFirstName() + " " + userProfile.getLastName()); // todo Korrekt att detta är mottagarens namn?
                address.setStreet(userProfile.getStreetAddress());

                deliveryChoice.setHomeDeliveryAddress(address);
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
