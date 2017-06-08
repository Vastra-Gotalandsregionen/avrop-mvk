package se._1177.lmn.configuration.spring;

import org.junit.Test;
import se._1177.lmn.configuration.counties.CountiesConfiguration;

import static org.junit.Assert.assertEquals;

public class BeanConfigTest {

    @Test
    public void parseContiesConfiguration() throws Exception {
        BeanConfig beanConfig = new BeanConfig();

        CountiesConfiguration countiesConfiguration = beanConfig.parseCountiesConfiguration();

        assertEquals(2, countiesConfiguration.getCounties().size());
        assertEquals("SE2222222222-E000000000001", countiesConfiguration.getCounties().get("14").getRtjpLogicalAddress());
    }

}