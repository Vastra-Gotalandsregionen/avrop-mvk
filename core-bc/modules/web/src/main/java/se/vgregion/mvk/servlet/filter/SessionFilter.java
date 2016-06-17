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

import static javax.swing.text.html.CSS.getAttribute;

/**
 * @author Patrik Björk
 */
@WebFilter("*")
public class SessionFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFilter.class);

    private static final String USER_ID_HEADER = "AJP_Subject_SerialNumber";
    private static final String GUID_PARAMETER = "guid";;
    private static final String OBJECTID_PARAMETER = "objectId";;

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

        try {
            String subjectSerialNumber = request.getHeader(USER_ID_HEADER);

            handleSessionInvalidation(request, subjectSerialNumber);

            boolean redirect = redirectIfInAppropriateRequest(request, response);

            if (redirect) {
                return;
            }

        } catch (Exception e) {
            throw e;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean redirectIfInAppropriateRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (request.getServletPath().startsWith("/order.xhtml")) {
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

    private void redirectToOrderPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String queryString = "";
        Object sessionGuid = session.getAttribute(GUID_PARAMETER);
        if (sessionGuid != null && !"".equals(sessionGuid)) {
            Object sessionObjectId = session.getAttribute(OBJECTID_PARAMETER);
            queryString = "?guid=" + sessionGuid + "&objectId=" + sessionObjectId;
        }
        response.sendRedirect(request.getContextPath() + "/order.xhtml" + queryString);
    }

    // If any of the session attributes have changed invalidate session to start all over.
    private void handleSessionInvalidation(HttpServletRequest request, String subjectSerialNumber) {
        String sessionSubjectSerialNumber = (String) request.getSession().getAttribute(USER_ID_HEADER);

        if (sessionSubjectSerialNumber != null && !sessionSubjectSerialNumber.equals(subjectSerialNumber)) {
            // A new user has logged in. Reset session.
            request.getSession().invalidate();
        }

        String guid = request.getParameter(GUID_PARAMETER);
        String objectId = request.getParameter(OBJECTID_PARAMETER);

        if ("".equals(guid)) {
            guid = null;
        }

        String sessionGuid = (String) request.getSession().getAttribute(GUID_PARAMETER);

        if (guid != null && !guid.equals(sessionGuid)) {
            request.getSession().invalidate();
            request.getSession().setAttribute(GUID_PARAMETER, guid);
            request.getSession().setAttribute(OBJECTID_PARAMETER, objectId);
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
