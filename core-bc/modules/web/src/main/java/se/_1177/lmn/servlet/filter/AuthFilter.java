package se._1177.lmn.servlet.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class handles authentication and authorization. It checks that relevant headers are set by the SAML SP proxy and
 * that the user is not logged in with SMS.
 *
 * @author Patrik Björk
 */
@WebFilter("*")
public class AuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    private static final String USER_ID_HEADER = "AJP_Subject_SerialNumber";
    private static final String SECURITY_LEVEL_DESCRIPTION = "AJP_SecurityLevelDescription";

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        request.setCharacterEncoding("UTF-8");

        String requestURI = request.getRequestURI();
        LOGGER.debug("RequestURI: " + requestURI);

        String env = System.getProperty("env");
        if (env != null && env.equalsIgnoreCase("dev")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String contextPath = request.getContextPath();
        String resourcePath = contextPath + "/javax.faces.resource/";

        if (requestURI.startsWith(resourcePath)) {
            // Resource requests are just let through.
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        try {
            String subjectSerialNumber = request.getHeader(USER_ID_HEADER);
//            subjectSerialNumber = "199001262394";

            String securityLevelDescription = request.getHeader(SECURITY_LEVEL_DESCRIPTION);

            boolean authenticated = subjectSerialNumber != null && subjectSerialNumber.length() > 0;

            if (authenticated) {

                boolean authenticatedWithSms = "OTP".equals(securityLevelDescription);

                if (authenticatedWithSms
                        && !requestURI.startsWith(resourcePath)
                        && !requestURI.startsWith(contextPath + "/smsNotAuthorized.xhtml")) {

                    response.sendRedirect("smsNotAuthorized.xhtml");
                } else {
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            } else {
                if (requestURI.startsWith(contextPath + "/notAuthenticated.xhtml")
                        || requestURI.startsWith(resourcePath)) {

                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    response.sendRedirect("notAuthenticated.xhtml");
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void destroy() {

    }
}
