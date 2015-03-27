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
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import io.vertx.core.net.SocketAddress;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class DatagramSocketMetricsImpl extends AbstractMetrics implements DatagramSocketMetrics {

  private Counter socketsCounter;
  private Histogram bytesRead;
  private Histogram bytesWritten;
  private Counter exceptions;
  private String serverName;

  DatagramSocketMetricsImpl(AbstractMetrics metrics, String baseName) {
    super(metrics.registry(), baseName);
    socketsCounter = counter("sockets");
    exceptions = counter("exceptions");
    bytesWritten = histogram("bytes-written");
    socketsCounter.inc();
  }

  @Override
  public void close() {
    socketsCounter.dec();
    removeAll();
  }

  @Override
  public void listening(SocketAddress localAddress) {
    serverName = NetServerMetricsImpl.addressName(localAddress);
    bytesRead = histogram(serverName, "bytes-read");
  }

  @Override
  public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    if (bytesRead != null) {
      bytesRead.update(numberOfBytes);
    }
  }

  @Override
  public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    bytesWritten.update(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
    exceptions.inc();
  }
}
