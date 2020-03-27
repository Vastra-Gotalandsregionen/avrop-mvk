package se._1177.lmn.controller;

import mvk.itintegration.userprofile._2.ResultCodeEnum;
import mvk.itintegration.userprofile._2.SubjectOfCareType;
import mvk.itintegration.userprofile._2.UserProfileType;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.ObjectFactory;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import se._1177.lmn.controller.session.UserProfileSession;
import se._1177.lmn.model.jaxb.GetUserProfileResponseTypeRootWrapper;
import se._1177.lmn.service.MvkUserProfileService;
import se._1177.lmn.service.ThreadLocalStore;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * This controller is responsible for keeping the current user which may be split in two persons if the logged in user
 * is a delegate. It reacts to changes in "delegate state". It is does not correspond the a specific view but applies
 * to all views.
 *
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileController.class);

    private static final String AJP_SUBJECT_SERIAL_NUMBER = "AJP_Subject_SerialNumber";

    @Autowired
    private MvkUserProfileService mvkUserProfileService;

    @Autowired
    private UtilController utilController;

    @Autowired
    private LocaleController localeController;

    @Autowired
    private UserProfileSession sessionData;

    @PostConstruct
    public void init() {
        if (!sessionData.isInited()) {

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

            Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

            if (requestParameterMap.containsKey("objectId")) {
                this.sessionData.setObjectId(requestParameterMap.get("objectId"));
                this.sessionData.setDelegate(true);
            } else {
                this.sessionData.setObjectId(null);
                this.sessionData.setDelegate(false);
            }

            try {
                if (this.sessionData.getUserProfileResponseLoggedInUser() == null) {
                    String ssn = getSubjectCareIdLoggedInUser();

                    this.sessionData.setUserProfileResponseLoggedInUser(mvkUserProfileService.getUserProfile(ssn));

                    ResultCodeEnum resultCode = sessionData.getUserProfileResponseLoggedInUser().getResultCode();
                    if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                        String text = sessionData.getUserProfileResponseLoggedInUser().getResultText();
                        utilController.addErrorMessageWithCustomerServiceInfo(text);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                String text = "Dina inställningar och din adress kunde inte hämtas.";
                utilController.addErrorMessageWithCustomerServiceInfo(text);
            }

            updateUserProfile();

            sessionData.setInited(true);
        }
    }

    public void updateUserProfile() {
        UserProfileType userProfile = null;

        try {
            if (sessionData.isDelegate()) {

                if (sessionData.getUserProfileByAgentResponse() == null) {
                    updateUserProfileByAgentResponse();
                }

                userProfile = sessionData.getUserProfileByAgentResponse().getCurrentUserProfile();

                ResultCodeEnum resultCode = sessionData.getUserProfileByAgentResponse().getResultCode();
                if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                    String text = sessionData.getUserProfileByAgentResponse().getResultText();
                    utilController.addErrorMessageWithCustomerServiceInfo(text);
                }

            } else {

                if (sessionData.getUserProfileResponseLoggedInUser() != null) {
                    sessionData.setUserProfileResponse(sessionData.getUserProfileResponseLoggedInUser());
                } else {
                    if (sessionData.getUserProfileResponse() == null) {
                        String ssn = getSubjectCareIdLoggedInUser();

                        sessionData.setUserProfileResponse(mvkUserProfileService.getUserProfile(ssn));
                    }

                    ResultCodeEnum resultCode = sessionData.getUserProfileResponse().getResultCode();
                    if (resultCode.equals(ResultCodeEnum.ERROR) || resultCode.equals(ResultCodeEnum.INFO)) {
                        String text = sessionData.getUserProfileResponse().getResultText();
                        utilController.addErrorMessageWithCustomerServiceInfo(text);
                    }

                }

                userProfile = sessionData.getUserProfileResponse().getUserProfile();
            }

            this.sessionData.setUserProfile(userProfile);
            ThreadLocalStore.setCountyCode(userProfile.getCountyCode());
            this.localeController.setLanguageVariant(userProfile.getCountyCode());

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
        return sessionData.getUserProfile();
    }

    private void updateUserProfileByAgentResponse() {
        String ssn = getSubjectCareIdLoggedInUser();

        sessionData.setUserProfileByAgentResponse(mvkUserProfileService.getUserProfileByAgent(
                ssn, this.sessionData.getObjectId()
                )
        );
    }

    public String getSubjectCareIdLoggedInUser() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();

//        return "199001262394";
        return request.getHeader(AJP_SUBJECT_SERIAL_NUMBER);
    }

    public SubjectOfCareType getLoggedInUser() {

        if (sessionData.getUserProfileResponseLoggedInUser() != null) {
            return sessionData.getUserProfileResponseLoggedInUser().getUserProfile();
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
                    .append(objectId, sessionData.getObjectId())
                    .build();

            sessionData.setDelegate(true);

            if (!equals) {
                sessionData.setObjectId(objectId);
            }

        } else {
            sessionData.setObjectId(null);
            sessionData.setDelegate(false);
            sessionData.setUserProfileByAgentResponse(null);
        }

        updateUserProfile();
    }

    public boolean isDelegate() {
        return sessionData.isDelegate();
    }

    public String getObjectId() {
        return sessionData.getObjectId();
    }

    public void setObjectId(String objectId) {
        // Set by checkDelegate()
    }

    public String getDelegateUrlParameters() {
        String objectId = sessionData.getObjectId();
        return objectId != null ? "?objectId=" + objectId : "";
    }

}
