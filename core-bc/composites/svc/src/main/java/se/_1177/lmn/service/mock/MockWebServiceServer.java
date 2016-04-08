package se._1177.lmn.service.mock;

import javax.xml.ws.Endpoint;

/**
 * @author Patrik Björk
 */
public class MockWebServiceServer {

    private static Endpoint endpoint1;
    private static Endpoint endpoint2;
    private static Endpoint endpoint3;

    public static void main(String[] args) {
        publishEndpoints();
    }

    public static void publishEndpoints() {
        String endpointBase = "http://localhost:18080";

        endpoint1 = Endpoint.publish(
                endpointBase + "/GetMedicalSupplyDeliveryPoints",
                new MockGetMedicalSupplyDeliveryPointsResponder());

        endpoint2 = Endpoint.publish(
                endpointBase + "/GetMedicalSupplyPrescriptions",
                new MockGetMedicalSupplyPrescriptionsResponder());

        endpoint3 = Endpoint.publish(
                endpointBase + "/RegisterMedicalSupplyOrder",
                new MockRegisterMedicalSupplyOrderResponder());
    }

    public static void shutdown() {
        endpoint1.stop();
        endpoint2.stop();
        endpoint3.stop();
    }

}
