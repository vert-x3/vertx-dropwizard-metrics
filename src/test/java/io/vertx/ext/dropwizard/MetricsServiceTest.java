package io.vertx.ext.dropwizard;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

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
    JsonObject metrics = metricsService.getMetricsSnapshot("vertx.timers");
    assertEquals(1, metrics.size());
    assertNotNull(metrics.getJsonObject("vertx.timers"));
  }

  @Test
  public void testMetricsNames() {
    Set<String> names = metricsService.metricsNames();
    assertTrue(names.contains("vertx.timers"));
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
