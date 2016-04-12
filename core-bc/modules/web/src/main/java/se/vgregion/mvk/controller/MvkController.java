package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import se._1177.lmn.service.LmnServiceFacade;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;
import se.vgregion.mvk.controller.model.Cart;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MvkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MvkController.class);

    @Autowired
    private LmnServiceFacade lmnServiceFacade;

    @Autowired
    private Cart cart;

    private MedicalSupplyPrescriptionsHolder medicalSupplyPrescriptions;

    private Map<String, Boolean> chosenItemMap = new HashMap<>();

    @PostConstruct
    public void  init() {
        try {
            this.medicalSupplyPrescriptions = lmnServiceFacade.getMedicalSupplyPrescriptionsHolder();

            for (PrescriptionItemType prescriptionItem : medicalSupplyPrescriptions.orderable) {
                String prescriptionId = prescriptionItem.getPrescriptionId();
                chosenItemMap.put(prescriptionId, cart.getItemsInCart().contains(prescriptionId));
            }
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

        return "delivery?faces-redirect=true&amp;includeViewParams=true";
    }
}
