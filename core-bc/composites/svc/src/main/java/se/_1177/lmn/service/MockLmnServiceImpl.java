package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._0.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

/**
 * @author Patrik Björk
 */
public class MockLmnServiceImpl implements LmnService {

    private GetMedicalSupplyDeliveryPointsResponderInterface deliveryPointsService;


    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder() {
        return null;
    }

    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints() {
        return null;
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions() {
        return null;
    }

    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder() {
        return null;
    }
}
