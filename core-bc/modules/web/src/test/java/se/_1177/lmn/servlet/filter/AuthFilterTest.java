package se._1177.lmn.servlet.filter;

import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class AuthFilterTest {

    private final static String SUBJECT_SERIAL_NUMBER = "191212121212";

    @Test
    public void doFilterNotAuthenticated() throws Exception {

        // Given
        AuthFilter authFilter = new AuthFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(servletRequest.getRequestURI()).thenReturn("/path/to/page.xhtml");

        // When
        authFilter.doFilter(servletRequest, servletResponse, filterChain);

        // Then
        verify(servletResponse, times(1)).sendRedirect(eq("notAuthenticated.xhtml"));
        verify(filterChain, times(0)).doFilter(any(), any());
    }

    @Test
    public void doFilterAuthenticated() throws Exception {

        // Given

        AuthFilter authFilter = new AuthFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(servletRequest.getRequestURI()).thenReturn("/path/to/page.xhtml");
        when(servletRequest.getHeader("AJP_Subject_SerialNumber")).thenReturn(SUBJECT_SERIAL_NUMBER);
        when(servletRequest.getHeader("AJP_SecurityLevelDescription")).thenReturn("somethingNotEqualsToOTP");

        // When
        authFilter.doFilter(servletRequest, servletResponse, filterChain);

        // Then
        verify(servletResponse, times(0)).sendRedirect(anyString());
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    public void doFilterAuthenticatedWithSms() throws Exception {

        // Given
        String subjectSerialNumber = "191212121212";

        AuthFilter authFilter = new AuthFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(servletRequest.getRequestURI()).thenReturn("/path/to/page.xhtml");
        when(servletRequest.getHeader("AJP_Subject_SerialNumber")).thenReturn(subjectSerialNumber);
        when(servletRequest.getHeader("AJP_SecurityLevelDescription")).thenReturn("OTP");

        // When
        authFilter.doFilter(servletRequest, servletResponse, filterChain);

        // Then
        verify(servletResponse, times(1)).sendRedirect(eq("smsNotAuthorized.xhtml"));
        verify(filterChain, times(0)).doFilter(any(), any());
    }

    @Test
    public void doFilterRequestResource() throws Exception {

        // Given
        AuthFilter authFilter = new AuthFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(servletRequest.getRequestURI()).thenReturn("/contextPath/javax.faces.resource/resource");
        when(servletRequest.getContextPath()).thenReturn("/contextPath");

        // When
        authFilter.doFilter(servletRequest, servletResponse, filterChain);

        // Then
        verify(servletResponse, times(0)).sendRedirect(anyString());
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    public void doFilterRequestSmsNotAuthorized() throws Exception {

        // Given
        String subjectSerialNumber = "191212121212";

        AuthFilter authFilter = new AuthFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(servletRequest.getRequestURI()).thenReturn("/path/to/page.xhtml");
        when(servletRequest.getHeader("AJP_Subject_SerialNumber")).thenReturn(subjectSerialNumber);
        when(servletRequest.getHeader("AJP_SecurityLevelDescription")).thenReturn("OTP");

        when(servletRequest.getRequestURI()).thenReturn("/contextPath/smsNotAuthorized.xhtml");
        when(servletRequest.getContextPath()).thenReturn("/contextPath");

        // When
        authFilter.doFilter(servletRequest, servletResponse, filterChain);

        // Then
        verify(servletResponse, times(0)).sendRedirect(anyString());
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    public void doFilterRequestNotAuthenticated() throws Exception {

        // Given
        AuthFilter authFilter = new AuthFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(servletRequest.getRequestURI()).thenReturn("/contextPath/notAuthenticated.xhtml");
        when(servletRequest.getContextPath()).thenReturn("/contextPath");

        // When
        authFilter.doFilter(servletRequest, servletResponse, filterChain);

        // Then
        verify(servletResponse, times(0)).sendRedirect(anyString());
        verify(filterChain, times(1)).doFilter(any(), any());
    }

}