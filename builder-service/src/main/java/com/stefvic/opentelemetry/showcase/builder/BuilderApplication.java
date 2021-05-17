package com.stefvic.opentelemetry.showcase.builder;

import com.stefvic.opentelemetry.showcase.order.entity.Order;
import io.opentelemetry.extension.annotations.WithSpan;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@SpringBootApplication(
    exclude = {CassandraReactiveDataAutoConfiguration.class, CassandraDataAutoConfiguration.class})
@Slf4j
public class BuilderApplication {

  public static void main(String[] args) {
    SpringApplication.run(BuilderApplication.class, args);
  }

  @Bean
  public Function<Flux<Order>, Flux<ProcessedOrder>> builderOrderAndNotify() {
    return flux -> flux.map(this::doProcess);
  }

  @WithSpan
  ProcessedOrder doProcess(Order order) {
    var processSeconds = ThreadLocalRandom.current().nextInt(1, 6);
    log.info("Process wait for '{}' seconds on order: {}", processSeconds, order);
    LockSupport.parkNanos(Duration.ofSeconds(processSeconds).toNanos());
    return new ProcessedOrder(UUID.randomUUID().toString(), order.getOrderKey().getOrderId());
  }
}
