package com.stefvic.opentelemetry.showcase.order.repo;

import com.stefvic.opentelemetry.showcase.order.entity.Order;
import com.stefvic.opentelemetry.showcase.order.entity.OrderKey;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface OrderRepository extends ReactiveCassandraRepository<Order, OrderKey> {}
