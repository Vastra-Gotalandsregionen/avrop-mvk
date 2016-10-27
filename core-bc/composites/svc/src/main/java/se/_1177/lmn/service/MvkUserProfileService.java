package se._1177.lmn.service;

import mvk.itintegration.userprofile.getuserprofile._2.rivtabp21.GetUserProfileResponderInterface;
import mvk.itintegration.userprofile.getuserprofilebyagent._2.rivtabp21.GetUserProfileByAgentResponderInterface;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentResponseType;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileType;
import org.springframework.stereotype.Service;

/**
 * Class for fetching information about users.
 *
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

    /**
     * Get the user profile from personal id.
     *
     * @param pid the user's pid
     * @return the {@link GetUserProfileResponseType} response
     */
    public GetUserProfileResponseType getUserProfile(String pid) {
        GetUserProfileType request = new GetUserProfileType();

        request.setSubjectOfCare(pid);

        GetUserProfileResponseType userProfile = getUserProfileResponderInterface.getUserProfile(request);

        return userProfile;
    }

    /**
     * Fetching a person who the subject of care is delegate for.
     *
     * @param subjectOfCare the subject of care id of the delegate user
     * @param objectId an object id which is mapped to a person in the source system
     * @return the {@link GetUserProfileByAgentResponseType}
     */
    public GetUserProfileByAgentResponseType getUserProfileByAgent(String subjectOfCare, String objectId) {
        GetUserProfileByAgentType request = new GetUserProfileByAgentType();

        request.setSubjectOfCareId(subjectOfCare);
        request.setObjectId(objectId);

        return getUserProfileByAgentResponderInterface.getUserProfileByAgent(request);
    }
}
