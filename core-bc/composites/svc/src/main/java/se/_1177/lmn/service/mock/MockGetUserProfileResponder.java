package se._1177.lmn.service.mock;

import mvk.itintegration.userprofile._2.ResultCodeEnum;
import mvk.itintegration.userprofile._2.UserProfileType;
import mvk.itintegration.userprofile.getuserprofile._2.rivtabp21.GetUserProfileResponderInterface;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileType;

import javax.jws.WebService;

/**
 * @author Patrik Björk
 */
@WebService(targetNamespace = "urn:mvk:itintegration:userprofile:GetUserProfile:2:rivtabp21", name = "GetUserProfileResponderInterface")
public class MockGetUserProfileResponder implements GetUserProfileResponderInterface {

    @Override
    public GetUserProfileResponseType getUserProfile(GetUserProfileType request) {
        String ssn = request.getSubjectOfCare();

        GetUserProfileResponseType response = new GetUserProfileResponseType();
        response.setIsActive(true);
        response.setResultCode(ResultCodeEnum.OK);
        response.setResultText("the result text");

        UserProfileType userProfile = new UserProfileType();
        userProfile.setEmail("dummy.email@example.com");
        userProfile.setCity("Göteborg");
        userProfile.setMobilePhoneNumber("0701234567");
        userProfile.setFirstName("Börje");
        userProfile.setLastName("Testare");
        userProfile.setPhoneNumber("031 123456");
        userProfile.setStreetAddress("Gatan 47");
        userProfile.setZip("12345");
        userProfile.setSubjectOfCareId(ssn);

        response.setUserProfile(userProfile);


        return response;
    }
}
