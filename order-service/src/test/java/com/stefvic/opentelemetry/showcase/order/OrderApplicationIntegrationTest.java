package com.stefvic.opentelemetry.showcase.order;

import static java.util.Objects.requireNonNullElseGet;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.stefvic.opentelemetry.showcase.order.entity.Order;
import com.stefvic.opentelemetry.showcase.order.entity.OrderKey;
import com.stefvic.opentelemetry.showcase.order.repo.OrderRepository;
import com.stefvic.springdata.test.cassandra.EmbeddedCassandra;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(
    properties = {"spring.data.cassandra.schema-action: recreate"},
    webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedCassandra
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Slf4j
class OrderApplicationIntegrationTest {
  private WebTestClient client;
  @LocalServerPort private int port;
  @Autowired private OrderRepository orderRepository;

  @BeforeEach
  void setUp() {
    client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void shouldReturnNonEmptyOrders() {
    var accountId = "acc123";
    var orderId = UUID.randomUUID().toString();
    var item = "item1";
    orderRepository
        .save(new Order(new OrderKey(accountId, orderId)).setItem(item).setNew(true))
        .block();

    this.client
        .get()
        .uri("/order/api/v1/orders")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(APPLICATION_JSON)
        .expectBody()
        .consumeWith(
            body ->
                log.info(
                    "Response body : {}",
                    new String(requireNonNullElseGet(body.getResponseBody(), () -> new byte[0]))))
        .jsonPath("$")
        .isArray()
        .jsonPath("$[0].orderKey.accountId")
        .isEqualTo(accountId)
        .jsonPath("$[0].orderKey.orderId")
        .isEqualTo(orderId)
        .jsonPath("$[0].item")
        .isEqualTo(item)
        .jsonPath("$[0].id")
        .doesNotExist()
        .jsonPath("$[0].new")
        .doesNotExist()
        .jsonPath("$[0].createdBy")
        .isEqualTo(OrderApplication.USER_PRINCIPAL)
        .jsonPath("$[0].createdDate")
        .isNotEmpty()
        .jsonPath("$[0].lastModifiedBy")
        .isEqualTo(OrderApplication.USER_PRINCIPAL)
        .jsonPath("$[0].lastModifiedDate")
        .isNotEmpty()
        .jsonPath("$[1]")
        .doesNotExist();
  }

  @Test
  void shouldSaveOrderBadRequest() {
    this.client
        .post()
        .uri("/order/api/v1/orders")
        .body(BodyInserters.fromValue("{}"))
        .header("Content-Type", "application/json")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(APPLICATION_JSON)
        .expectBody()
        .consumeWith(
            body ->
                log.info(
                    "Response body : {}",
                    new String(requireNonNullElseGet(body.getResponseBody(), () -> new byte[0]))));
  }
}
