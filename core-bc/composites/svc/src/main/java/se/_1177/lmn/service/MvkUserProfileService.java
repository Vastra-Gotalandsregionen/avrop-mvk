package se._1177.lmn.service;

import mvk.itintegration.userprofile.getsubjectofcare._2.rivtabp21.GetSubjectOfCareResponderInterface;
import mvk.itintegration.userprofile.getsubjectofcareresponder._2.GetSubjectOfCareResponseType;
import mvk.itintegration.userprofile.getsubjectofcareresponder._2.GetSubjectOfCareType;
import mvk.itintegration.userprofile.getuserprofile._2.rivtabp21.GetUserProfileResponderInterface;
import mvk.itintegration.userprofile.getuserprofilebyagent._2.rivtabp21.GetUserProfileByAgentResponderInterface;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentResponseType;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileType;
import org.springframework.stereotype.Service;

/**
 * @author Patrik Björk
 */
@Service
public class MvkUserProfileService {

    private GetUserProfileResponderInterface getUserProfileResponderInterface;
    private GetUserProfileByAgentResponderInterface getUserProfileByAgentResponderInterface;

    public MvkUserProfileService(GetUserProfileResponderInterface getUserProfileResponderInterface,
                                 GetUserProfileByAgentResponderInterface getUserProfileByAgentResponderInterface) {
        this.getUserProfileResponderInterface = getUserProfileResponderInterface;
        this.getUserProfileByAgentResponderInterface = getUserProfileByAgentResponderInterface;
    }

    public GetUserProfileResponseType getUserProfile(String pid) {
        GetUserProfileType request = new GetUserProfileType();

        request.setSubjectOfCare(pid);

        GetUserProfileResponseType userProfile = getUserProfileResponderInterface.getUserProfile(request);

        return userProfile;
    }

    public GetUserProfileByAgentResponseType getUserProfileByAgent(String subjectOfCare, String objectId) {
        GetUserProfileByAgentType request = new GetUserProfileByAgentType();

        request.setSubjectOfCareId(subjectOfCare);
        request.setObjectId(objectId);

        return getUserProfileByAgentResponderInterface.getUserProfileByAgent(request);
    }
}
