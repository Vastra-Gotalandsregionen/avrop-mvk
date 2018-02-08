package se._1177.lmn.service;

import org.junit.Before;
import org.junit.Test;
import riv.crm.selfservice.medicalsupply._1.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply._1.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply._1.SubjectOfCareType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._1.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._1.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._1.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._1.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LmnServiceRoutingImplTest {

    private LmnServiceRoutingImpl lmnServiceRouting;
    private GetMedicalSupplyDeliveryPointsResponderInterface msdp10;
    private GetMedicalSupplyPrescriptionsResponderInterface mdp10;
    private RegisterMedicalSupplyOrderResponderInterface rms10;
    private GetMedicalSupplyDeliveryPointsResponderInterface msdp20;
    private GetMedicalSupplyPrescriptionsResponderInterface mdp20;
    private RegisterMedicalSupplyOrderResponderInterface rms20;
    private String key10;
    private String key20;

    @Before
    public void init() {
        SubjectOfCareType subjectOfCareType = new SubjectOfCareType();

        GetMedicalSupplyDeliveryPointsResponseType deliveryPointsResponse = new GetMedicalSupplyDeliveryPointsResponseType();
        deliveryPointsResponse.setResultCode(ResultCodeEnum.OK);

        GetMedicalSupplyPrescriptionsResponseType prescriptionsResponse = new GetMedicalSupplyPrescriptionsResponseType();
        prescriptionsResponse.setResultCode(ResultCodeEnum.OK);
        prescriptionsResponse.setSubjectOfCareType(subjectOfCareType);

        HashMap<String, LmnService> countyCodeToLmnService = new HashMap<>();

        key10 = "10";
        key20 = "20";

        msdp10 = mock(GetMedicalSupplyDeliveryPointsResponderInterface.class);
        mdp10 = mock(GetMedicalSupplyPrescriptionsResponderInterface.class);
        rms10 = mock(RegisterMedicalSupplyOrderResponderInterface.class);

        msdp20 = mock(GetMedicalSupplyDeliveryPointsResponderInterface.class);
        mdp20 = mock(GetMedicalSupplyPrescriptionsResponderInterface.class);
        rms20 = mock(RegisterMedicalSupplyOrderResponderInterface.class);

        when(msdp10.getMedicalSupplyDeliveryPoints(anyString(), any())).thenReturn(
                deliveryPointsResponse);
        when(msdp20.getMedicalSupplyDeliveryPoints(anyString(), any())).thenReturn(
                deliveryPointsResponse);

        when(mdp10.getMedicalSupplyPrescriptions(anyString(), any())).thenReturn(prescriptionsResponse);
        when(mdp20.getMedicalSupplyPrescriptions(anyString(), any())).thenReturn(prescriptionsResponse);

        countyCodeToLmnService.put(key10, new LmnServiceImpl(
                msdp10,
                mdp10,
                rms10,
                "theLogicalAddress" + key10,
                "012-123456" + key10,
                "Kontakta kundtjänst på 012-123456..." + key10,
                "SE21341234-234234234" + key10,
                "Tack för din beställning..." + key10));

        countyCodeToLmnService.put(key20, new LmnServiceImpl(
                msdp20,
                mdp20,
                rms20,
                "theLogicalAddress" + key20,
                "012-123456" + key20,
                "Kontakta kundtjänst på 012-123456..." + key20,
                "SE21341234-234234234" + key20,
                "Tack för din beställning..." + key20));

        lmnServiceRouting = new LmnServiceRoutingImpl(countyCodeToLmnService);
    }

    @Test
    public void getMedicalSupplyPrescriptionsHolderWithoutSettingThreadContext() throws Exception {
        try {
            lmnServiceRouting.getMedicalSupplyPrescriptionsHolder("191212121212");
            fail();
        } catch (Exception e) {
            // OK
        }
    }

    @Test
    public void getMedicalSupplyPrescriptionsHolder() throws Exception {
        // Given
        ThreadLocalStore.setCountyCode("10");

        // When
        lmnServiceRouting.getMedicalSupplyPrescriptionsHolder("191212121212");

        // Then
        verify(mdp10).getMedicalSupplyPrescriptions(anyString(), any());
    }

    @Test
    public void getMedicalSupplyDeliveryPoints() throws Exception {
        // Given
        ThreadLocalStore.setCountyCode("20");

        // When
        lmnServiceRouting.getMedicalSupplyDeliveryPoints(ServicePointProviderEnum.POSTNORD, "12345");

        // Then
        verify(msdp20).getMedicalSupplyDeliveryPoints(anyString(), any());
    }

    @Test
    public void getMedicalSupplyPrescriptions() throws Exception {
        // Given
        ThreadLocalStore.setCountyCode("10");

        // When
        lmnServiceRouting.getMedicalSupplyPrescriptions("191212121212");

        // Then
        verify(mdp10).getMedicalSupplyPrescriptions(anyString(), any());
    }

    @Test
    public void registerMedicalSupplyOrder() throws Exception {
        // Given
        ThreadLocalStore.setCountyCode("20");

        // When
        lmnServiceRouting.registerMedicalSupplyOrder("191212121212", false, "test testsson", new ArrayList<>(), new HashMap<>());

        // Then
        verify(rms20).registerMedicalSupplyOrder(anyString(), any());
    }

    @Test
    public void getDeliveryPointById() throws Exception {
        // Not yet implemented. More complicated to implement.
        lmnServiceRouting.getDeliveryPointById("1");
    }

    @Test
    public void getCustomerServicePhoneNumber() throws Exception {
        // Given
        ThreadLocalStore.setCountyCode("10");

        // When
        String customerServicePhoneNumber = lmnServiceRouting.getCustomerServicePhoneNumber();

        assertTrue(customerServicePhoneNumber.endsWith(key10));
    }

    @Test
    public void getCustomerServiceInfo() throws Exception {
        // Given
        ThreadLocalStore.setCountyCode("20");

        // When
        String customerServiceInfo = lmnServiceRouting.getCustomerServiceInfo();

        assertTrue(customerServiceInfo.endsWith(key20));
    }

    @Test
    public void getReceptionHsaId() throws Exception {
        // Given
        ThreadLocalStore.setCountyCode("10");

        // When
        String receptionHsaId = lmnServiceRouting.getReceptionHsaId();

        assertTrue(receptionHsaId.endsWith(key10));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        // Given
        ThreadLocalStore.setCountyCode("20");

        // When
        String confirmationMessage = lmnServiceRouting.getConfirmationMessage();

        assertTrue(confirmationMessage.endsWith(key20));
    }

}