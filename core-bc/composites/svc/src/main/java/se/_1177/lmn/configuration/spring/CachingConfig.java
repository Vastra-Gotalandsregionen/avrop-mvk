package se._1177.lmn.configuration.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import se._1177.lmn.service.LmnService;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CachingConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingConfig.class);

    public static final String SUPPLY_DELIVERY_POINTS_CACHE = "supplyDeliveryPoints";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory lettuceConnectionFactory/*, CacheConfigurationProperties properties*/) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        return RedisCacheManager
                .builder(lettuceConnectionFactory)
                .cacheDefaults(createCacheConfiguration(2 * 60 * 60)) // Two hours
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }

    private static RedisCacheConfiguration createCacheConfiguration(long timeoutInSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeoutInSeconds));
    }

    @Bean("supplyDeliveryKeyGenerator")
    public KeyGenerator supplyDeliveryKeyGenerator() {
        return new SupplyDeliveryKeyGenerator();
    }

    public static class SupplyDeliveryKeyGenerator implements KeyGenerator {
        @Override
        public Object generate(Object target, Method method, Object... params) {
            // The purpose to include the canonical class name is to separate cache between co-existing versions of the application.
            // The canonical name changes between versions of the service contract.
            return ((LmnService) target).getLogicalAddress()
                    + params[0]
                    + params[1]
                    + params[0].getClass().getCanonicalName();
        }
    }
}
