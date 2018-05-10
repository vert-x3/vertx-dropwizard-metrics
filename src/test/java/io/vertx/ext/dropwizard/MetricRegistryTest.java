package io.vertx.ext.dropwizard;

import com.codahale.metrics.MetricRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author <a href="mailto:bartosz.jk.wozniak@gmail.com">Bartosz Woźniak</a>
 */
public class MetricRegistryTest {

  private VertxOptions getOptionsWithoutSetMetricRegistry() {
    return new VertxOptions().setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
  }

  private VertxOptions getOptionsWithSetMetricRegistry() {
    MetricRegistry metricRegistry = new MetricRegistry();
    return new VertxOptions().setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true).setMetricRegistry(metricRegistry));
  }

  @Test
  public void testVertxWithoutSetMetricRegistryOption() {
    Vertx vertx = Vertx.vertx(getOptionsWithoutSetMetricRegistry());
    assertNotNull(vertx);
    vertx.close();
  }

  @Test
  public void testMetricServiceWithoutSetMetricRegistryOption(){
    Vertx vertx = Vertx.vertx(getOptionsWithoutSetMetricRegistry());
    MetricsService metricsService = MetricsService.create(vertx);
    assertTrue(metricsService.metricsNames().size()>0);
  }

  @Test
  public void testVertxWithSetMetricRegistryOption() {
    Vertx vertx = Vertx.vertx(getOptionsWithSetMetricRegistry());
    assertNotNull(vertx);
    vertx.close();
  }

  @Test
  public void testMetricServiceWithSetMetricRegistryOption(){
    Vertx vertx = Vertx.vertx(getOptionsWithSetMetricRegistry());
    MetricsService metricsService = MetricsService.create(vertx);
    assertTrue(metricsService.metricsNames().size()>0);
  }
}
