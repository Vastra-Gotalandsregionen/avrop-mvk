package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;
import se.vgregion.mvk.controller.model.Cart;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private CollectDeliveryController collectDeliveryController;

    @Autowired
    private LmnService lmnService;

    @Autowired
    private Cart cart;

    private MedicalSupplyPrescriptionsHolder medicalSupplyPrescriptions;

    private Map<String, Boolean> chosenItemMap = new HashMap<>();

    private ExecutorService executorService;

    public ExecutorService getExecutor() {
        if (executorService != null) {
            return executorService;
        }
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();

        threadFactory.setDaemon(true);
        threadFactory.setThreadGroupName("backgroundTasksGroup");
        threadFactory.setThreadNamePrefix("backgroundTask");

        executorService = Executors.newCachedThreadPool(threadFactory);

        return executorService;
    }

    @PostConstruct
    public void  init() {
        try {
            this.medicalSupplyPrescriptions = lmnService.getMedicalSupplyPrescriptionsHolder(
                    userProfileController.getUserProfile().getUserProfile().getSubjectOfCareId());

            for (PrescriptionItemType prescriptionItem : medicalSupplyPrescriptions.orderable) {
                String prescriptionId = prescriptionItem.getPrescriptionId();
                chosenItemMap.put(prescriptionId, cart.getItemsInCart().contains(prescriptionId));
                cart.addPrescriptionItemForInfo(prescriptionId, prescriptionItem);
            }

            getExecutor().submit(() -> {
                // Just calling any method will init the bean. We do this to load the delivery points.
                collectDeliveryController.dummyMethod();
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            String msg = "Internt kommunikationsfel. Dina produkter kunde inte hämtas. Försök senare.";

            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }

    public List<PrescriptionItemType> getMedicalSupplyPrescriptions() {
        return medicalSupplyPrescriptions.orderable;
    }

    public List<PrescriptionItemType> getNoLongerOrderableMedicalSupplyPrescriptions() {
        return medicalSupplyPrescriptions.noLongerOrderable;
    }

    public Map<String, Boolean> getChosenItemMap() {
        return chosenItemMap;
    }

    public String toDelivery() {

        List<String> toCart = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : chosenItemMap.entrySet()) {
            if (entry.getValue()) {
                toCart.add(entry.getKey());
            }
        }

        cart.setItemsInCart(toCart);

        if (cart.getItemsInCart().size() == 0) {
            String msg = "Du har inte valt någon produkt. Välj minst en för att fortsätta.";
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg));

            return "order";
        } else {
            return "delivery" + ACTION_SUFFIX;
        }

    }
}
