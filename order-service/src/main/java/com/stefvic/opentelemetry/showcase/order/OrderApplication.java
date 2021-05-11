package com.stefvic.opentelemetry.showcase.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.config.EnableReactiveCassandraAuditing;
import org.springframework.data.domain.ReactiveAuditorAware;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableReactiveCassandraAuditing
public class OrderApplication {

    public static final String USER_PRINCIPAL = "user-principal";

    public static void main(String[] args) {
    SpringApplication.run(OrderApplication.class, args);
  }

    @Bean
    ReactiveAuditorAware<String> reactiveAuditorAware() {
        return () -> Mono.just(USER_PRINCIPAL);// security principal,
    }
}
