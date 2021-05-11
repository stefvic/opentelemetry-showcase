package com.stefvic.opentelemetry.showcase.order.service;

import com.stefvic.springdata.test.cassandra.EmbeddedCassandra;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;

@DataCassandraTest(properties = {"spring.data.cassandra.schema-action: recreate"})
@EmbeddedCassandra
class OrderServiceTest {


}
