package se.vgregion.mvk.servlet.filter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import se.vgregion.mvk.controller.model.Cart;

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
import java.util.List;

/**
 * @author Patrik Björk
 */
@WebFilter("*")
public class SessionFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFilter.class);

    private static final String USER_ID_HEADER = "AJP_Subject_SerialNumber";
    private static final String SHIB_SESSION_ID_HEADER = "AJP_Shib-Session-ID";
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
        LOGGER.debug("RequestURI: " + requestURI);

        String contextPath = request.getContextPath();
        String resourcePath = contextPath + "/javax.faces.resource/";

        if (requestURI.startsWith(resourcePath)) {
            // Resource requests are just let through.
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String subjectSerialNumber = request.getHeader(USER_ID_HEADER);

        handleSessionInvalidation(request, subjectSerialNumber);

        boolean redirect = redirectIfInAppropriateRequest(request, response);

        if (redirect) {
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean redirectIfInAppropriateRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String servletPath = request.getServletPath();
        if (servletPath.startsWith(START_PAGE_SUFFIX) || anyPublicPage(servletPath)) {
            return false;
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            Object cart = session.getAttribute("scopedTarget.cart");
            if (cart != null) {
                List<PrescriptionItemType> itemsInCart = ((Cart) cart).getItemsInCart();

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
        String sessionSubjectSerialNumber = (String) request.getSession().getAttribute(USER_ID_HEADER);

        if (sessionSubjectSerialNumber != null && !sessionSubjectSerialNumber.equals(subjectSerialNumber)) {
            // A new user has logged in. Reset session.
            request.getSession().invalidate();
        }

        String objectId = request.getParameter(OBJECTID_PARAMETER);

        if ("".equals(objectId)) {
            objectId = null;
        }

        String sessionObjectId = (String) request.getSession().getAttribute(OBJECTID_PARAMETER);
        String sessionShibSessionId = (String) request.getSession().getAttribute(SHIB_SESSION_ID_HEADER);

        String shibSessionIdFromRequest = request.getHeader(SHIB_SESSION_ID_HEADER);

        if (!EqualsBuilder.reflectionEquals(sessionShibSessionId, shibSessionIdFromRequest)) {
            request.getSession().invalidate();
        }

        if (objectId != null && !objectId.equals(sessionObjectId)) {
            request.getSession().invalidate();
            request.getSession().setAttribute(OBJECTID_PARAMETER, objectId);
        } else if (request.getRequestURI().endsWith(START_PAGE_SUFFIX)) {
            if (!EqualsBuilder.reflectionEquals(objectId, sessionObjectId)) {
                request.getSession().invalidate();
                request.getSession().setAttribute(OBJECTID_PARAMETER, objectId);
            }
        }

        request.getSession().setAttribute(SHIB_SESSION_ID_HEADER, shibSessionIdFromRequest);
        request.getSession().setAttribute(USER_ID_HEADER, subjectSerialNumber);
    }

    public void destroy() {

    }
}
