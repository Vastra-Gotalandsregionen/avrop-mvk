package se._1177.lmn.servlet.filter;

import org.junit.Test;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import se._1177.lmn.controller.model.PrescriptionItemInfo;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.HashMap;

import static org.mockito.Mockito.*;

public class SessionFilterTest {

    private final static String SUBJECT_SERIAL_NUMBER = "191212121212";

    private static final String USER_ID_HEADER = "AJP_Subject_SerialNumber";
    private static final String SHIB_SESSION_ID_HEADER = "AJP_Shib-Session-ID";
    private static final String OBJECTID_PARAMETER = "objectId";

    private static final String START_PAGE_SUFFIX = "/order.xhtml";
    private static final String SMS_NOT_AUTHORIZED_PAGE_SUFFIX = "/smsNotAuthorized.xhtml";
    private static final String NOT_AUTHENTICATED_PAGE_SUFFIX = "/notAuthenticated.xhtml";

    @Test
    public void doFilterOkRequest() throws Exception {

        // Given
        String shibbolethSessionId = "123";

        SessionFilter sessionFilter = new SessionFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        HashMap<String, PrescriptionItemType> itemMap = new HashMap<>();
        itemMap.put("id", new PrescriptionItemType());
        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();
        prescriptionItemInfo.getChosenPrescriptionItemInfo().putAll(itemMap);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(session.getAttribute("scopedTarget.prescriptionItemInfo")).thenReturn(prescriptionItemInfo);
        when(session.getAttribute(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(session.getAttribute(OBJECTID_PARAMETER)).thenReturn(shibbolethSessionId);

        when(servletRequest.getHeader(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(servletRequest.getHeader(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(servletRequest.getContextPath()).thenReturn("/contextPath");
        when(servletRequest.getRequestURI()).thenReturn("/contextPath/somePage.xhtml");
        when(servletRequest.getServletPath()).thenReturn("/somePage.xhtml");
        when(servletRequest.getSession()).thenReturn(session);
        when(servletRequest.getSession(eq(false))).thenReturn(session);

        // When
        sessionFilter.doFilter(servletRequest, servletResponse, filterChain);

        //Then
        verify(filterChain, times(1)).doFilter(any(), any());
        verify(session, times(0)).invalidate();
    }

    @Test
    public void doFilterChangedShibSession() throws Exception {

        // Given
        String shibbolethSessionId = "123";

        SessionFilter sessionFilter = new SessionFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        // Don't put these in session since the session is going to be invalidated...
//        HashMap<String, PrescriptionItemType> itemMap = new HashMap<>();
//        itemMap.put("id", new PrescriptionItemType());
        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();
//        prescriptionItemInfo.getChosenPrescriptionItemInfo().putAll(itemMap);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(session.getAttribute("scopedTarget.prescriptionItemInfo")).thenReturn(prescriptionItemInfo);
        when(session.getAttribute(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(session.getAttribute(OBJECTID_PARAMETER)).thenReturn(shibbolethSessionId);

        when(servletRequest.getHeader(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(servletRequest.getHeader(SHIB_SESSION_ID_HEADER)).thenReturn("newSessionId"); // The important detail
        when(servletRequest.getContextPath()).thenReturn("/contextPath");
        when(servletRequest.getRequestURI()).thenReturn("/contextPath/somePage.xhtml");
        when(servletRequest.getServletPath()).thenReturn("/somePage.xhtml");
        when(servletRequest.getSession()).thenReturn(session);
        when(servletRequest.getSession(eq(false))).thenReturn(session);

        // When
        sessionFilter.doFilter(servletRequest, servletResponse, filterChain);

        //Then
        verify(filterChain, times(0)).doFilter(any(), any());
        verify(session, times(1)).invalidate();
        verify(servletResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void doFilterChangedUserByParameter() throws Exception {

        // Given
        String shibbolethSessionId = "123";

        SessionFilter sessionFilter = new SessionFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        // Don't put these in session since the session is going to be invalidated...
//        HashMap<String, PrescriptionItemType> itemMap = new HashMap<>();
//        itemMap.put("id", new PrescriptionItemType());
        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();
//        prescriptionItemInfo.getChosenPrescriptionItemInfo().putAll(itemMap);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(session.getAttribute("scopedTarget.prescriptionItemInfo")).thenReturn(prescriptionItemInfo);
        when(session.getAttribute(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(session.getAttribute(OBJECTID_PARAMETER)).thenReturn(shibbolethSessionId);

        when(servletRequest.getHeader(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(servletRequest.getHeader(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(servletRequest.getContextPath()).thenReturn("/contextPath");
        when(servletRequest.getRequestURI()).thenReturn("/contextPath/somePage.xhtml");
        when(servletRequest.getServletPath()).thenReturn("/somePage.xhtml");
        when(servletRequest.getSession()).thenReturn(session);
        when(servletRequest.getSession(eq(false))).thenReturn(session);
        when(servletRequest.getParameter(OBJECTID_PARAMETER)).thenReturn("newSessionId"); // The important detail

        // When
        sessionFilter.doFilter(servletRequest, servletResponse, filterChain);

        //Then
        verify(filterChain, times(0)).doFilter(any(), any());
        verify(session, times(1)).invalidate();
        verify(servletResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void doFilterChangedUserIdHeader() throws Exception {

        // Given
        String shibbolethSessionId = "123";

        SessionFilter sessionFilter = new SessionFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        // Don't put these in session since the session is going to be invalidated...
//        HashMap<String, PrescriptionItemType> itemMap = new HashMap<>();
//        itemMap.put("id", new PrescriptionItemType());
        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();
//        prescriptionItemInfo.getChosenPrescriptionItemInfo().putAll(itemMap);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(USER_ID_HEADER)).thenReturn("190808080808"); // The important detail
        when(session.getAttribute("scopedTarget.prescriptionItemInfo")).thenReturn(prescriptionItemInfo);
        when(session.getAttribute(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(session.getAttribute(OBJECTID_PARAMETER)).thenReturn(shibbolethSessionId);

        when(servletRequest.getHeader(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(servletRequest.getHeader(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(servletRequest.getContextPath()).thenReturn("/contextPath");
        when(servletRequest.getRequestURI()).thenReturn("/contextPath/somePage.xhtml");
        when(servletRequest.getServletPath()).thenReturn("/somePage.xhtml");
        when(servletRequest.getSession()).thenReturn(session);
        when(servletRequest.getSession(eq(false))).thenReturn(session);

        // When
        sessionFilter.doFilter(servletRequest, servletResponse, filterChain);

        //Then
        verify(filterChain, times(0)).doFilter(any(), any());
        verify(session, times(1)).invalidate();
    }

    @Test
    public void doFilterObjectIdDiscrepancyStartPage() throws Exception {

        // Given
        String shibbolethSessionId = "123";

        SessionFilter sessionFilter = new SessionFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        // Don't put these in session since the session is going to be invalidated...
//        HashMap<String, PrescriptionItemType> itemMap = new HashMap<>();
//        itemMap.put("id", new PrescriptionItemType());
        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();
//        prescriptionItemInfo.getChosenPrescriptionItemInfo().putAll(itemMap);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(session.getAttribute("scopedTarget.prescriptionItemInfo")).thenReturn(prescriptionItemInfo);
        when(session.getAttribute(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(session.getAttribute(OBJECTID_PARAMETER)).thenReturn(shibbolethSessionId);

        when(servletRequest.getHeader(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(servletRequest.getHeader(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(servletRequest.getContextPath()).thenReturn("/contextPath");
        when(servletRequest.getRequestURI()).thenReturn("/contextPath/order.xhtml");
        when(servletRequest.getServletPath()).thenReturn("/order.xhtml");
        when(servletRequest.getSession()).thenReturn(session);
        when(servletRequest.getSession(eq(false))).thenReturn(session);
        when(servletRequest.getParameter(OBJECTID_PARAMETER)).thenReturn(null);

        // When
        sessionFilter.doFilter(servletRequest, servletResponse, filterChain);

        //Then
        verify(filterChain, times(1)).doFilter(any(), any());
        verify(session, times(1)).invalidate();
    }

    @Test
    public void doFilterResourceRequest() throws Exception {

        // Given
        String shibbolethSessionId = "123";

        SessionFilter sessionFilter = new SessionFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        HashMap<String, PrescriptionItemType> itemMap = new HashMap<>();
        itemMap.put("id", new PrescriptionItemType());
        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();
        prescriptionItemInfo.getChosenPrescriptionItemInfo().putAll(itemMap);

        HttpSession session = mock(HttpSession.class);
//        when(session.getAttribute(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
//        when(session.getAttribute("scopedTarget.prescriptionItemInfo")).thenReturn(prescriptionItemInfo);
//        when(session.getAttribute(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
//        when(session.getAttribute(OBJECTID_PARAMETER)).thenReturn(shibbolethSessionId);

        when(servletRequest.getHeader(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
//        when(servletRequest.getHeader(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(servletRequest.getContextPath()).thenReturn("/contextPath");
        when(servletRequest.getRequestURI()).thenReturn("/contextPath/javax.faces.resource/someResource");
        when(servletRequest.getServletPath()).thenReturn("/javax.faces.resource/someResource");
        when(servletRequest.getSession()).thenReturn(session);
        when(servletRequest.getSession(eq(false))).thenReturn(session);

        // When
        sessionFilter.doFilter(servletRequest, servletResponse, filterChain);

        //Then
        verify(filterChain, times(1)).doFilter(any(), any());
        verify(session, times(0)).invalidate();
        verify(servletRequest, times(0)).getHeader(eq(USER_ID_HEADER)); // Should not even check this header.
    }

    @Test
    public void doFilterRequestWithoutSessionData() throws Exception {

        // Given
        String shibbolethSessionId = "123";

        SessionFilter sessionFilter = new SessionFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        /*HashMap<String, PrescriptionItemType> itemMap = new HashMap<>();
        itemMap.put("id", new PrescriptionItemType());
        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();
        prescriptionItemInfo.getChosenPrescriptionItemInfo().putAll(itemMap);*/

        HttpSession session = mock(HttpSession.class);
//        when(session.getAttribute(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
//        when(session.getAttribute("scopedTarget.prescriptionItemInfo")).thenReturn(prescriptionItemInfo);
//        when(session.getAttribute(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
//        when(session.getAttribute(OBJECTID_PARAMETER)).thenReturn(shibbolethSessionId);

        when(servletRequest.getHeader(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
//        when(servletRequest.getHeader(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(servletRequest.getContextPath()).thenReturn("/contextPath");
        when(servletRequest.getRequestURI()).thenReturn("/contextPath/verify.xhtml");
        when(servletRequest.getServletPath()).thenReturn("/verify.xhtml");
        when(servletRequest.getSession()).thenReturn(session);
        when(servletRequest.getSession(eq(false))).thenReturn(session);

        // When
        sessionFilter.doFilter(servletRequest, servletResponse, filterChain);

        //Then
        verify(filterChain, times(0)).doFilter(any(), any());
        verify(session, times(0)).invalidate();
        verify(servletResponse, times(1)).sendRedirect(anyString()); // Important
    }

    @Test
    public void doFilterRequestWithoutSessionItemsInCart() throws Exception {

        // Given
        String shibbolethSessionId = "123";

        SessionFilter sessionFilter = new SessionFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        HashMap<String, PrescriptionItemType> itemMap = new HashMap<>();
//        itemMap.put("id", new PrescriptionItemType()); // So we have zero items
        PrescriptionItemInfo prescriptionItemInfo = new PrescriptionItemInfo();
        prescriptionItemInfo.getChosenPrescriptionItemInfo().putAll(itemMap);

        HttpSession session = mock(HttpSession.class);
//        when(session.getAttribute(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(session.getAttribute("scopedTarget.prescriptionItemInfo")).thenReturn(prescriptionItemInfo);
//        when(session.getAttribute(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
//        when(session.getAttribute(OBJECTID_PARAMETER)).thenReturn(shibbolethSessionId);

        when(servletRequest.getHeader(USER_ID_HEADER)).thenReturn(SUBJECT_SERIAL_NUMBER);
//        when(servletRequest.getHeader(SHIB_SESSION_ID_HEADER)).thenReturn(shibbolethSessionId);
        when(servletRequest.getContextPath()).thenReturn("/contextPath");
        when(servletRequest.getRequestURI()).thenReturn("/contextPath/verify.xhtml");
        when(servletRequest.getServletPath()).thenReturn("/verify.xhtml");
        when(servletRequest.getSession()).thenReturn(session);
        when(servletRequest.getSession(eq(false))).thenReturn(session);

        // When
        sessionFilter.doFilter(servletRequest, servletResponse, filterChain);

        //Then
        verify(filterChain, times(0)).doFilter(any(), any());
        verify(session, times(0)).invalidate();
        verify(servletResponse, times(1)).sendRedirect(anyString()); // Important
    }
}