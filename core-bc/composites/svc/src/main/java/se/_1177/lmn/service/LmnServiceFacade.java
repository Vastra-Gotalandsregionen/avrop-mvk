package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

/**
 * @author Patrik Björk
 */
public interface LmnServiceFacade {
    MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder();

    GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints();

    GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions();

    RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder();
}
