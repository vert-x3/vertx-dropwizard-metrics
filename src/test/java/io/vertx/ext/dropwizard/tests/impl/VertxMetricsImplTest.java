package io.vertx.ext.dropwizard.tests.impl;

import com.codahale.metrics.MetricRegistry;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.impl.VertxMetricsImpl;
import org.junit.Test;

/**
 * @author Siarhei.Bahdanchuk Date: 29.07.2025
 */
public class VertxMetricsImplTest {

  @Test(expected = Test.None.class)
  public void testCreatePoolMetrics() {
    VertxMetricsImpl vmi = new VertxMetricsImpl(new MetricRegistry(), false,
      new VertxOptions(), new DropwizardMetricsOptions(), "baseName");

    vmi.createPoolMetrics("http", "poolName", 1);
  }
}
