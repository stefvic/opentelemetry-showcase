package com.stefvic.opentelemetry.showcase.notification;

import com.stefvic.opentelemetry.showcase.builder.ProcessedOrder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationStorage {

  private final List<ProcessedOrder> storage = new CopyOnWriteArrayList<>();

  public boolean add(ProcessedOrder p) {
    return storage.add(p);
  }

  public List<ProcessedOrder> getAll() {
    return storage;
  }
}
