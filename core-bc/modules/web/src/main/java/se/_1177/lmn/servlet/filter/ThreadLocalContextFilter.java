package se._1177.lmn.servlet.filter;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se._1177.lmn.controller.UserProfileController;
import se._1177.lmn.controller.session.UserProfileSession;
import se._1177.lmn.service.ThreadLocalStore;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * This filter is responsible for setting the countyCode of the session user profile to the current thread.
 */
@WebFilter("*")
public class ThreadLocalContextFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalContextFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        HttpSession session = request.getSession(false);

        UserProfileType userProfile = null;

        if (session != null) {
            UserProfileSession userProfileController = (UserProfileSession) session
                    .getAttribute("scopedTarget.userProfileSession");

            if (userProfileController != null) {
                userProfile = userProfileController.getUserProfile();
            }
        }

        if (userProfile != null) {
            String countyCode = userProfile.getCountyCode();

            ThreadLocalStore.setCountyCode(countyCode);
        } else {
            ThreadLocalStore.setCountyCode(null);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {

    }
}
