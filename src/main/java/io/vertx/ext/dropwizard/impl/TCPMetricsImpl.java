/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.TCPMetrics;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class TCPMetricsImpl extends AbstractMetrics implements TCPMetrics<Long> {

  private Counter openConnections;
  private Timer connections;
  private Counter bytesRead;
  private Counter bytesWritten;
  private Counter exceptions;
  protected volatile boolean closed;

  TCPMetricsImpl(MetricRegistry registry, String baseName) {
    super(registry, baseName);

    this.openConnections = counter("open-netsockets");
    this.connections = timer("connections");
    this.exceptions = counter("exceptions");
    this.bytesRead = counter("bytes-read");
    this.bytesWritten = counter("bytes-written");
  }

  @Override
  public void close() {
    this.closed = true;
    removeAll();
  }

  @Override
  public Long connected(SocketAddress remoteAddress, String remoteName) {
    // Connection metrics
    openConnections.inc();

    // On network outage the remoteAddress can be null.
    // Do not report the open-connections when it's null
    if (remoteAddress != null) {
      // Remote address connection metrics
      counter("open-connections", remoteAddress.host()).inc();

    }

    // A little clunky, but it's possible we got here after closed has been called
    if (closed) {
      removeAll();
    }

    return System.nanoTime();
  }

  @Override
  public void disconnected(Long ctx, SocketAddress remoteAddress) {
    openConnections.dec();
    connections.update(System.nanoTime() - ctx, TimeUnit.NANOSECONDS);

    // On network outage the remoteAddress can be null.
    // Do not report the open-connections when it's null
    if (remoteAddress != null) {
      // Remote address connection metrics
      Counter counter = counter("open-connections", remoteAddress.host());
      counter.dec();
      if (counter.getCount() == 0) {
        remove("open-connections", remoteAddress.host());
      }
    }

    // A little clunky, but it's possible we got here after closed has been called
    if (closed) {
      removeAll();
    }
  }

  @Override
  public void bytesRead(Long socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    if (numberOfBytes > 0L) {
      bytesRead.inc(numberOfBytes);
    }
  }

  @Override
  public void bytesWritten(Long socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    if (numberOfBytes > 0L) {
      bytesWritten.inc(numberOfBytes);
    }
  }

  @Override
  public void exceptionOccurred(Long socketMetric, SocketAddress remoteAddress, Throwable t) {
    exceptions.inc();
  }

  protected long connections() {
    if (openConnections == null) return 0;

    return openConnections.getCount();
  }

  static String addressName(SocketAddress address) {
    if (address == null) return null;

    return address.host() + ":" + address.port();
  }
}
