package io.vertx.ext.dropwizard.it;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.ext.dropwizard.impl.VertxMetricsImpl;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VertxServiceProviderTest {

  @Test
  public void testEnabled() {
    Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(true)));
    VertxMetrics spi = ((VertxInternal) vertx).metrics();
    assertNotNull(spi);
    assertTrue(spi instanceof VertxMetricsImpl);
  }
}
