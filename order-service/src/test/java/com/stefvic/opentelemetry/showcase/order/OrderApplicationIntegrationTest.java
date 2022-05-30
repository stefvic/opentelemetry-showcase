package com.stefvic.opentelemetry.showcase.order;

import static java.util.Objects.requireNonNullElseGet;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stefvic.opentelemetry.showcase.order.entity.Order;
import com.stefvic.opentelemetry.showcase.order.entity.OrderKey;
import com.stefvic.opentelemetry.showcase.order.repo.OrderRepository;
import com.stefvic.opentelemetry.showcase.order.rest.AccountOrder;
import com.stefvic.springdata.test.cassandra.EmbeddedCassandra;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(
    properties = {
      "spring.data.cassandra.schema-action=recreate",
      "spring.cloud.stream.function.definition=builderOrders;builderOrdersTest",
      "spring.cloud.stream.bindings.builderOrders-out-0.group=orders",
      "spring.cloud.stream.bindings.builderOrders-out-0.destination=builderOrders",
      "spring.cloud.stream.bindings.builderOrdersTest-in-0.group=orders",
      "spring.cloud.stream.bindings.builderOrdersTest-in-0.destination=builderOrders"
    },
    webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedCassandra
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Testcontainers
@Slf4j
class OrderApplicationIntegrationTest {

  private static final int RABBIT_MQ_PORT = 5672;
  private static final int RABBIT_MQ_MGMT_PORT = 15672;
  // will be shared between test methods
  @Container
  private static final RabbitMQContainer RABBIT_MQ =
      new RabbitMQContainer("rabbitmq:management-alpine")
          .withExposedPorts(RABBIT_MQ_PORT, RABBIT_MQ_MGMT_PORT);

  @Autowired private WebTestClient client;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private OrderContainer orderContainer;
  @LocalServerPort private int port;
  @Autowired private OrderRepository orderRepository;

  private static String truncateInstantTextPrecision(String instant) {
    // 2021-05-14T01:56:27Z
    if (instant.length() == "2021-05-14T01:56:27Z".length()) {
      return instant;
    }

    // 2021-05-14T01:56:27.345123Z
    var matcher = Pattern.compile("(.*\\.\\d{3})(\\d+)Z$").matcher(instant);
    if (matcher.matches()) {
      // 2021-05-14T01:56:27.345Z
      return matcher.group(1) + "Z";
    }
    return instant;
  }

  @BeforeAll
  static void beforeAll() {
    var mqHost = RABBIT_MQ.getHost();
    var mqPort = RABBIT_MQ.getMappedPort(RABBIT_MQ_PORT);
    log.info("RabbitMQ: {}:{}", mqHost, mqPort);
    log.info("RabbitMQ mgmt port: {}", RABBIT_MQ.getMappedPort(RABBIT_MQ_MGMT_PORT));

    System.setProperty("spring.rabbitmq.host", "" + mqHost);
    System.setProperty("spring.rabbitmq.port", "" + mqPort);
  }

  @BeforeEach
  void setUp() {
    client =
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .responseTimeout(Duration.ofSeconds(60))
            .build();
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
  void shouldReturnBadRequestOnSaveOrder() {
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

  @Test
  void shouldReturnOkOnSaveOrder() throws Exception {
    var item = "item1";
    var account = "account123";
    EntityExchangeResult<byte[]> result =
        this.client
            .post()
            .uri("/order/api/v1/orders")
            .body(
                BodyInserters.fromValue(
                    AccountOrder.builder().accountId(account).item(item).build()))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectHeader()
            .contentType(APPLICATION_JSON)
            .expectHeader()
            .location("http://localhost:" + port + "/order/api/v1/orders")
            .expectBody()
            .consumeWith(
                body ->
                    log.info(
                        "Response body : {}",
                        new String(
                            requireNonNullElseGet(body.getResponseBody(), () -> new byte[0]))))
            .jsonPath("$")
            .isMap()
            .jsonPath("$.orderKey.accountId")
            .isEqualTo(account)
            .jsonPath("$.orderKey.orderId")
            .isNotEmpty()
            .jsonPath("$.item")
            .isEqualTo(item)
            .jsonPath("$.id")
            .doesNotExist()
            .jsonPath("$.new")
            .doesNotExist()
            .jsonPath("$.createdBy")
            .isEqualTo(OrderApplication.USER_PRINCIPAL)
            .jsonPath("$.createdDate")
            .isNotEmpty()
            .jsonPath("$.lastModifiedBy")
            .isEqualTo(OrderApplication.USER_PRINCIPAL)
            .jsonPath("$.lastModifiedDate")
            .isNotEmpty()
            .returnResult();

    await()
        .atMost(Duration.ofMinutes(1))
        .until(() -> orderContainer.getOrders().size(), is(greaterThan(0)));

    var savedOrder = objectMapper.readTree(result.getResponseBody());
    var savedAccountId = savedOrder.get("orderKey").get("accountId").asText();
    var savedOrderId = savedOrder.get("orderKey").get("orderId").asText();

    // find the above created order
    this.client
        .get()
        .uri("/order/api/v1/orders/{accountId}/{orderId}", savedAccountId, savedOrderId)
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
                    "Response body on find by order key : {}",
                    new String(requireNonNullElseGet(body.getResponseBody(), () -> new byte[0]))))
        .jsonPath("$")
        .isMap()
        .jsonPath("$.orderKey.accountId")
        .isEqualTo(savedAccountId)
        .jsonPath("$.orderKey.orderId")
        .isEqualTo(savedOrderId)
        .jsonPath("$.item")
        .isEqualTo(item)
        .jsonPath("$.id")
        .doesNotExist()
        .jsonPath("$.new")
        .doesNotExist()
        .jsonPath("$.createdBy")
        .isEqualTo(OrderApplication.USER_PRINCIPAL)
        .jsonPath("$.createdDate")
        .isEqualTo(truncateInstantTextPrecision(savedOrder.get("createdDate").asText()))
        .jsonPath("$.lastModifiedBy")
        .isEqualTo(OrderApplication.USER_PRINCIPAL)
        .jsonPath("$.lastModifiedDate")
        .isEqualTo(truncateInstantTextPrecision(savedOrder.get("lastModifiedDate").asText()));
  }

  @TestConfiguration
  static class TestConfig {

    @Bean
    public Function<Flux<Order>, Mono<Void>> builderOrdersTest(OrderContainer orderContainer) {
      return flux -> flux.collect(() -> orderContainer, OrderContainer::add).then();
    }

    @Bean
    public OrderContainer orderContainer() {
      return new OrderContainer();
    }
  }

  @Getter
  @RequiredArgsConstructor
  @Slf4j
  static final class OrderContainer {
    private final List<Order> orders = new CopyOnWriteArrayList<>();

    void add(Order order) {
      log.info("Order received: {}", order);
      orders.add(order);
    }
  }
}
