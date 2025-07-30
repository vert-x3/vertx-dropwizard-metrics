package io.vertx.ext.dropwizard.tests.impl;

import static org.junit.Assert.assertNotNull;

import com.codahale.metrics.MetricRegistry;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.impl.VertxMetricsImpl;
import org.junit.Test;

/**
 * @author Siarhei.Bahdanchuk Date: 29.07.2025
 */
public class VertxMetricsImplTest {

  @Test(expected = Test.None.class)
  public void testCreatePoolMetricsShouldNotThrowNPE() {
    VertxMetricsImpl vmi = new VertxMetricsImpl(new MetricRegistry(), false,
      new VertxOptions(), new DropwizardMetricsOptions(), "baseName");

    PoolMetrics actual = vmi.createPoolMetrics("http", "poolName", 1);
    assertNotNull(actual);
  }
}
