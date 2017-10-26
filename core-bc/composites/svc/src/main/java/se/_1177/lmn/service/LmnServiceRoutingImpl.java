package se._1177.lmn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.OrderRowType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;

import java.util.List;
import java.util.Map;

public class LmnServiceRoutingImpl implements LmnService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LmnServiceRoutingImpl.class);

    private final Map<String, LmnService> countyCodeToLmnService;
    private final String DEFAULT_COUNTY_CODE = "default";

    public LmnServiceRoutingImpl(
            Map<String, LmnService> countyCodeToLmnService) {
        this.countyCodeToLmnService = countyCodeToLmnService;
    }

    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        return getContextLmnService().getMedicalSupplyPrescriptionsHolder(subjectOfCareId);
    }

    @Override
    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(ServicePointProviderEnum provider,
                                                                                     String postalCode) {
        return getContextLmnService().getMedicalSupplyDeliveryPoints(provider, postalCode);
    }

    @Override
    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId) {
        return getContextLmnService().getMedicalSupplyPrescriptions(subjectOfCareId);
    }

    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder(
            String subjectOfCareId, boolean orderByDelegate, String orderer, List<OrderRowType> orderRows,
            Map<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem) {

        return getContextLmnService().registerMedicalSupplyOrder(subjectOfCareId, orderByDelegate, orderer, orderRows,
                deliveryChoicePerItem);
    }

    @Override
    public DeliveryPointType getDeliveryPointById(String deliveryPointId) {
        return getContextLmnService().getDeliveryPointById(deliveryPointId);
    }

    @Override
    public String getCustomerServicePhoneNumber() {
        return getContextLmnService().getCustomerServicePhoneNumber();
    }

    @Override
    public String getCustomerServiceInfo() {
        return getContextLmnService().getCustomerServiceInfo();
    }

    @Override
    public String getReceptionHsaId() {
        return getContextLmnService().getReceptionHsaId();
    }

    @Override
    public String getConfirmationMessage() {
        return getContextLmnService().getConfirmationMessage();
    }

    /**
     * This method's only purpose is to delay the external web service from going into "sleep mode" where it becomes
     * slower than preferred. We allow it to go into sleep mode when time is 3.XX in the night only. This method is
     * executed by schedule.
     */
    // Every fifteen minutes all the time except when time is 3-something in the night.
    @Scheduled(cron = "0 0/15 0-2,4-23 * * ?")
    public void keepWebServiceAwake() {
        // This delays the external web service going into "sleep mode" where it becomes slower than preferred. We allow
        // it to go into sleep mode when time is 3.XX in the night only.
        try {
            LOGGER.info("Scheduled operation...");
            ThreadLocalStore.setCountyCode("14");
            getMedicalSupplyDeliveryPoints(ServicePointProviderEnum.POSTNORD, "41648");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            ThreadLocalStore.setCountyCode(null);
        }
    }

    private LmnService getContextLmnService() {
        String countyCode = ThreadLocalStore.getCountyCode();

        if (countyCode == null) {
            countyCode = DEFAULT_COUNTY_CODE;
        }

        LmnService lmnService = this.countyCodeToLmnService.get(countyCode);

        if (lmnService == null) {
            // We expect the configuration to contain a "default" county.
            lmnService = this.countyCodeToLmnService.get(DEFAULT_COUNTY_CODE);

            // Last resort. We don't want exceptions when the customer service properties are called from the views.
            // That would cause a blank page.
            if (lmnService == null) {
                lmnService = new LmnServiceImpl(null, null, null, null, "Tekniskt fel.", "Tekniskt fel.", "", "");
            }
        }

        return lmnService;
    }
}
