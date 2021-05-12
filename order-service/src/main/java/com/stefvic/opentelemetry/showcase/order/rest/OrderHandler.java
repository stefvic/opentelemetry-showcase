package com.stefvic.opentelemetry.showcase.order.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.stefvic.opentelemetry.showcase.order.entity.Order;
import com.stefvic.opentelemetry.showcase.order.entity.OrderKey;
import com.stefvic.opentelemetry.showcase.order.service.OrderService;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderHandler {

  private final OrderService orderService;
  private final Validator validator;

  private static String toString(Set<? extends ConstraintViolation<?>> constraintViolations) {
    return constraintViolations.stream()
        .map(cv -> cv == null ? "null" : cv.getPropertyPath() + ": " + cv.getMessage())
        .collect(Collectors.joining(", "));
  }

  @NonNull
  public Mono<ServerResponse> createOrder(@NonNull ServerRequest request) {
    Mono<AccountOrder> accountOrder =
        request.bodyToMono(AccountOrder.class).doOnNext(this::validate);
    var orderMono = orderService.create(accountOrder);
    var ordersUri = request.uriBuilder().build();
    return created(ordersUri).contentType(APPLICATION_JSON).body(orderMono, Order.class);
  }

  @NonNull
  public Mono<ServerResponse> findAll(@NonNull ServerRequest request) {
    return ok().contentType(APPLICATION_JSON).body(orderService.findAll(), Order.class);
  }

  @NonNull
  public Mono<ServerResponse> findByOrderKey(@NonNull ServerRequest request) {
    var accountId = request.pathVariable("accountId");
    var orderId = request.pathVariable("orderId");
    return ok().contentType(APPLICATION_JSON)
        .body(orderService.findByOrderKey(new OrderKey(accountId, orderId)), Order.class);
  }

  private <T> void validate(T value) {
    Set<ConstraintViolation<T>> constraints = validator.validate(value);
    if (!constraints.isEmpty()) {
      var violations = toString(constraints);
      log.warn("Constraint violations: {}", violations);
      throw new ServerWebInputException(violations);
    }
  }
}
