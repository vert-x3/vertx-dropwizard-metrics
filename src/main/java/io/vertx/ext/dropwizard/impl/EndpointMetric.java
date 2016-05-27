package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class EndpointMetric {

  final String name;
  final Timer delay;
  final Counter queued;
  final Counter openConnections;
  final Timer usage;
  final Counter inUse;

  public EndpointMetric(HttpClientReporter reporter, String host, int port) {

    this.name = host + ":" + port;
    this.delay = reporter.timer("endpoint", name, "delay");
    this.queued = reporter.counter("endpoint", name, "queued");
    this.openConnections = reporter.counter("endpoint", name, "open-netsockets");
    this.usage = reporter.timer("endpoint", name, "usage");
    this.inUse = reporter.counter("endpoint", name, "in-use");
  }

  void close(HttpClientReporter reporter) {
    reporter.remove("endpoint", name);
  }
}
