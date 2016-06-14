package se._1177.lmn.service;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
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

import static org.junit.Assert.assertNotNull;

/**
 * @author Patrik Björk
 */
public class LmnServiceImplLocalIT {

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
    public void smokmeTest() throws Exception {
        GetMedicalSupplyDeliveryPointsType parameters = new GetMedicalSupplyDeliveryPointsType();

        parameters.setPostalCode("12345");
        parameters.setServicePointProvider(ServicePointProviderEnum.POSTNORD);

        GetMedicalSupplyDeliveryPointsResponseType response = medicalSupplyDeliveryPoints
                .getMedicalSupplyDeliveryPoints("?", parameters);

        GetMedicalSupplyPrescriptionsResponseType prescriptions =
                medicalSupplyPrescriptions.getMedicalSupplyPrescriptions("", new GetMedicalSupplyPrescriptionsType());

        RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder =
                this.registerMedicalSupplyOrder.registerMedicalSupplyOrder("", new RegisterMedicalSupplyOrderType());
    }

}