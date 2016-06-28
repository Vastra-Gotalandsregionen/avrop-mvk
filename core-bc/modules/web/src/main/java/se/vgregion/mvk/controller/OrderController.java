package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;
import se._1177.lmn.service.LmnService;
import se.vgregion.mvk.controller.model.Cart;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private CollectDeliveryController collectDeliveryController;

    @Autowired
    private DeliveryController deliveryController;

    @Autowired
    private LmnService lmnService;

    @Autowired
    private UtilController utilController;

    @Autowired
    private Cart cart;

    private MedicalSupplyPrescriptionsHolder medicalSupplyPrescriptions;

    private Map<String, Boolean> chosenItemMap = new HashMap<>();

    // This is inited by UserProfileController.
    public void init() {
        try {
            if (userProfileController.getUserProfile() == null) {
                // We don't need to add a message here since a message should already be added to inform the user.
                return;
            }

            this.medicalSupplyPrescriptions = lmnService.getMedicalSupplyPrescriptionsHolder(
                    userProfileController.getUserProfile().getSubjectOfCareId());

            GetMedicalSupplyPrescriptionsResponseType supplyPrescriptionsResponse =
                    this.medicalSupplyPrescriptions.getSupplyPrescriptionsResponse();

            if (!supplyPrescriptionsResponse.getResultCode().equals(ResultCodeEnum.OK)) {
                String msg = supplyPrescriptionsResponse.getComment();

                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));

                return;
            }

            for (PrescriptionItemType prescriptionItem : medicalSupplyPrescriptions.orderable) {
                String prescriptionItemId = prescriptionItem.getPrescriptionItemId();
                cart.addPrescriptionItemForInfo(prescriptionItemId, prescriptionItem);

                if (!UtilController.isAfterToday(prescriptionItem.getNextEarliestOrderDate())
                        && prescriptionItem.getArticle().isIsOrderable()) {
                    chosenItemMap.put(prescriptionItemId, true);
                }
            }

            if (userProfileController.getUserProfile() != null) {
                collectDeliveryController.loadDeliveryPointsForAllSuppliersInBackground(
                        userProfileController.getUserProfile().getZip());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            String msg = "Dina produkter kunde inte hämtas. Försök senare eller kontakta kundtjänst.";

            utilController.addErrorMessageWithCustomerServiceInfo(msg);
        }
    }

    public synchronized void possiblyReinit() {
        if (medicalSupplyPrescriptions == null) {
            init();
        }
    }

    public void reset() {
        medicalSupplyPrescriptions = null;
        chosenItemMap = new HashMap<>();
    }

    public String reinit() {
        ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession().invalidate();

        return "order" + ACTION_SUFFIX;
    }

    public List<PrescriptionItemType> getMedicalSupplyPrescriptions() {
        if (medicalSupplyPrescriptions != null) {
            return medicalSupplyPrescriptions.orderable;
        } else {
            return null;
        }
    }

    public List<PrescriptionItemType> getNoLongerOrderableMedicalSupplyPrescriptions() {
        if (medicalSupplyPrescriptions != null) {
            return medicalSupplyPrescriptions.noLongerOrderable;
        } else {
            return null;
        }
    }

    public Map<String, Boolean> getChosenItemMap() {
        return chosenItemMap;
    }

    public String toDelivery() {

        List<PrescriptionItemType> toCart = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : chosenItemMap.entrySet()) {
            if (entry.getValue()) {
                toCart.add(cart.getPrescriptionItemInfo().get(entry.getKey()));
            }
        }

        cart.setItemsInCart(toCart);

        if (cart.getItemsInCart().size() == 0) {
            String msg = "Du har inte valt någon produkt. Välj minst en för att fortsätta.";
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg));

            return "order" + userProfileController.getDelegateUrlParameters();
        } else {
            prepareDeliveryOptions(toCart);

            return "delivery" + ACTION_SUFFIX;
        }

    }

    void prepareDeliveryOptions(final List<PrescriptionItemType> chosenPrescriptionItems) {

        final Set<DeliveryMethodEnum>               remainingDeliveryMethods =
                new HashSet<>(Arrays.asList(DeliveryMethodEnum.values()));

        for (PrescriptionItemType prescriptionItem : chosenPrescriptionItems) {

            // Find out which deliveryNotificationMethod(s) that are available for all items.
            // Also find out which ServicePointProvider that are available for all items.

            Set<DeliveryMethodEnum> deliveryMethodsForItem = new HashSet<>();

            for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {
                deliveryMethodsForItem.add(deliveryAlternative.getDeliveryMethod());
            }

            remainingDeliveryMethods.retainAll(deliveryMethodsForItem);

        }

        deliveryController.setPossibleDeliveryMethodsFittingAllItems(remainingDeliveryMethods);
    }

}
