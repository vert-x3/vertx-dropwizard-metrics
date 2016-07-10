package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class EndpointMetric {

  final Timer queueDelay;
  final Counter queueSize;
  final Counter openConnections;
  final Timer usage;
  final Timer ttfb;
  final Counter inUse;

  public EndpointMetric(HttpClientReporter reporter, String name) {
    this.queueDelay = reporter.timer("endpoint", name, "queue-delay");
    this.queueSize = reporter.counter("endpoint", name, "queue-size");
    this.openConnections = reporter.counter("endpoint", name, "open-netsockets");
    this.usage = reporter.timer("endpoint", name, "usage");
    this.ttfb = reporter.timer("endpoint", name, "ttfb");
    this.inUse = reporter.counter("endpoint", name, "in-use");
  }
}
