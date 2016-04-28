package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._0.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

import java.util.List;

/**
 * @author Patrik Björk
 */
public class MockLmnServiceImpl implements LmnService {

    private GetMedicalSupplyDeliveryPointsResponderInterface deliveryPointsService;


    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        return null;
    }

    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(String postalCode) {
        return null;
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId) {
        return null;
    }

    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrderCollectDelivery(DeliveryPointType deliveryPoint, DeliveryNotificationMethodEnum deliveryNotificationMethod, String subjectOfCareId, boolean orderByDelegate, String orderer, List<String> articleNumbers) {
        return null;
    }

    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrderHomeDelivery(String receiverFullName, String phone, String postalCode, String street, String doorCode, String city, String careOfAddress, String subjectOfCareId, boolean orderByDelegate, String orderer, List<String> articleNumbers) {
        return null;
    }

}
