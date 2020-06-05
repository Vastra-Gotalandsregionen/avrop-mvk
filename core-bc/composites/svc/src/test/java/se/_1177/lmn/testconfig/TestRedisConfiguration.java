package se._1177.lmn.testconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
@Import(RedisProperties.class)
public class TestRedisConfiguration {
 
    private redis.embedded.RedisServer redisServer;

    @Autowired
    public TestRedisConfiguration(RedisProperties redisProperties) {
        this.redisServer = new redis.embedded.RedisServer(redisProperties.getRedisPort());
    }
 
    @PostConstruct
    public void postConstruct() {
        redisServer.start();
    }
 
    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }
}
