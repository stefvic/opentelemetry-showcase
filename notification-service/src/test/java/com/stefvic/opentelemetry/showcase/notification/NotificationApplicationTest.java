package com.stefvic.opentelemetry.showcase.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import com.stefvic.opentelemetry.showcase.builder.ProcessedOrder;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.support.GenericMessage;

class NotificationApplicationTest {

  @Test
  void app() {
    var processedOrder = new ProcessedOrder("id1", "order1");
    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(
                TestChannelBinderConfiguration.getCompleteConfiguration(
                    NotificationApplication.class))
            .run()) {
      InputDestination source = context.getBean(InputDestination.class);
      NotificationStorage notificationStorage = context.getBean(NotificationStorage.class);
      source.send(new GenericMessage<>(processedOrder));

      await()
          .atMost(Duration.ofMinutes(1))
          .until(() -> notificationStorage.getAll().size(), is(greaterThan(0)));

      assertThat(notificationStorage.getAll()).hasSize(1);
      assertThat(notificationStorage.getAll().get(0))
          .usingRecursiveComparison()
          .isEqualTo(processedOrder);
    }
  }
}
