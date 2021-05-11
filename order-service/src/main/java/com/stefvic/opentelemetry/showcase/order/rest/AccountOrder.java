package com.stefvic.opentelemetry.showcase.order.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountOrder {
  @NotBlank private final String item;
  @NotBlank private final String accountId;
}
