package se.vgregion.mvk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import se._1177.lmn.service.LmnServiceFacade;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MvkController {

    @Autowired
    LmnServiceFacade lmnServiceFacade;

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions() {
        GetMedicalSupplyPrescriptionsResponseType medicalSupplyPrescriptions =
                lmnServiceFacade.getMedicalSupplyPrescriptions();

        return medicalSupplyPrescriptions;
    }
}
