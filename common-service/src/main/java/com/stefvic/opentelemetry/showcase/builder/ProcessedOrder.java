package com.stefvic.opentelemetry.showcase.builder;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessedOrder {

  @NonNull private final String processedId;

  @NonNull private final String orderId;

  @JsonCreator
  public ProcessedOrder(
      @JsonProperty("processedId") @NonNull String processedId,
      @JsonProperty("orderId") @NonNull String orderId) {
    this.processedId = requireNonNull(processedId);
    this.orderId = requireNonNull(orderId);
  }

  public @NonNull String getProcessedId() {
    return this.processedId;
  }

  public @NonNull String getOrderId() {
    return this.orderId;
  }
}
