package com.stefvic.opentelemetry.showcase.order.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.io.Serializable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@Data
@Accessors(chain = true)
@PrimaryKeyClass
@RequiredArgsConstructor
public class OrderKey implements Serializable {

  @PrimaryKeyColumn(name = "account_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
  @JsonProperty(access = Access.READ_ONLY)
  private final String accountId;

  @PrimaryKeyColumn(name = "order_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
  @JsonProperty(access = Access.READ_ONLY)
  private final String orderId;
}
