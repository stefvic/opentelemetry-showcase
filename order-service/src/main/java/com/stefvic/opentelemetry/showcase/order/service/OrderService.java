package com.stefvic.opentelemetry.showcase.order.service;

import com.stefvic.opentelemetry.showcase.order.entity.Order;
import com.stefvic.opentelemetry.showcase.order.entity.OrderKey;
import com.stefvic.opentelemetry.showcase.order.repo.OrderRepository;
import com.stefvic.opentelemetry.showcase.order.rest.AccountOrder;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService {

  private static final String ORDER_ID_ATTR_KEY = "order_id";

  @NonNull
  private final @Qualifier("orderProcessor") Sinks.Many<Order> orderProcessor;

  @NonNull private final OrderRepository orderRepository;
  @NonNull private final StreamBridge streamBridge;

  public Mono<Order> create(@NonNull Mono<AccountOrder> accountOrder) {
    return accountOrder
        .map(
            aOrder ->
                new Order(new OrderKey(aOrder.getAccountId(), UUID.randomUUID().toString()))
                    .setItem(aOrder.getItem())
                    .setNew(true))
        .flatMap(orderRepository::save)
        .doOnNext(
            order -> {
              log.info("Order created successfully: {}", order);

              // add span attribute and event
              var currentSpan = Span.current();
              var orderId = order.getOrderKey().getOrderId();
              currentSpan.setAttribute(ORDER_ID_ATTR_KEY, orderId);
              var orderAttr = Attributes.builder().put(ORDER_ID_ATTR_KEY, orderId).build();
              currentSpan.addEvent("order_saved", orderAttr);

              // send the order downstream
              var emitResult = orderProcessor.tryEmitNext(order);
              if (emitResult.isFailure()) {
                currentSpan.addEvent("order_sent_fail", orderAttr);
                emitResult.orThrow();
              }

              // let's add one more event to span
              currentSpan.addEvent("order_sent_success", orderAttr);
              log.info("Build order message sent");
            });
  }

  public Flux<Order> findAll() {
    return orderRepository.findAll();
  }

  public Mono<Order> findByOrderKey(OrderKey orderKey) {
    return orderRepository.findById(orderKey).single();
  }
}
