package se._1177.lmn.service;

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

    public MvkUserProfileService(GetUserProfileResponderInterface getUserProfileResponderInterface) {
        this.getUserProfileResponderInterface = getUserProfileResponderInterface;
    }

    public GetUserProfileResponseType getSubjectOfCare(String pid) {
        GetUserProfileType request = new GetUserProfileType();

        request.setSubjectOfCare(pid);

        GetUserProfileResponseType userProfile = getUserProfileResponderInterface.getUserProfile(request);

        return userProfile;
    }
}
