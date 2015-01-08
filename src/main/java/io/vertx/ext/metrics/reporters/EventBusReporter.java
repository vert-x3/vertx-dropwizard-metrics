package io.vertx.ext.metrics.reporters;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.ext.metrics.ScheduledMetricsConsumer;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

/**
 * Publishes specified metrics to the event bus.
 */
public class EventBusReporter implements Closeable {

  /**
   * Message header containing the name of the metric.
   */
  public static final String METRIC_NAME = "metricName";
  private final EventBus eventBus;
  private final String address;
  private final ScheduledMetricsConsumer metricsConsumer;

  private EventBusReporter(Builder builder) {
    Objects.requireNonNull(builder.address, "No null address accepted");
    Objects.requireNonNull(builder.metricsConsumer, "No null metricsConsumer accepted");
    eventBus = builder.vertx.eventBus();
    address = builder.address;
    metricsConsumer = builder.metricsConsumer;
  }

  /**
   * Starts publishing metrics.
   *
   * @param delay time span between reporting metric snapshots
   * @param unit unit of the time span
   */
  public void start(long delay, TimeUnit unit) {
    metricsConsumer.start(delay, unit, (name, metric) -> eventBus
        .publish(address, metric, new DeliveryOptions().addHeader(METRIC_NAME, name)));
  }

  @Override
  public void close() throws IOException {
    metricsConsumer.stop();
  }

  /**
   * Returns a new {@link io.vertx.ext.metrics.reporters.EventBusReporter.Builder} for
   * {@link io.vertx.ext.metrics.reporters.EventBusReporter}.
   *
   * @param vertx the {@link io.vertx.core.Vertx} instance
   * @return a new {@link io.vertx.ext.metrics.reporters.EventBusReporter.Builder} instance for
   * {@link io.vertx.ext.metrics.reporters.EventBusReporter}
   */
  public static Builder builder(Vertx vertx) {
    return new Builder(vertx);
  }

  /**
   * A builder for {@link io.vertx.ext.metrics.reporters.EventBusReporter} instances. Defaults to report all
   * {@link io.vertx.core.Vertx} metrics.
   */
  public static class Builder {

    private final Vertx vertx;
    private String address;
    private ScheduledMetricsConsumer metricsConsumer;

    private Builder(Vertx vertx) {
      Objects.requireNonNull(vertx, "No null Vertx accepted");
      this.vertx = vertx;
      metricsConsumer = new ScheduledMetricsConsumer(vertx);
    }

    /**
     * Publish the metrics to the specified event bus address.
     *
     * @param address event bus address to publish the metrics
     * @return {@code this}
     */
    public Builder publishTo(String address) {
      this.address = address;
      return this;
    }

    /**
     * Publish only metrics of the specified {@link io.vertx.core.metrics.Measured}.
     *
     * @param measured a {@link io.vertx.core.metrics.Measured} instance as the source for publishing metrics
     * @return {@code this}
     */
    public Builder forMeasured(Measured measured) {
      metricsConsumer = new ScheduledMetricsConsumer(vertx, measured);
      return this;
    }

    /**
     * Filter messages to be published. Can be called multiple times to specify multiple filters.
     *
     * @param filter a filter predicate
     * @return {@code this}
     */
    public Builder withFilter(BiPredicate<String, JsonObject> filter) {
      metricsConsumer.filter(filter);
      return this;
    }

    public EventBusReporter build() {
      return new EventBusReporter(this);
    }
  }
}
