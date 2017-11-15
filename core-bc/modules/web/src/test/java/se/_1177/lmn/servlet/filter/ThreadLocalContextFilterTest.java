package se._1177.lmn.servlet.filter;

import mvk.itintegration.userprofile._2.UserProfileType;
import org.junit.Test;
import se._1177.lmn.controller.UserProfileController;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.service.ThreadLocalStore;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadLocalContextFilterTest {

    @Test
    public void doFilter() throws Exception {

        // Given
        ThreadLocalContextFilter filter = new ThreadLocalContextFilter();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        UserProfileType userProfileType = new UserProfileType();
        userProfileType.setCountyCode("123");

        UserProfileController userProfileController = mock(UserProfileController.class);
        when(userProfileController.getUserProfile()).thenReturn(userProfileType);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(eq("scopedTarget.userProfileController"))).thenReturn(userProfileController);

        when(servletRequest.getSession(false)).thenReturn(session);

        // When
        filter.doFilter(servletRequest, servletResponse, filterChain);

        // Then
        assertEquals("123", ThreadLocalStore.getCountyCode());
    }

}