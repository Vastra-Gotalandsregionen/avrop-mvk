package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import se._1177.lmn.service.LmnServiceFacade;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MvkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MvkController.class);

    @Autowired
    LmnServiceFacade lmnServiceFacade;
    private GetMedicalSupplyPrescriptionsResponseType medicalSupplyPrescriptions;

    public MvkController() {
        try {
            medicalSupplyPrescriptions = lmnServiceFacade.getMedicalSupplyPrescriptions();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            String msg = "Internt kommunikationsfel";

            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions() {

        return medicalSupplyPrescriptions;
    }

}
