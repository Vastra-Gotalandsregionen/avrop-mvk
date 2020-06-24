package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply._2.CVType;
import riv.crm.selfservice.medicalsupply._2.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._2.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._2.OrderRowType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._2.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._2.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._2.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

import java.util.List;
import java.util.Map;

public class DefaultLmnServiceImpl implements LmnService {
    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        throw new DefaultLmnServiceException();
    }

    @Override
    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(CVType provider, String postalCode) {
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
