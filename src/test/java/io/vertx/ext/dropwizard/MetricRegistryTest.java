package io.vertx.ext.dropwizard;

import com.codahale.metrics.MetricRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.VertxInternal;
import io.vertx.ext.dropwizard.impl.VertxMetricsImpl;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author <a href="mailto:bartosz.jk.wozniak@gmail.com">Bartosz Woźniak</a>
 */
public class MetricRegistryTest {

  private VertxOptions getOptionsWithoutSetMetricRegistry() {
    return new VertxOptions().setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
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
    MetricRegistry metricRegistry = new MetricRegistry();
    Vertx vertx = Vertx.builder()
      .with(getOptionsWithoutSetMetricRegistry())
      .withMetrics(new DropwizardVertxMetricsFactory(metricRegistry))
      .build();
    try {
      assertNotNull(vertx);
      VertxMetricsImpl metrics = (VertxMetricsImpl) ((VertxInternal)vertx).metricsSPI();
      assertSame(metricRegistry, metrics.registry());
    } finally {
      vertx.close();
    }
  }

  @Test
  public void testMetricServiceWithSetMetricRegistryOption(){
    MetricRegistry metricRegistry = new MetricRegistry();
    Vertx vertx = Vertx.builder()
      .with(getOptionsWithoutSetMetricRegistry())
      .withMetrics(new DropwizardVertxMetricsFactory(metricRegistry))
      .build();
    try {
      MetricsService metricsService = MetricsService.create(vertx);
      assertTrue(metricsService.metricsNames().size()>0);
    } finally {
      vertx.close();
    }
  }
}
