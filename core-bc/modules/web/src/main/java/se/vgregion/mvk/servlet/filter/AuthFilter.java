package se.vgregion.mvk.servlet.filter;

import org.apache.commons.lang3.builder.EqualsBuilder;
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
 * @author Patrik Björk
 */
@WebFilter("*")
public class AuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    private static final String USER_ID_HEADER = "AJP_Subject_SerialNumber";
    private static final String SECURITY_LEVEL_DESCRIPTION = "AJP_SecurityLevelDescription";
    private static final String GUID_PARAMETER = "guid";;

    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Filter init...");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        request.setCharacterEncoding("UTF-8");

        String requestURI = request.getRequestURI();
        LOGGER.debug("RequestURI: " + requestURI);

        String contextPath = request.getContextPath();
        String resourcePath = contextPath + "/javax.faces.resource/";

        if (requestURI.startsWith(resourcePath)) {
            // Resource requests are just let through.
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        try {
            String subjectSerialNumber = request.getHeader(this.USER_ID_HEADER);


            String securityLevelDescription = request.getHeader(this.SECURITY_LEVEL_DESCRIPTION);

            handleSession(request, subjectSerialNumber);

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

    // If any of the session attributes have changed invalidate session to start all over.
    private void handleSession(HttpServletRequest request, String subjectSerialNumber) {
        String sessionSubjectSerialNumber = (String) request.getSession().getAttribute(USER_ID_HEADER);

        if (sessionSubjectSerialNumber != null && !sessionSubjectSerialNumber.equals(subjectSerialNumber)) {
            // A new user has logged in. Reset session.
            request.getSession().invalidate();
        }

        String guid = request.getParameter(GUID_PARAMETER);
        if ("".equals(guid)) {
            guid = null;
        }

        String sessionGuid = (String) request.getSession().getAttribute(GUID_PARAMETER);

        if (guid != null && !guid.equals(sessionGuid)) {
            request.getSession().invalidate();
            request.getSession().setAttribute(GUID_PARAMETER, guid);
        } else if (request.getRequestURI().endsWith("/order.xhtml")) {
            if (!EqualsBuilder.reflectionEquals(guid, sessionGuid)) {
                request.getSession().invalidate();
                request.getSession().setAttribute(GUID_PARAMETER, guid);
            }
        }

        request.getSession().setAttribute(USER_ID_HEADER, subjectSerialNumber);
    }

    public void destroy() {

    }
}
