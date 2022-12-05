package wildme.org.ratelimiter;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories
@EnableR2dbcAuditing
public class RateLimiterApplication {

	public static void main(String[] args) {
		SpringApplication.run(RateLimiterApplication.class, args);
	}

	@Bean(initMethod = "migrate")
	public Flyway flyway(Environment env) {
		return new Flyway(Flyway.configure()
				.baselineOnMigrate(true)
				.dataSource(
						env.getRequiredProperty("spring.flyway.url"),
						env.getRequiredProperty("spring.flyway.user"),
						env.getRequiredProperty("spring.flyway.password"))
		);
	}

}
