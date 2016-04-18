package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import se._1177.lmn.service.MvkUserProfileService;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Patrik Björk
 */
// TODO Figure out whether it's better to have request scope here and cache result or have session scope without needing to cache.
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserProfileController {

    @Autowired
    private MvkUserProfileService mvkUserProfileService;
    private GetUserProfileResponseType subjectOfCare;

    public GetUserProfileResponseType getUserProfile() {

        if (subjectOfCare == null) {

            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                    .getRequest();

            String ssn = request.getHeader("AJP_Subject_SerialNumber");

            subjectOfCare = mvkUserProfileService.getSubjectOfCare(ssn);
        }

        return subjectOfCare;
    }
}
