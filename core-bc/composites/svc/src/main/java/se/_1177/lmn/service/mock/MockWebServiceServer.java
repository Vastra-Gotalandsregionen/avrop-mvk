package se._1177.lmn.service.mock;

import javax.xml.ws.Endpoint;

/**
 * @author Patrik Björk
 */
public class MockWebServiceServer {

    private static Endpoint endpoint1;
    private static Endpoint endpoint2;
    private static Endpoint endpoint3;
    private static Endpoint endpoint4;
    private static Endpoint endpoint5;

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

        endpoint4 = Endpoint.publish(
                endpointBase + "/GetUserProfile",
                new MockGetUserProfileResponder());

        endpoint5 = Endpoint.publish(
                endpointBase + "/AddMessage",
                new MockAddMessageResponder());
    }

    public static void shutdown() {
        if (endpoint1 != null) {
            endpoint1.stop();
        }

        if (endpoint1 != null) {
            endpoint2.stop();
        }
        if (endpoint1 != null) {
            endpoint3.stop();
        }
        if (endpoint1 != null) {
            endpoint4.stop();
        }
    }

}
