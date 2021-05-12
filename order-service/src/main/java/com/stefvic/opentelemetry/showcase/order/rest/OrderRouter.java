package com.stefvic.opentelemetry.showcase.order.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OrderRouter {

  // Note: Web flux support @RestController, @ControllerAdvice, @ExceptionHandler too
  // Functional way to export
  @Bean
  public RouterFunction<ServerResponse> route(OrderHandler orderHandler) {
    return RouterFunctions.route()
        .GET("/api/v1/orders", accept(APPLICATION_JSON), orderHandler::findAll)
        .POST("/api/v1/orders", accept(APPLICATION_JSON), orderHandler::createOrder)
        .build();
  }
}
