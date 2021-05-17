package com.stefvic.opentelemetry.showcase.notification;

import com.stefvic.opentelemetry.showcase.builder.ProcessedOrder;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.extension.annotations.WithSpan;
import java.time.Duration;
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
import reactor.core.publisher.Mono;

@SpringBootApplication(
    exclude = {CassandraReactiveDataAutoConfiguration.class, CassandraDataAutoConfiguration.class})
@Slf4j
public class NotificationApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotificationApplication.class, args);
  }

  @Bean
  public NotificationStorage notificationStorage() {
    return new NotificationStorage();
  }

  @Bean
  public Function<Flux<ProcessedOrder>, Mono<Void>> orderNotify(
      NotificationStorage notificationStorage) {
    return flux ->
        flux.doOnNext(this::doNotify)
            .collect(() -> notificationStorage, NotificationStorage::add)
            .then();
  }

  @WithSpan
  void doNotify(ProcessedOrder processedOrder) {
    var processSeconds = ThreadLocalRandom.current().nextInt(1, 6);
    log.info("Notify wait for '{}' seconds on order: {}", processSeconds, processedOrder);
    LockSupport.parkNanos(Duration.ofSeconds(processSeconds).toNanos());

    Span.current()
        .addEvent(
            "order_notified",
            Attributes.builder()
                .put("processedId", processedOrder.getProcessedId())
                .put("orderId", processedOrder.getOrderId())
                .build());
  }
}
