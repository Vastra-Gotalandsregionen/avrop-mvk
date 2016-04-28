package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

import java.util.List;

/**
 * @author Patrik Björk
 */
public interface LmnService {
    MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId);

    GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(String postalCode);

    GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId);

    RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrderCollectDelivery(
            DeliveryPointType deliveryPoint,
            DeliveryNotificationMethodEnum deliveryNotificationMethod,
            String subjectOfCareId,
            boolean orderByDelegate,
            String orderer, // May be delegate
            List<String> articleNumbers);

    RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrderHomeDelivery(
            String receiverFullName,
            String phone,
            String postalCode,
            String street,
            String doorCode,
            String city,
            String careOfAddress,
            String subjectOfCareId,
            boolean orderByDelegate,
            String orderer, // May be delegate
            List<String> articleNumbers//,
    );
}
