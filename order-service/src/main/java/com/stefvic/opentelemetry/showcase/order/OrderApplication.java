package com.stefvic.opentelemetry.showcase.order;

import com.stefvic.opentelemetry.showcase.order.entity.Order;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.config.EnableReactiveCassandraAuditing;
import org.springframework.data.domain.ReactiveAuditorAware;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@SpringBootApplication
@EnableReactiveCassandraAuditing
@Slf4j
public class OrderApplication {

  public static final String USER_PRINCIPAL = "user-principal";

  public static void main(String[] args) {
    SpringApplication.run(OrderApplication.class, args);
  }

  @Bean
  ReactiveAuditorAware<String> reactiveAuditorAware() {
    return () -> Mono.just(USER_PRINCIPAL); // security principal,
  }

  @Bean
  @Qualifier("orderProcessor")
  public Many<Order> orderProcessor() {
    log.info("Creating order processor bean");
    return Sinks.many().multicast().onBackpressureBuffer();
  }

  @Bean
  public Supplier<Flux<Order>> builderOrders(
      @Qualifier("orderProcessor") Many<Order> orderProcessor) {
    log.info("Creating build orders out function");
    return orderProcessor::asFlux;
  }
}
