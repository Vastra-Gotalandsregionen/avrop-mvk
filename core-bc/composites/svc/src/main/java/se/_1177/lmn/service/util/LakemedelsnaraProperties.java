package se._1177.lmn.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton class keeping the properties which are loaded from a file on the classpath.
 */
public class LakemedelsnaraProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(LakemedelsnaraProperties.class);
    private static final String PROPERTIES_FILENAME = "lakemedelsnara.properties";
    private static final LakemedelsnaraProperties INSTANCE = new LakemedelsnaraProperties();

    private Properties properties = new Properties();

    private LakemedelsnaraProperties() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream(PROPERTIES_FILENAME);
        if (input != null) {
            try {
                properties.load(input);
            } catch (IOException e) {
                LOGGER.error("Failed to load property file \"" + PROPERTIES_FILENAME + "\"");
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.error("Missing property file \"" + PROPERTIES_FILENAME + "\"");
            throw new RuntimeException("Missing property file \"" + PROPERTIES_FILENAME + "\"");
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public static Properties getProperties() {
        return getInstance().properties;
    }

    public static LakemedelsnaraProperties getInstance() {
        return INSTANCE;
    }
}