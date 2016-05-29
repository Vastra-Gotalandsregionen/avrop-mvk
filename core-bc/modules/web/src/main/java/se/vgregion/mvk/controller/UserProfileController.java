package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.ResultCodeEnum;
import mvk.itintegration.userprofile._2.SubjectOfCareType;
import mvk.itintegration.userprofile._2.UserProfileType;
import mvk.itintegration.userprofile.getsubjectofcareresponder._2.GetSubjectOfCareResponseType;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import se._1177.lmn.service.MvkUserProfileService;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Patrik Björk
 */
// TODO Figure out whether it's better to have request scope here and cache result or have session scope without needing to cache.
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserProfileController {

    @Autowired
    private MvkUserProfileService mvkUserProfileService;

    private GetUserProfileResponseType userProfileResponse;
    private GetUserProfileByAgentResponseType userProfileByAgentResponse;
    private GetSubjectOfCareResponseType subjectOfCareResponseLoggedInUser;
    private GetSubjectOfCareResponseType subjectOfCareResponse;

    private boolean delegate;
    private String objectId;
    private String guid;

    public UserProfileType getUserProfile() {

        UserProfileType toReturn = null;

        if (delegate) {

            if (userProfileByAgentResponse == null) {
                String ssn = getSubjectCareId();

                userProfileByAgentResponse = mvkUserProfileService.getUserProfileByAgent(ssn, this.objectId);
            }

            toReturn = userProfileByAgentResponse.getCurrentUserProfile();

            ResultCodeEnum resultCode = userProfileResponse.getResultCode();
            if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                String text = userProfileResponse.getResultText();
                FacesContext.getCurrentInstance().addMessage("",
                        new FacesMessage(FacesMessage.SEVERITY_FATAL, text, text));
            }

        } else {

            if (userProfileResponse == null) {
                String ssn = getSubjectCareId();

                userProfileResponse = mvkUserProfileService.getUserProfile(ssn);
            }

            toReturn = userProfileResponse.getUserProfile();

            ResultCodeEnum resultCode = userProfileResponse.getResultCode();
            if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                String text = userProfileResponse.getResultText();
                FacesContext.getCurrentInstance().addMessage("",
                        new FacesMessage(FacesMessage.SEVERITY_FATAL, text, text));
            }
        }

        return toReturn;
    }

    private String getSubjectCareId() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();

        return request.getHeader("AJP_Subject_SerialNumber");
    }

    public SubjectOfCareType getLoggedInUser() {

        if (subjectOfCareResponseLoggedInUser == null) {
            String ssn = getSubjectCareId();

            subjectOfCareResponseLoggedInUser = mvkUserProfileService.getSubjectOfCare(ssn);
        }

        return subjectOfCareResponseLoggedInUser.getSubjectOfCare();
    }

    public SubjectOfCareType getSubjectOfCare() {

        if (subjectOfCareResponse == null) {

            String subjectOfCareId = getUserProfile().getSubjectOfCareId();

            subjectOfCareResponse = mvkUserProfileService.getSubjectOfCare(subjectOfCareId);
        }

        return subjectOfCareResponse.getSubjectOfCare();
    }

    public void checkDelegate() {

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        if (requestParameterMap.containsKey("guid")) {
            this.guid = requestParameterMap.get("guid");
            this.objectId = requestParameterMap.get("objectId");
            this.delegate = true;
        } else {
            this.guid = null;
            this.objectId = null;
            this.delegate = false;
        }
    }

    public String getSubjectOfCareId() {
        return getUserProfile().getSubjectOfCareId();
    }

    public boolean isDelegate() {
        return delegate;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getDelegateUrlParameters() {
        return guid != null ? "?guid=" + guid + "&objectId" + objectId : "";
    }

    // ?guid=C8E52E08A3CEBB546F27A61416D6DC3DA0B13441E2437C953692A057E0F2F5CF&objectId=449a660049744cc586f0643cb6059a85
}
