package com.stefvic.springdata.test.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.CassandraContainer;

@Slf4j
final class EmbeddedCassandraLifecycle {

  static final String KEYSPACE = "test";
  static final String DATACENTER = "datacenter1";
  private static final String LOCAL_ONE = "local_one";
  private static final String CASSANDRA_MAX_WAIT_MIN_ENV = "CASSANDRA_MAX_WAIT_MIN";
  private static final String CASSANDRA_VERSION_ENV = "CASSANDRA_VERSION";
  private static final int DEFAULT_WAIT_MIN = 3;
  private static final int CASSANDRA_PORT = 9042;

  private EmbeddedCassandraLifecycle() {
    throw new UnsupportedOperationException("No instance");
  }

  private static void createTestKeyspace(CqlSession session) {
    session.execute(
        String.format(
            "CREATE KEYSPACE IF NOT EXISTS %s \n"
                + "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };",
            KEYSPACE));
    log.info("Cassandra Lifecycle: Embedded Cassandra keyspace created: {}", KEYSPACE);
  }

  static EmbeddedCassandraServer start() {
    try {
      log.info("Cassandra Lifecycle: Starting Embedded Cassandra server ...");
      var container = runCassandraContainer();
      var port = container.getMappedPort(CASSANDRA_PORT);
      var host = container.getHost();
      // spring data cassandra overwrite
      System.setProperty("spring.data.cassandra.port", String.valueOf(port));
      System.setProperty("spring.data.cassandra.contact-points", "" + host);
      System.setProperty("spring.data.cassandra.local-datacenter", DATACENTER);
      System.setProperty("spring.data.cassandra.keyspace-name", KEYSPACE);
      System.setProperty("spring.data.cassandra.request.consistency", LOCAL_ONE);
      System.setProperty("spring.data.cassandra.request.serial-consistency", LOCAL_ONE);

      awaitCassandraSessionInit(port, host);

      CqlSession session = cqlSessionFactory(port, host).call();
      createTestKeyspace(session);

      log.info(
          "Cassandra Lifecycle: Embedded Cassandra server'{}:{}' started successfully.",
          host,
          port);
      return new EmbeddedCassandraServer(host, port, container, session);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void awaitCassandraSessionInit(Integer port, String host) {
    // the cassandra port should be already available
    Callable<CqlSession> sessionFactory = cqlSessionFactory(port, host);
    Awaitility.await()
        .ignoreExceptions()
        .atMost(Duration.ofMinutes(1))
        .untilAsserted(() -> sessionFactory.call().close());
  }

  private static Integer getWaitMin() {
    return Optional.ofNullable(System.getenv(CASSANDRA_MAX_WAIT_MIN_ENV))
        .filter(StringUtils::hasText)
        .map(Integer::parseInt)
        .orElse(DEFAULT_WAIT_MIN);
  }

  private static Callable<CqlSession> cqlSessionFactory(Integer port, String host) {
    return () ->
        CqlSession.builder()
            .addContactPoint(new InetSocketAddress(host, port))
            .withLocalDatacenter(DATACENTER)
            .build();
  }

  private static CassandraContainer<?> runCassandraContainer() {

    var container = new CassandraContainer<>(getCassandraDockerImageName());
    container.withExposedPorts(CASSANDRA_PORT).withStartupTimeout(Duration.ofMinutes(getWaitMin()));
    container.start();
    return container;
  }

  private static String getCassandraDockerImageName() {
    return "cassandra:tag"
        .replaceFirst(
            "tag",
            Optional.ofNullable(System.getenv(CASSANDRA_VERSION_ENV))
                .filter(StringUtils::hasText)
                .orElse("4.0"));
  }
}
