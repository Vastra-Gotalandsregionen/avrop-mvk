package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.ResultCodeEnum;
import mvk.itintegration.userprofile._2.SubjectOfCareType;
import mvk.itintegration.userprofile._2.UserProfileType;
import mvk.itintegration.userprofile.getsubjectofcareresponder._2.GetSubjectOfCareResponseType;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import se._1177.lmn.service.MvkUserProfileService;

import javax.annotation.PostConstruct;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileController.class);

    private static final String AJP_SUBJECT_SERIAL_NUMBER = "AJP_Subject_SerialNumber";

    @Autowired
    private MvkUserProfileService mvkUserProfileService;

    /*@Autowired
    private OrderController orderController;

    @Autowired
    private CollectDeliveryController collectDeliveryController;*/

    private GetUserProfileResponseType userProfileResponse;
    private GetUserProfileByAgentResponseType userProfileByAgentResponse;
    private GetSubjectOfCareResponseType subjectOfCareResponseLoggedInUser;

    private GetSubjectOfCareResponseType subjectOfCareResponse;
    private boolean delegate;
    private String objectId;
    private String guid;
    private UserProfileType userProfile;

    @PostConstruct
    public void init() {

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

            updateUserProfile();

        try {
            if (subjectOfCareResponseLoggedInUser == null) {
                String ssn = getSubjectCareId();

                subjectOfCareResponseLoggedInUser = mvkUserProfileService.getSubjectOfCare(ssn);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            String text = "Dina folkbokföringsuppgifter kunde inte hämtas.";
            addErrorMessage(text);
        }
    }

    public void updateUserProfile() {
        UserProfileType userProfile = null;

        try {
            if (delegate) {

                if (userProfileByAgentResponse == null) {
                    updateUserProfileByAgentResponse();
                }

                userProfile = userProfileByAgentResponse.getCurrentUserProfile();

                ResultCodeEnum resultCode = userProfileByAgentResponse.getResultCode();
                if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                    String text = userProfileByAgentResponse.getResultText();
                    addErrorMessage(text);
                }

            } else {

                if (userProfileResponse == null) {
                    String ssn = getSubjectCareId();

                    userProfileResponse = mvkUserProfileService.getUserProfile(ssn);
                }

                userProfile = userProfileResponse.getUserProfile();

                ResultCodeEnum resultCode = userProfileResponse.getResultCode();
                if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                    String text = userProfileResponse.getResultText();
                    addErrorMessage(text);
                }
            }

            this.userProfile = userProfile;

            if (getUserProfile() != null) {
                // Obviously update subjectOfCareResponse if it's null but also if we are in delegate mode
                if (subjectOfCareResponse == null || !subjectOfCareResponse.getSubjectOfCare()
                        .getSubjectOfCareId()
                        .equals(getUserProfile().getSubjectOfCareId())) {

                    String subjectOfCareId = getUserProfile().getSubjectOfCareId();

                    subjectOfCareResponse = mvkUserProfileService.getSubjectOfCare(subjectOfCareId);
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            String text = "Din användare kunde inte hämtas.";
            addErrorMessage(text);
        }

    }

    private void addErrorMessage(String text) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // Don't add duplicate error messages
        for (FacesMessage facesMessage : facesContext.getMessageList()) {
            if (facesMessage.getSummary().equals(text)) {
                return;
            }
        }

        facesContext.addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, text, text));
    }

    /**
     * User profile by "folkbokföring".
     *
     * @return
     */
    public UserProfileType getUserProfile() {
        return userProfile;
    }

    private void updateUserProfileByAgentResponse() {
        String ssn = getSubjectCareId();

        userProfileByAgentResponse = mvkUserProfileService.getUserProfileByAgent(ssn, this.objectId);
    }

    private String getSubjectCareId() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();

        return request.getHeader(AJP_SUBJECT_SERIAL_NUMBER);
    }

    public SubjectOfCareType getLoggedInUser() {

        if (subjectOfCareResponseLoggedInUser != null) {
            return subjectOfCareResponseLoggedInUser.getSubjectOfCare();
        } else {
            return null;
        }
    }

    public SubjectOfCareType getSubjectOfCare() {

        if (subjectOfCareResponse != null) {
            return subjectOfCareResponse.getSubjectOfCare();
        } else {
            return null;
        }
    }

    public synchronized void checkDelegate() {

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        if (requestParameterMap.containsKey("guid")) {
            String guid = requestParameterMap.get("guid");
            String objectId = requestParameterMap.get("objectId");

            Boolean equals = new EqualsBuilder()
                    .append(objectId, this.objectId)
                    .append(guid, this.guid)
                    .build();

            this.delegate = true;

            if (!equals) {
                this.guid = guid;
                this.objectId = objectId;
            }

        } else {
            if (this.guid != null) {
                this.guid = null;
                this.objectId = null;
                this.delegate = false;
                this.userProfileByAgentResponse = null;
            } else {
                this.guid = null;
                this.objectId = null;
                this.delegate = false;
                this.userProfileByAgentResponse = null;
            }
        }
        updateUserProfile();
    }

    public String getSubjectOfCareId() {
        UserProfileType userProfile = getUserProfile();

        if (userProfile != null) {
            return userProfile.getSubjectOfCareId();
        } else {
            return null;
        }
    }

    public boolean isDelegate() {
        return delegate;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        // Set by checkDelegate()
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        // Set by checkDelegate()
    }

    public String getDelegateUrlParameters() {
        return guid != null ? "?guid=" + guid + "&objectId" + objectId : "";
    }

}
