package com.stefvic.opentelemetry.showcase.order.service;

import com.stefvic.opentelemetry.showcase.order.entity.Order;
import com.stefvic.opentelemetry.showcase.order.entity.OrderKey;
import com.stefvic.opentelemetry.showcase.order.repo.OrderRepository;
import com.stefvic.opentelemetry.showcase.order.rest.AccountOrder;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService {
  @NonNull private final OrderRepository orderRepository;

  public Mono<Order> create(@NonNull Mono<AccountOrder> accountOrder) {
    return accountOrder
        .map(
            aOrder ->
                new Order(new OrderKey(aOrder.getAccountId(), UUID.randomUUID().toString()))
                    .setItem(aOrder.getItem())
                    .setNew(true))
        .flatMap(orderRepository::save)
        .doOnNext(order -> log.info("Order created successfully: {}", order));
  }

  public Flux<Order> findAll() {
    return orderRepository.findAll();
  }

  public Mono<Order> findByOrderKey(OrderKey orderKey) {
      return orderRepository.findById(orderKey).single();
  }
}
