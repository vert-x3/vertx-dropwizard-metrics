package io.vertx.groovy.ext.dropwizard;
public class GroovyExtension {
  public static java.util.Map<String, Object> getMetricsSnapshot(io.vertx.ext.dropwizard.MetricsService j_receiver, io.vertx.core.metrics.Measured measured) {
    return io.vertx.lang.groovy.ConversionHelper.fromJsonObject(j_receiver.getMetricsSnapshot(measured));
  }
  public static java.util.Map<String, Object> getMetricsSnapshot(io.vertx.ext.dropwizard.MetricsService j_receiver, java.lang.String baseName) {
    return io.vertx.lang.groovy.ConversionHelper.fromJsonObject(j_receiver.getMetricsSnapshot(baseName));
  }
}
