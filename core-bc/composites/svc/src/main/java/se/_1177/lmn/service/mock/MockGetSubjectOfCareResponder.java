package se._1177.lmn.service.mock;

import mvk.itintegration.userprofile._2.GenderType;
import mvk.itintegration.userprofile._2.SubjectOfCareType;
import mvk.itintegration.userprofile.getsubjectofcare._2.rivtabp21.GetSubjectOfCareResponderInterface;
import mvk.itintegration.userprofile.getsubjectofcareresponder._2.GetSubjectOfCareResponseType;
import mvk.itintegration.userprofile.getsubjectofcareresponder._2.GetSubjectOfCareType;

import javax.jws.WebService;

/**
 * @author Patrik Björk
 */
@WebService(targetNamespace = "urn:mvk:itintegration:userprofile:GetSubjectOfCare:2:rivtabp21", name = "GetSubjectOfCareResponderInterface")
public class MockGetSubjectOfCareResponder implements GetSubjectOfCareResponderInterface {

    @Override
    public GetSubjectOfCareResponseType getSubjectOfCare(GetSubjectOfCareType request) {

        /*try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        GetSubjectOfCareResponseType response = new GetSubjectOfCareResponseType();

        SubjectOfCareType subjectOfCare = new SubjectOfCareType();

        subjectOfCare.setSubjectOfCareId(request.getSubjectOfCare());
        subjectOfCare.setZip("43213");
        subjectOfCare.setCity("Folkbokföringsstaden");
        subjectOfCare.setFirstName("Folke");
        subjectOfCare.setLastName("Folkesson");
        subjectOfCare.setGender(GenderType.MALE);
        subjectOfCare.setStreetAddress("Folkgatan 4");

        response.setSubjectOfCare(subjectOfCare);

        return response;
    }
}
