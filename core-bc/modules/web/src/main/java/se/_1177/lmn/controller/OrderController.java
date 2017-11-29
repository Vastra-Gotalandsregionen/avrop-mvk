package se._1177.lmn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._1.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply._1.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;
import se._1177.lmn.service.LmnService;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static se._1177.lmn.service.util.CartUtil.createOrderRow;
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

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

    @Autowired
    private SubArticleController subArticleController;

    private MedicalSupplyPrescriptionsHolder medicalSupplyPrescriptions;

    private Map<String, Boolean> chosenItemMap = new HashMap<>();

    private Map<String, PrescriptionItemType> prescriptionItemInfosToPresent = new HashMap<>();

    /**
     * This is called by UserProfileController. It fetches the {@link PrescriptionItemType}s, preserves them in a map
     * where the id is mapped to the instance, and also triggers loading of
     * {@link riv.crm.selfservice.medicalsupply._1.DeliveryPointType}s for all {@link ServicePointProviderEnum}s that
     * are relevant, i.e. they are available for at least one orderable {@link PrescriptionItemType}.
     */
    public void init() {
        try {
            if (userProfileController.getUserProfile() == null) {
                // We don't need to add a message here since a message should already be added to inform the user.
                return;
            }

            this.medicalSupplyPrescriptions = lmnService.getMedicalSupplyPrescriptionsHolder(
                    userProfileController.getUserProfile().getSubjectOfCareId());

            this.prescriptionItemInfo.setLatestOrderItemsByArticleNo(
                    this.medicalSupplyPrescriptions.getLatestOrderedNumbersByArticleNo());

            GetMedicalSupplyPrescriptionsResponseType supplyPrescriptionsResponse =
                    this.medicalSupplyPrescriptions.getSupplyPrescriptionsResponse();

            if (!supplyPrescriptionsResponse.getResultCode().equals(ResultCodeEnum.OK)) {
                String msg = supplyPrescriptionsResponse.getComment();

                utilController.addErrorMessageWithCustomerServiceInfo(msg);

                return;
            }

            Set<ServicePointProviderEnum> allRelevantProviders = new HashSet<>();
            for (PrescriptionItemType prescriptionItem : medicalSupplyPrescriptions.orderable) {
                String prescriptionItemId = prescriptionItem.getPrescriptionItemId();
                prescriptionItemInfosToPresent.put(prescriptionItemId, prescriptionItem);

                if (!UtilController.isAfterToday(prescriptionItem.getNextEarliestOrderDate())
                        && prescriptionItem.getArticle().isIsOrderable()) {
                    chosenItemMap.put(prescriptionItemId, true);

                    prescriptionItem.getDeliveryAlternative().forEach(alternative -> {
                        if (!alternative.getServicePointProvider().equals(ServicePointProviderEnum.INGEN)
                                && alternative.isAllowChioceOfDeliveryPoints()) {
                            allRelevantProviders.add(alternative.getServicePointProvider());
                        }
                    });
                }
            }

            if (userProfileController.getUserProfile() != null) {

                collectDeliveryController.loadDeliveryPointsForRelevantSuppliersInBackground(
                        userProfileController.getUserProfile().getZip(),
                        allRelevantProviders,
                        userProfileController.getUserProfile().getCountyCode());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            String msg = "Dina förskrivna produkter kunde inte visas.";

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

        List<OrderRowType> toCart = new ArrayList<>();

        boolean anyArticleWithSubArticles = false;

        prescriptionItemInfo.getChosenPrescriptionItemInfo().clear();

        for (Map.Entry<String, Boolean> entry : chosenItemMap.entrySet()) {
            if (entry.getValue()) {
                PrescriptionItemType prescriptionItem = prescriptionItemInfosToPresent.get(entry.getKey());

                prescriptionItemInfo.getChosenPrescriptionItemInfo()
                        .put(prescriptionItem.getPrescriptionItemId(), prescriptionItem);

                if (prescriptionItem.getSubArticle() != null && prescriptionItem.getSubArticle().size() > 0) {
                    anyArticleWithSubArticles = true;
                }

                Optional<OrderRowType> orderRow = createOrderRow(prescriptionItem);

                // We can only add orderRows for those not having sub-articles for now. Sub-articles will be added in
                // the next step if such are present.
                orderRow.ifPresent(toCart::add);
            }
        }

        cart.setOrderRows(toCart);

        if (prescriptionItemInfo.getChosenPrescriptionItemInfo().size() == 0) {
            String msg = "Du har inte valt någon produkt. Välj minst en för att fortsätta.";
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg));

            return "order" + userProfileController.getDelegateUrlParameters();
        } else if (anyArticleWithSubArticles) {
            prepareDeliveryOptions(prescriptionItemInfo.getChosenPrescriptionItemInfoList());

            subArticleController.init();

            return "subArticle" + ACTION_SUFFIX;
        } else {
            prepareDeliveryOptions(prescriptionItemInfo.getChosenPrescriptionItemInfoList());

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
