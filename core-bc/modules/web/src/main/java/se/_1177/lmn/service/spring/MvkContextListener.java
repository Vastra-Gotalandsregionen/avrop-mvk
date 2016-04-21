package se._1177.lmn.service.spring;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import se._1177.lmn.service.mock.MockWebServiceServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Patrik Björk
 */
public class MvkContextListener extends ContextLoader implements ServletContextListener {

    private boolean started = false;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContextEvent.getServletContext());

        String shouldStartup = ((XmlWebApplicationContext) webApplicationContext).getBeanFactory()
                .resolveEmbeddedValue("${local.mock.service.startup}");

        if ("true".equals(shouldStartup)) {
            MockWebServiceServer.publishEndpoints(18080);
            started = true;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        if (started) {
            MockWebServiceServer.shutdown();
        }
    }
}
