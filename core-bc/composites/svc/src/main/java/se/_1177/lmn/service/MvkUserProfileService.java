package se._1177.lmn.service;

import mvk.itintegration.userprofile.getsubjectofcare._2.rivtabp21.GetSubjectOfCareResponderInterface;
import mvk.itintegration.userprofile.getsubjectofcareresponder._2.GetSubjectOfCareResponseType;
import mvk.itintegration.userprofile.getsubjectofcareresponder._2.GetSubjectOfCareType;
import mvk.itintegration.userprofile.getuserprofile._2.rivtabp21.GetUserProfileResponderInterface;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileType;
import org.springframework.stereotype.Service;

/**
 * @author Patrik Björk
 */
@Service
public class MvkUserProfileService {

    private GetUserProfileResponderInterface getUserProfileResponderInterface;
    private GetSubjectOfCareResponderInterface getSubjectOfCareResponderInterface;

    public MvkUserProfileService(GetUserProfileResponderInterface getUserProfileResponderInterface/*,
                                 GetSubjectOfCareResponderInterface getSubjectOfCareResponderInterface*/) {
        this.getUserProfileResponderInterface = getUserProfileResponderInterface;
//        this.getSubjectOfCareResponderInterface = getSubjectOfCareResponderInterface;
    }

    public GetUserProfileResponseType getUserProfile(String pid) {
        GetUserProfileType request = new GetUserProfileType();

        request.setSubjectOfCare(pid);

        GetUserProfileResponseType userProfile = getUserProfileResponderInterface.getUserProfile(request);

        return userProfile;
    }

    public GetSubjectOfCareResponseType getSubjectOfCare(String subjectOfCare) {
        GetSubjectOfCareType request = new GetSubjectOfCareType();

        request.setSubjectOfCare(subjectOfCare);

        GetSubjectOfCareResponseType response = getSubjectOfCareResponderInterface.getSubjectOfCare(request);

        return response;
    }
}
