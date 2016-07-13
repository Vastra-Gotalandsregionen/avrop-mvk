package se._1177.lmn.controller;

import mvk.itintegration.userprofile._2.ResultCodeEnum;
import mvk.itintegration.userprofile._2.SubjectOfCareType;
import mvk.itintegration.userprofile._2.UserProfileType;
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

    @Autowired
    private UtilController utilController;

    private GetUserProfileResponseType userProfileResponse;
    private GetUserProfileResponseType userProfileResponseLoggedInUser;
    private GetUserProfileByAgentResponseType userProfileByAgentResponse;

    private boolean delegate;
    private String objectId;
    private UserProfileType userProfile;

    @PostConstruct
    public void init() {

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        if (requestParameterMap.containsKey("objectId")) {
            this.objectId = requestParameterMap.get("objectId");
            this.delegate = true;
        } else {
            this.objectId = null;
            this.delegate = false;
        }

        try {
            if (userProfileResponseLoggedInUser == null) {
                String ssn = getSubjectCareIdLoggedInUser();

                userProfileResponseLoggedInUser = mvkUserProfileService.getUserProfile(ssn);

                ResultCodeEnum resultCode = userProfileResponseLoggedInUser.getResultCode();
                if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                    String text = userProfileResponseLoggedInUser.getResultText();
                    utilController.addErrorMessageWithCustomerServiceInfo(text);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            String text = "Dina inställningar och din adress kunde inte hämtas.";
            utilController.addErrorMessageWithCustomerServiceInfo(text);
        }

        updateUserProfile();
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
                    utilController.addErrorMessageWithCustomerServiceInfo(text);
                }

            } else {

                if (userProfileResponseLoggedInUser != null) {
                    userProfileResponse = userProfileResponseLoggedInUser;
                } else {
                    if (userProfileResponse == null) {
                        String ssn = getSubjectCareIdLoggedInUser();

                        userProfileResponse = mvkUserProfileService.getUserProfile(ssn);
                    }

                    ResultCodeEnum resultCode = userProfileResponse.getResultCode();
                    if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                        String text = userProfileResponse.getResultText();
                        utilController.addErrorMessageWithCustomerServiceInfo(text);
                    }

                }

                userProfile = userProfileResponse.getUserProfile();
            }

            this.userProfile = userProfile;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            String text = "Dina inställningar och din adress kunde inte hämtas.";
            utilController.addErrorMessageWithCustomerServiceInfo(text);
        }
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
        String ssn = getSubjectCareIdLoggedInUser();

        userProfileByAgentResponse = mvkUserProfileService.getUserProfileByAgent(ssn, this.objectId);
    }

    public String getSubjectCareIdLoggedInUser() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();

        return request.getHeader(AJP_SUBJECT_SERIAL_NUMBER);
    }

    public SubjectOfCareType getLoggedInUser() {

        if (userProfileResponseLoggedInUser != null) {
            return userProfileResponseLoggedInUser.getUserProfile();
        } else {
            return null;
        }
    }

    public synchronized void checkDelegate() {

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        if (requestParameterMap.containsKey("objectId")) {
            String objectId = requestParameterMap.get("objectId");

            Boolean equals = new EqualsBuilder()
                    .append(objectId, this.objectId)
                    .build();

            this.delegate = true;

            if (!equals) {
                this.objectId = objectId;
            }

        } else {
            if (this.objectId != null) {
                this.objectId = null;
                this.delegate = false;
                this.userProfileByAgentResponse = null;
            } else {
                this.objectId = null;
                this.delegate = false;
                this.userProfileByAgentResponse = null;
            }
        }
        updateUserProfile();
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

    public String getDelegateUrlParameters() {
        return objectId != null ? "?objectId=" + objectId : "";
    }

}
