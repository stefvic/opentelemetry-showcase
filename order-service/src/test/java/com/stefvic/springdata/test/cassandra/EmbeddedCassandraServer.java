package com.stefvic.springdata.test.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.testcontainers.containers.CassandraContainer;

@Getter
@RequiredArgsConstructor
@ToString
@Slf4j
class EmbeddedCassandraServer implements CloseableResource {

  @NonNull private final String host;
  private final int port;
  @NonNull private final CassandraContainer<?> container;
  @NonNull private final CqlSession session;

  @Override
  public void close() {
    log.info("Cassandra Lifecycle: Stopping Cassandra container");
    session.close();
    container.stop();
    log.info("Cassandra Lifecycle: Cassandra container stopped");
  }
}
