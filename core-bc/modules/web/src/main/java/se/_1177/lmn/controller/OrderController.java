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
import se._1177.lmn.controller.session.OrderControllerSession;
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
import static se._1177.lmn.service.util.Constants.PRODUCTS_FETCH_DEFAULT_ERROR;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderController {

    public static final String VIEW_NAME = "Mina förskrivna förbrukningsprodukter";

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

    @Autowired
    private MessageController messageController;

    @Autowired
    private NavigationController navigationController;

    @Autowired
    private OrderControllerSession sessionData;

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

            setMedicalSupplyPrescriptions(
                    lmnService.getMedicalSupplyPrescriptionsHolder(
                            userProfileController.getUserProfile().getSubjectOfCareId()
                    )
            );

            this.prescriptionItemInfo.setLatestOrderItemsByArticleNo(
                    getMedicalSupplyPrescriptionsHolder().getLatestOrderedNumbersByArticleNo());

            GetMedicalSupplyPrescriptionsResponseType supplyPrescriptionsResponse =
                    getMedicalSupplyPrescriptionsHolder().getSupplyPrescriptionsResponse();

            if (!supplyPrescriptionsResponse.getResultCode().equals(ResultCodeEnum.OK)) {
                String msg = supplyPrescriptionsResponse.getComment();

                utilController.addErrorMessageWithCustomerServiceInfo(msg);

                return;
            }

            Set<ServicePointProviderEnum> allRelevantProviders = new HashSet<>();
            for (PrescriptionItemType prescriptionItem : getMedicalSupplyPrescriptionsHolder().orderable) {
                String prescriptionItemId = prescriptionItem.getPrescriptionItemId();
                getPrescriptionItemInfosToPresent().put(prescriptionItemId, prescriptionItem);

                if (!UtilController.isAfterToday(prescriptionItem.getNextEarliestOrderDate())
                        && prescriptionItem.getArticle().isIsOrderable()) {

                    getChosenItemMap().put(prescriptionItemId, lmnService.getDefaultSelectedPrescriptions());

                    prescriptionItem.getDeliveryAlternative().forEach(alternative -> {
                        if (!alternative.getServicePointProvider().equals(ServicePointProviderEnum.INGEN)
                                && alternative.isAllowChioceOfDeliveryPoints()) {
                            allRelevantProviders.add(alternative.getServicePointProvider());
                        }
                    });
                }
            }

            if (userProfileController.getUserProfile() != null) {

                collectDeliveryController.cacheDeliveryPointsForRelevantSuppliersInBackground(
                        userProfileController.getUserProfile().getZip(),
                        allRelevantProviders,
                        userProfileController.getUserProfile().getCountyCode());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            String msg = messageController.getMessage(PRODUCTS_FETCH_DEFAULT_ERROR);

            utilController.addErrorMessageWithCustomerServiceInfo(msg);
        } finally {
            String delegateUrlParameters = userProfileController.getDelegateUrlParameters();

            String ampOrQuestionMark = delegateUrlParameters != null && delegateUrlParameters.length() > 0 ? "&amp;" : "?";

            String result = "order" + delegateUrlParameters
                    + ampOrQuestionMark
                    + "faces-redirect=true&amp;includeViewParams=true";

            navigationController.init(result, VIEW_NAME);
        }
    }

    private Map<String, PrescriptionItemType> getPrescriptionItemInfosToPresent() {
        return sessionData.getPrescriptionItemInfosToPresent();
    }

    private void setMedicalSupplyPrescriptions(MedicalSupplyPrescriptionsHolder medicalSupplyPrescriptionsHolder) {
        sessionData.setMedicalSupplyPrescriptions(medicalSupplyPrescriptionsHolder);
    }

    private MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder() {
        return sessionData.getMedicalSupplyPrescriptions();
    }

    public synchronized void possiblyReinit() {
        if (getMedicalSupplyPrescriptionsHolder() == null) {
            init();
        }
    }

    public void reset() {
        setMedicalSupplyPrescriptions(null);
        setChosenItemMap(new HashMap<>());
    }

    public String reinit() {
        ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession().invalidate();

        return "order" + ACTION_SUFFIX;
    }

    public List<PrescriptionItemType> getMedicalSupplyPrescriptions() {
        if (getMedicalSupplyPrescriptionsHolder() != null) {
            return getMedicalSupplyPrescriptionsHolder().orderable;
        } else {
            return null;
        }
    }

    public List<PrescriptionItemType> getNoLongerOrderableMedicalSupplyPrescriptions() {
        if (getMedicalSupplyPrescriptionsHolder() != null) {
            return getMedicalSupplyPrescriptionsHolder().noLongerOrderable;
        } else {
            return null;
        }
    }

    public void setChosenItemMap(Map<String, Boolean> chosenItemMap) {
        sessionData.setChosenItemMap(chosenItemMap);
    }

    public Map<String, Boolean> getChosenItemMap() {
        return sessionData.getChosenItemMap();
    }

    public String toDelivery() {

        List<OrderRowType> toCart = new ArrayList<>();

        boolean anyArticleWithSubArticles = false;

        prescriptionItemInfo.getChosenPrescriptionItemInfo().clear();

        for (Map.Entry<String, Boolean> entry : getChosenItemMap().entrySet()) {
            if (entry.getValue()) {
                PrescriptionItemType prescriptionItem = getPrescriptionItemInfosToPresent().get(entry.getKey());

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

            return navigationController.gotoView("subArticle" + ACTION_SUFFIX, SubArticleController.VIEW_NAME);
        } else {
            prepareDeliveryOptions(prescriptionItemInfo.getChosenPrescriptionItemInfoList());

            return navigationController.gotoView("delivery" + ACTION_SUFFIX, DeliveryController.VIEW_NAME);
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

    public String getViewName() {
        return VIEW_NAME;
    }
}
