package com.stefvic.opentelemetry.showcase.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.stefvic.opentelemetry.showcase.order.entity.Order;
import com.stefvic.opentelemetry.showcase.order.entity.OrderKey;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.support.GenericMessage;

class BuilderApplicationTest {

  @Test
  void app() throws Exception {
    var oder = new Order(new OrderKey("account123", UUID.randomUUID().toString())).setItem("item1");
    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(
                TestChannelBinderConfiguration.getCompleteConfiguration(BuilderApplication.class))
            .run()) {
      InputDestination source = context.getBean(InputDestination.class);
      CompositeMessageConverter messageConvertor = context.getBean(CompositeMessageConverter.class);
      OutputDestination target = context.getBean(OutputDestination.class);
      source.send(new GenericMessage<>(oder));

      MappingJackson2MessageConverter jacksonConvertor =
          messageConvertor.getConverters().stream()
              .filter(MappingJackson2MessageConverter.class::isInstance)
              .map(MappingJackson2MessageConverter.class::cast)
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("convertor not found"));
      ProcessedOrder received =
          jacksonConvertor
              .getObjectMapper()
              .readValue(target.receive().getPayload(), ProcessedOrder.class);

      assertThat(received).isNotNull();
      assertThat(received.getOrderId()).isEqualTo(oder.getOrderKey().getOrderId());
    }
  }
}
