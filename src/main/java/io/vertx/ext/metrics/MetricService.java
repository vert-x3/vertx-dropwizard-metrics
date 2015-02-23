package io.vertx.ext.metrics;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

@VertxGen
@ProxyGen
public interface MetricService {

  static MetricService create(Vertx vertx) {
    return new MetricServiceImpl(vertx);
  }

  static MetricService createEventBusProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(MetricService.class, vertx, address);
  }

  void report(String name, Metric metric);

}
