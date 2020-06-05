package se._1177.lmn.servlet.filter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import se._1177.lmn.controller.model.PrescriptionItemInfo;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

/**
 * This filter is responsible for invalidating the session if the user changes. There is no way the user can log out of
 * the application. The user can log out of the SAML IDP and SP but the application does not get to know when this
 * happens. So the session is either invalidated because of the container's timeout setting or if the user has changed.
 * @author Patrik Björk
 */
@WebFilter("*")
public class SessionFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFilter.class);

    private static final String USER_ID_HEADER = "AJP_Subject_SerialNumber";
    private static final String SHIB_SESSION_ID_HEADER = "AJP_Shib-Session-ID";

    // The object id is passed as a request parameter to the application and stored in the session. This filter detects
    // when a changed object id is passed and uses that to invalidate the session.
    private static final String OBJECTID_PARAMETER = "objectId";

    private static final String START_PAGE_SUFFIX = "/order.xhtml";
    private static final String SMS_NOT_AUTHORIZED_PAGE_SUFFIX = "/smsNotAuthorized.xhtml";
    private static final String NOT_AUTHENTICATED_PAGE_SUFFIX = "/notAuthenticated.xhtml";

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        request.setCharacterEncoding("UTF-8");

        String requestURI = request.getRequestURI();

        String contextPath = request.getContextPath();
        String resourcePath = contextPath + "/javax.faces.resource/";

        if (requestURI.startsWith(resourcePath)) {
            // Resource requests are just let through.
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String subjectSerialNumber = request.getHeader(USER_ID_HEADER);

        boolean redirect = false;
        try {
            handleSessionInvalidation(request, subjectSerialNumber);

            redirect = redirectIfInappropriateRequest(request, response);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            redirectToOrderPage(request, response);
            return;
        }

        if (redirect) {
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);

        touchSessionObjects(request);
    }

    private void touchSessionObjects(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Enumeration<String> attributeNames = session.getAttributeNames();

        while (attributeNames.hasMoreElements()) {
            String element = attributeNames.nextElement();

//            if (element.startsWith("scopedTarget")) {
                session.setAttribute(element, session.getAttribute(element));
//            }
        }
    }

    private boolean redirectIfInappropriateRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String servletPath = request.getServletPath();
        if (servletPath.startsWith(START_PAGE_SUFFIX) || anyPublicPage(servletPath)) {
            return false;
        }

        // As the code is written there will always be a session at this point but we don't want to rely on that - this
        // method should be failsafe independently of what's been done before.
        HttpSession session = request.getSession(false);

        if (session != null) {
            Object prescriptionItemInfo = session.getAttribute("scopedTarget.prescriptionItemInfo");
            if (prescriptionItemInfo != null) {
                Map<String, PrescriptionItemType> itemsInCart = ((PrescriptionItemInfo) prescriptionItemInfo)
                        .getChosenPrescriptionItemInfo();

                if (itemsInCart == null || itemsInCart.size() == 0) {
                    redirectToOrderPage(request, response);
                    return true;
                } else {
                    return false;
                }
            } else {
                redirectToOrderPage(request, response);
                return true;
            }
        } else {
            redirectToOrderPage(request, response);
            return true;
        }
    }

    private boolean anyPublicPage(String servletPath) {
        return servletPath.startsWith(SMS_NOT_AUTHORIZED_PAGE_SUFFIX)
                || servletPath.startsWith(NOT_AUTHENTICATED_PAGE_SUFFIX);
    }

    private void redirectToOrderPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String queryString = "";
        Object sessionObjectId = session.getAttribute(OBJECTID_PARAMETER);
        if (sessionObjectId != null && !"".equals(sessionObjectId)) {
            queryString = "?objectId=" + sessionObjectId;
        }
        response.sendRedirect(request.getContextPath() + START_PAGE_SUFFIX + queryString);
    }

    // If any of the session attributes have changed invalidate session to start all over.
    private void handleSessionInvalidation(HttpServletRequest request, String subjectSerialNumber) {
        HttpSession session = request.getSession();
        String sessionSubjectSerialNumber = (String) session.getAttribute(USER_ID_HEADER);

        if (sessionSubjectSerialNumber != null && !sessionSubjectSerialNumber.equals(subjectSerialNumber)) {
            // A new user has logged in. Reset session.
            session = resetSession(request, session);
        }

        String objectIdFromRequestParam = request.getParameter(OBJECTID_PARAMETER);

        if ("".equals(objectIdFromRequestParam)) {
            objectIdFromRequestParam = null;
        }

        String sessionObjectId = (String) session.getAttribute(OBJECTID_PARAMETER);
        String sessionShibSessionId = (String) session.getAttribute(SHIB_SESSION_ID_HEADER);

        String shibSessionIdFromRequest = request.getHeader(SHIB_SESSION_ID_HEADER);

        if (!new EqualsBuilder().append(sessionShibSessionId, shibSessionIdFromRequest).isEquals()) {
            session = resetSession(request, session);
        }

        if (objectIdFromRequestParam != null && !objectIdFromRequestParam.equals(sessionObjectId)) {
            session = resetSession(request, session);
            session.setAttribute(OBJECTID_PARAMETER, objectIdFromRequestParam);
        } else if (request.getRequestURI().endsWith(START_PAGE_SUFFIX)) {
            // It is normal that objectIdFromRequestParam is null when page isn't the start page but on the start page
            // the object id should always be passed if it exists.
            if (!new EqualsBuilder().append(objectIdFromRequestParam, sessionObjectId).isEquals()) {
                session = resetSession(request, session);
                session.setAttribute(OBJECTID_PARAMETER, objectIdFromRequestParam);
            }
        }

        session.setAttribute(SHIB_SESSION_ID_HEADER, shibSessionIdFromRequest);
        session.setAttribute(USER_ID_HEADER, subjectSerialNumber);
    }

    public void destroy() {

    }

    private HttpSession resetSession(HttpServletRequest request, HttpSession session) {
        session.invalidate();
        return request.getSession(true);
    }
}
