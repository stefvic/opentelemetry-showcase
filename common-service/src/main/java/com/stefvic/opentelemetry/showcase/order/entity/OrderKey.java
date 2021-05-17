package com.stefvic.opentelemetry.showcase.order.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@Data
@Accessors(chain = true)
@PrimaryKeyClass
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderKey implements Serializable {

  @PrimaryKeyColumn(name = "account_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
  @JsonProperty("accountId")
  @NonNull
  private final String accountId;

  @PrimaryKeyColumn(name = "order_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
  @JsonProperty("orderId")
  @NonNull
  private final String orderId;
}
