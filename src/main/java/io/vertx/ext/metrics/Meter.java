package io.vertx.ext.metrics;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

@DataObject
public class Meter implements Metric {

  private static final String COUNT_KEY = "count";
  private static final String FIFTEEN_MINUTE_RATE_KEY = "fifteen_minute_rate";
  private static final String FIVE_MINUTE_RATE_KEY = "five_minute_rate";
  private static final String ONE_MINUTE_RATE_KEY = "one_minute_rate";
  private static final String MEAN_RATE_KEY = "mean_rate";

  private long count;
  private double fifteenMinuteRate;
  private double fiveMinuteRate;
  private double oneMinuteRate;
  private double meanRate;

  public Meter(long count, double fifteenMinuteRate, double fiveMinuteRate, double oneMinuteRate, double meanRate) {
    setCount(count);
    setFifteenMinuteRate(fifteenMinuteRate);
    setFiveMinuteRate(fiveMinuteRate);
    setOneMinuteRate(oneMinuteRate);
    setMeanRate(meanRate);
  }

  public Meter(JsonObject json) {
    Objects.requireNonNull(json);
    setCount(json.getLong(COUNT_KEY));
    setFifteenMinuteRate(json.getDouble(FIFTEEN_MINUTE_RATE_KEY));
    setFiveMinuteRate(json.getDouble(FIVE_MINUTE_RATE_KEY));
    setOneMinuteRate(json.getDouble(ONE_MINUTE_RATE_KEY));
    setMeanRate(json.getDouble(MEAN_RATE_KEY));
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put(COUNT_KEY, count);
    json.put(FIFTEEN_MINUTE_RATE_KEY, fifteenMinuteRate);
    json.put(FIVE_MINUTE_RATE_KEY, fiveMinuteRate);
    json.put(ONE_MINUTE_RATE_KEY, oneMinuteRate);
    json.put(MEAN_RATE_KEY, meanRate);
    return json;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public double getFifteenMinuteRate() {
    return fifteenMinuteRate;
  }

  public void setFifteenMinuteRate(double fifteenMinuteRate) {
    this.fifteenMinuteRate = fifteenMinuteRate;
  }

  public double getFiveMinuteRate() {
    return fiveMinuteRate;
  }

  public void setFiveMinuteRate(double fiveMinuteRate) {
    this.fiveMinuteRate = fiveMinuteRate;
  }

  public double getOneMinuteRate() {
    return oneMinuteRate;
  }

  public void setOneMinuteRate(double oneMinuteRate) {
    this.oneMinuteRate = oneMinuteRate;
  }

  public double getMeanRate() {
    return meanRate;
  }

  public void setMeanRate(double meanRate) {
    this.meanRate = meanRate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Meter meter = (Meter) o;

    if (count != meter.count) return false;
    if (Double.compare(meter.fifteenMinuteRate, fifteenMinuteRate) != 0) return false;
    if (Double.compare(meter.fiveMinuteRate, fiveMinuteRate) != 0) return false;
    if (Double.compare(meter.meanRate, meanRate) != 0) return false;
    if (Double.compare(meter.oneMinuteRate, oneMinuteRate) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = (int) (count ^ (count >>> 32));
    temp = Double.doubleToLongBits(fifteenMinuteRate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(fiveMinuteRate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(oneMinuteRate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(meanRate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
