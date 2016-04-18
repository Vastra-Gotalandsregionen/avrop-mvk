package se._1177.lmn.service;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._0.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._0.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._0.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderType;
import se._1177.lmn.service.mock.MockWebServiceServer;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author Patrik Björk
 */
public class LmnServiceImplTest {

    private GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoints;
    private GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions;
    private RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder;

    @BeforeClass
    public static void setupMockServer() {
        MockWebServiceServer.publishEndpoints(18081);
    }

    @AfterClass
    public static void stopMockServer() {
        MockWebServiceServer.shutdown();
    }

    @Before
    public void setup() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("webservice-context-test.xml");

        medicalSupplyDeliveryPoints = ctx.getBean(GetMedicalSupplyDeliveryPointsResponderInterface.class);
        medicalSupplyPrescriptions = ctx.getBean(GetMedicalSupplyPrescriptionsResponderInterface.class);
        registerMedicalSupplyOrder = ctx.getBean(RegisterMedicalSupplyOrderResponderInterface.class);

    }

    @Test
    public void context() {
        assertNotNull(medicalSupplyDeliveryPoints);
    }

    @Test
    public void smokeTest() throws Exception {
        GetMedicalSupplyDeliveryPointsResponseType response = medicalSupplyDeliveryPoints
                .getMedicalSupplyDeliveryPoints("?", new GetMedicalSupplyDeliveryPointsType());

        GetMedicalSupplyPrescriptionsResponseType prescriptions =
                medicalSupplyPrescriptions.getMedicalSupplyPrescriptions("", new GetMedicalSupplyPrescriptionsType());

        RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder =
                this.registerMedicalSupplyOrder.registerMedicalSupplyOrder("", new RegisterMedicalSupplyOrderType());
    }

    @Test
    public void sort() {
        ArrayList<DeliveryPointType> deliveryPoints = new ArrayList<>();

        DeliveryPointType dp1 = new DeliveryPointType();
        dp1.setDeliveryPointAddress("ddd");
        dp1.setIsClosest(false);

        DeliveryPointType dp2 = new DeliveryPointType();
        dp2.setDeliveryPointAddress("ccc");
        dp2.setIsClosest(false);

        DeliveryPointType dp3 = new DeliveryPointType();
        dp3.setDeliveryPointAddress("bbb");
        dp3.setIsClosest(true); // Closest

        DeliveryPointType dp4 = new DeliveryPointType();
        dp4.setDeliveryPointAddress("aaa");
        dp4.setIsClosest(false);

        DeliveryPointType dp5 = new DeliveryPointType();
        dp5.setDeliveryPointAddress("ggg");
        dp5.setIsClosest(null);

        DeliveryPointType dp6 = new DeliveryPointType();
        dp6.setDeliveryPointAddress("fff");
        dp6.setIsClosest(true);

        DeliveryPointType dp7 = new DeliveryPointType();
        dp7.setDeliveryPointAddress(null);
        dp7.setIsClosest(false);

        deliveryPoints.add(dp1);
        deliveryPoints.add(dp2);
        deliveryPoints.add(dp3);
        deliveryPoints.add(dp4);
        deliveryPoints.add(dp5);
        deliveryPoints.add(dp6);
        deliveryPoints.add(dp7);

        LmnServiceImpl.sort(deliveryPoints);

        assertEquals(dp3, deliveryPoints.get(0));
        assertEquals(dp6, deliveryPoints.get(1));
        assertEquals(dp7, deliveryPoints.get(2));
        assertEquals(dp4, deliveryPoints.get(3));
        assertEquals(dp2, deliveryPoints.get(4));
        assertEquals(dp1, deliveryPoints.get(5));
        assertEquals(dp5, deliveryPoints.get(6));
    }

}