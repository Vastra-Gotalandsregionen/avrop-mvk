package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._0.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

import java.util.List;
import java.util.Map;

/**
 * @author Patrik Björk
 */
public class MockLmnServiceImpl implements LmnService {

    private GetMedicalSupplyDeliveryPointsResponderInterface deliveryPointsService;


    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        return null;
    }

    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(ServicePointProviderEnum provider, String postalCode) {
        return null;
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId) {
        return null;
    }

    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder(String subjectOfCareId, boolean orderByDelegate, String orderer, List<PrescriptionItemType> prescriptionItems, Map<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem) {
        return null;
    }

    @Override
    public DeliveryPointType getDeliveryPointById(String deliveryPointId) {
        return null;
    }

}
