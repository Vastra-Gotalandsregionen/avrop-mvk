package se._1177.lmn.service.spring;

import org.springframework.web.context.ContextLoader;
import se._1177.lmn.service.mock.MockWebServiceServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Patrik Björk
 */
public class MvkContextListener extends ContextLoader implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        MockWebServiceServer.publishEndpoints(18080);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MockWebServiceServer.shutdown();
    }
}
