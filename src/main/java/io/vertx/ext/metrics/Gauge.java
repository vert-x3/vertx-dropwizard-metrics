package io.vertx.ext.metrics;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

@DataObject
public class Gauge<T> implements Metric {

  private static final String VALUE_KEY = "count";
  private T value;

  public Gauge(T value) {
    setValue(value);
  }

  public Gauge(JsonObject json) {
    Objects.requireNonNull(json);
    Objects.requireNonNull(json.getValue(VALUE_KEY));
    setValue((T) json.getValue(VALUE_KEY));
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put(VALUE_KEY, value);
    return json;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    Objects.requireNonNull(value);
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Gauge gauge = (Gauge) o;

    if (!value.equals(gauge.value)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
