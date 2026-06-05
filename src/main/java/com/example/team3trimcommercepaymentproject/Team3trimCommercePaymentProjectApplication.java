package com.example.team3trimcommercepaymentproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Team3trimCommercePaymentProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(Team3trimCommercePaymentProjectApplication.class, args);
	}

}
