package se._1177.lmn.service.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class LakemedelsnaraPropertiesTest {

    private final LakemedelsnaraProperties properties = LakemedelsnaraProperties.getInstance();

    @Test
    public void get() throws Exception {
        assertEquals("https://url", properties.get("backToOwnProfileLink"));
    }

    @Test
    public void get1() throws Exception {
        assertEquals("defaultValue", properties.get("non-existing-property", "defaultValue"));
    }

    @Test
    public void getProperties() throws Exception {
        assertEquals("https://url", LakemedelsnaraProperties.getProperties().getProperty("backToOwnProfileLink"));
    }

    @Test
    public void getInstance() throws Exception {
        assertEquals(properties, LakemedelsnaraProperties.getInstance());
    }

}