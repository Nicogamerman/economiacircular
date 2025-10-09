package com.pp.economia_circular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pp.economia_circular","com.pp.economia_circular.config" })
@EnableJpaRepositories(basePackages = "com.pp.economia_circular.repositories")
public class EconomiaCircularApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EconomiaCircularApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(EconomiaCircularApplication.class, args);
	}
}