package io.vertx.ext.dropwizard.tests;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.MetricsService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MetricsServiceTest extends MetricsTestBase {

  private MetricsService metricsService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    metricsService = MetricsService.create(vertx);
  }

  @Override
  protected VertxOptions getOptions() {
    return new VertxOptions().setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
  }

  @Test
  public void testFindByBaseName() {
    JsonObject metrics = metricsService.getMetricsSnapshot("vertx.event-loop-size");
    assertEquals(1, metrics.size());
    Assert.assertNotNull(metrics.getJsonObject("vertx.event-loop-size"));
  }

  @Test
  public void testMetricsNames() {
    Set<String> names = metricsService.metricsNames();
    assertTrue(names.contains("vertx.event-loop-size"));
    assertTrue(names.stream().filter(name -> name.startsWith("vertx.eventbus")).count() > 0);
  }

  @Test
  public void testMeasuredSnapshotName() {
    JsonObject metrics = metricsService.getMetricsSnapshot(vertx);
    assertFalse(metrics.isEmpty());
    for (Map.Entry<String, Object> entry : metrics) {
      assertTrue(entry.getKey().startsWith("vertx."));
    }
    metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    assertFalse(metrics.isEmpty());
    for (Map.Entry<String, Object> entry : metrics) {
      assertFalse(entry.getKey().startsWith("vertx.eventbus."));
    }
  }
}
