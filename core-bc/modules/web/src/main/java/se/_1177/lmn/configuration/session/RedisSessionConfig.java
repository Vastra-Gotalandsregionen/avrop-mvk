package se._1177.lmn.configuration.session;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600) // One hour, which is more than session timeout for fronting web servers.
public class RedisSessionConfig {

	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory() {
		return new LettuceConnectionFactory(
				/*new RedisStandaloneConfiguration("localhost", 6379)*/
		);
	}
}

