package io.vertx.ext.metrics;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

@DataObject
public class Counter implements Metric {

  private static final String COUNT_KEY = "count";
  private long count;

  public Counter(long count) {
    setCount(count);
  }

  public Counter(JsonObject json) {
    Objects.requireNonNull(json);
    setCount(json.getLong(COUNT_KEY));
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put(COUNT_KEY, count);
    return json;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Counter counter = (Counter) o;

    if (count != counter.count) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return (int) (count ^ (count >>> 32));
  }
}
