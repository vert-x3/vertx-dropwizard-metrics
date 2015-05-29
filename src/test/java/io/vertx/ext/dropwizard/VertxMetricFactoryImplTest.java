package io.vertx.ext.dropwizard;

import io.vertx.core.VertxOptions;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.ext.dropwizard.impl.VertxMetricsFactoryImpl;
import io.vertx.test.core.VertxTestBase;
import org.junit.After;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:john.warner@ef.com">John Warner</a>
 */
public class VertxMetricFactoryImplTest extends VertxTestBase {

  private VertxMetrics metrics;

  @After
  public void after() throws Exception {
    if (metrics != null) {
      metrics.close();
      metrics = null;
    }
  }

  @Test
  public void testLoadingFromFile() throws Exception {
    String filePath = ClassLoader.getSystemResource("test_metrics_config.json").getFile();
    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions().setConfigFileName(filePath);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    // Verify our jmx domain isn't there already, just in case.
    assertFalse(Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains()).contains("test-jmx-domain"));

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    // If our file was loaded correctly, then our jmx domain will exist.
    assertTrue(jmxDomains.contains("test-jmx-domain"));
  }

  @Test
  public void testloadingFileFromClasspath() throws Exception {
    String fileName = "test_metrics_config.json";
    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions().setConfigFileName(fileName);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    // Verify our jmx domain isn't there already, just in case.
    assertFalse(Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains()).contains("test-jmx-domain"));

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    // If our file was loaded correctly, then our jmx domain will exist.
    assertTrue(jmxDomains.contains("test-jmx-domain"));
  }

  @Test
  public void testLoadingWithJmxDisabled() throws Exception {
    String filePath = ClassLoader.getSystemResource("test_metrics_config_jmx_disabled.json").getFile();
    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions().setConfigFileName(filePath);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    // If our file was loaded correctly, then our jmx domain will exist.
    assertFalse(jmxDomains.contains("test-jmx-domain"));
  }

  @Test
  public void testWithNoConfigFile() throws Exception {
    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions()
        .setJmxEnabled(true)
        .setJmxDomain("non-file-jmx");
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    assertFalse(jmxDomains.contains("test-jmx-domain"));
    assertTrue(jmxDomains.contains("non-file-jmx"));
  }

  @Test
  public void testLoadingWithMissingFile() throws Exception {
    String filePath = "/i/am/not/here/missingfile.json";

    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions()
        .setJmxEnabled(true)
        .setJmxDomain("non-file-jmx")
        .setConfigFileName(filePath);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    assertFalse(jmxDomains.contains("test-jmx-domain"));
    assertTrue(jmxDomains.contains("non-file-jmx"));
  }
}
