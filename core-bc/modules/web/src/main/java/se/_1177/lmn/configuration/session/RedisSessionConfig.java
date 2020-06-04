package se._1177.lmn.configuration.session;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import se._1177.lmn.service.util.LakemedelsnaraProperties;

import java.util.Arrays;

@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600) // One hour, which is more than session timeout for fronting web servers.
public class RedisSessionConfig {

	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory() {
		LakemedelsnaraProperties properties = LakemedelsnaraProperties.getInstance();

		return new LettuceConnectionFactory(
				new RedisClusterConfiguration(Arrays.asList(properties.get("spring.redis.cluster.nodes").split(",")))
		);
	}
}

