package com.pp.economia_circular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.pp.economia_circular")
@EnableJpaRepositories(basePackages = "com.pp.economia_circular.repositories")
public class EconomiaCircularApplication {

	public static void main(String[] args) {
		SpringApplication.run(EconomiaCircularApplication.class, args);
	}

}
