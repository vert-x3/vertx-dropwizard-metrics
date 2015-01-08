package io.vertx.ext.metrics;

import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.ext.metrics.reporters.EventBusReporter;
import io.vertx.test.core.VertxTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.vertx.test.core.TestUtils.assertNullPointerException;

/**
 * Tests for {@link io.vertx.ext.metrics.reporters.EventBusReporter}.
 */
public class EventBusReporterTest extends VertxTestBase {

  private String knownAddress;
  private String knownMetricBaseName;
  private String knownMetricName;
  private JsonObject knownMetric;
  private HashMap<String, JsonObject> knownMetrics;
  private EventBusReporter reporter;

  @Before
  public void setUpDefaults() throws Exception {
    knownAddress = "foo";
    knownMetricBaseName = "metricBaseName";
    knownMetricName = "metricName";
    knownMetric = new JsonObject().put("some", "content");
    knownMetrics = new HashMap<>();
    knownMetrics.put(knownMetricName, knownMetric);
    Measured knownMeasured = new Measured() {
      @Override
      public String metricBaseName() {
        return knownMetricBaseName;
      }

      @Override
      public Map<String, JsonObject> metrics() {
        return knownMetrics;
      }
    };
    reporter = EventBusReporter.builder(vertx).publishTo(knownAddress).forMeasured(knownMeasured).build();
  }

  @Override
  protected VertxOptions getOptions() {
    return new VertxOptions().setMetricsEnabled(true);
  }

  @Test
  public void testIllegalArguments() throws Exception {
    assertNullPointerException(() -> EventBusReporter.builder(null));
    EventBusReporter.Builder builder = EventBusReporter.builder(vertx);
    assertNullPointerException(builder::build);
  }

  @Test
  public void testPublish() throws Exception {

    MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(knownAddress);
    consumer.handler((message) -> {
      assertEquals(knownMetricName, message.headers().get(EventBusReporter.METRIC_NAME));
      assertEquals(knownMetric, message.body());
      consumer.unregister();
      testComplete();
    });

    reporter.start(1L, TimeUnit.MILLISECONDS);

    await();
  }

  @Test
  public void testClose() throws Exception {

    reporter.start(1L, TimeUnit.MILLISECONDS);
    reporter.close();

    MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(knownAddress);
    consumer.handler((message) -> {
      consumer.unregister();
      fail();
    });

    vertx.setTimer(200, (id) -> testComplete());

    await();
  }
}
