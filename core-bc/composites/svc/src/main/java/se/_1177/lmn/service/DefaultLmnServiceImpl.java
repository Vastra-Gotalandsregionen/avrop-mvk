package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply._1.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._1.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._1.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._1.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._1.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

import java.util.List;
import java.util.Map;

public class DefaultLmnServiceImpl implements LmnService {
    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        throw new DefaultLmnServiceException();
    }

    @Override
    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(ServicePointProviderEnum provider, String postalCode) {
        throw new DefaultLmnServiceException();
    }

    @Override
    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId) {
        throw new DefaultLmnServiceException();
    }

    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder(String subjectOfCareId, boolean orderByDelegate, String orderer, List<OrderRowType> orderRows, Map<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem) {
        throw new DefaultLmnServiceException();
    }

    @Override
    public DeliveryPointType getDeliveryPointById(String deliveryPointId) {
        throw new DefaultLmnServiceException();
    }

    @Override
    public String getReceptionHsaId() {
        throw new DefaultLmnServiceException();
    }

    @Override
    public String getLogicalAddress() {
        throw new DefaultLmnServiceException();
    }

    @Override
    public boolean getDefaultSelectedPrescriptions() {
        throw new DefaultLmnServiceException();
    }
}
