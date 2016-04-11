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
        publishEndpoints(18080);
    }

    public static void publishEndpoints(int port) {
        String endpointBase = "http://localhost:" + port;

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
