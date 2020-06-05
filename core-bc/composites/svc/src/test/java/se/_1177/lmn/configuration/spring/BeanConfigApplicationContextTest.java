package se._1177.lmn.configuration.spring;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.testconfig.RedisSessionConfig;
import se._1177.lmn.testconfig.TestRedisConfiguration;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BeanConfig.class, CachingConfig.class, RedisSessionConfig.class, TestRedisConfiguration.class})
public class BeanConfigApplicationContextTest {

    @Autowired
    private ApplicationContext ctx;

    @BeforeClass
    public static void setup() {
        URL truststore = BeanConfigApplicationContextTest.class.getClassLoader().getResource("truststore.jks");
        URL keystore = BeanConfigApplicationContextTest.class.getClassLoader().getResource("keystore.p12");

        System.setProperty("ssl_truststore", truststore.getFile());
        System.setProperty("ssl_keystore", keystore.getFile());
    }

    @Test
    public void contextLoaded() throws Exception {
        assertNotNull(ctx);
    }

    @Test
    public void getBean() throws Exception {
        assertNotNull(ctx.getBeansOfType(LmnService.class));
    }

}
