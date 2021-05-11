package com.stefvic.opentelemetry.showcase.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.time.Instant;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@Accessors(chain = true)
@Table
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order implements Persistable<OrderKey> {

  @PrimaryKey
  @NonNull
  @JsonProperty(access = Access.READ_ONLY)
  private final OrderKey orderKey;

  private String item;

  @Transient @JsonIgnore private boolean isNew;

  @CreatedBy
  @JsonProperty(access = Access.READ_ONLY)
  private String createdBy;

  @CreatedDate
  @JsonProperty(access = Access.READ_ONLY)
  private Instant createdDate;

  @LastModifiedBy
  @JsonProperty(access = Access.READ_ONLY)
  private String lastModifiedBy;

  @LastModifiedDate
  @JsonProperty(access = Access.READ_ONLY)
  private Instant lastModifiedDate;

  @Override
  @JsonIgnore
  public OrderKey getId() {
    return getOrderKey();
  }

  @Override
  @JsonIgnore
  public boolean isNew() {
    return isNew;
  }
}
