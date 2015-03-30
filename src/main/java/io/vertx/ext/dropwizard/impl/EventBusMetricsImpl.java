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
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Throughput;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class EventBusMetricsImpl extends AbstractMetrics implements EventBusMetrics<EventBusMetricsImpl.HandlerMetric> {

  private final ConcurrentMap<String, HandlerTimer> handlerTimers = new ConcurrentHashMap<>();
  private final Matcher handlerMatcher;
  private final Counter handlerCount;
  private final Counter pending;
  private final Counter pendingLocal;
  private final Counter pendingRemote;
  private final Meter bytesRead;
  private final Meter bytesWritten;
  private final Throughput receivedMessages;
  private final Throughput receivedLocalMessages;
  private final Throughput receivedRemoteMessages;
  private final Throughput sentMessages;
  private final Throughput sentLocalMessages;
  private final Throughput sentRemoteMessages;
  private final Throughput publishedMessages;
  private final Throughput publishedLocalMessages;
  private final Throughput publishedRemoteMessages;
  private final Throughput deliveredMessages;
  private final Throughput deliveredLocalMessages;
  private final Throughput deliveredRemoteMessages;
  private final Meter replyFailures;


  EventBusMetricsImpl(AbstractMetrics metrics, String baseName, DropwizardMetricsOptions options) {
    super(metrics.registry(), baseName);

    handlerCount = counter("handlers");
    pending = counter("messages", "pending");
    pendingLocal = counter("messages", "pending-local");
    pendingRemote = counter("messages", "pending-remote");
    receivedMessages = throughput("messages", "received");
    receivedLocalMessages = throughput("messages", "received-local");
    receivedRemoteMessages = throughput("messages", "received-remote");
    deliveredMessages = throughput("messages", "delivered");
    deliveredLocalMessages = throughput("messages", "delivered-local");
    deliveredRemoteMessages = throughput("messages", "delivered-remote");
    sentMessages = throughput("messages", "sent");
    sentLocalMessages = throughput("messages", "sent-local");
    sentRemoteMessages = throughput("messages", "sent-remote");
    publishedMessages = throughput("messages", "published");
    publishedLocalMessages = throughput("messages", "published-local");
    publishedRemoteMessages = throughput("messages", "published-remote");
    replyFailures = meter("messages", "reply-failures");
    bytesRead = meter("messages", "bytes-read");
    bytesWritten = meter("messages", "bytes-written");
    handlerMatcher = new Matcher(options.getMonitoredEventBusHandlers());
  }

  @Override
  public void messageWritten(String address, int size) {
    bytesWritten.mark(size);
  }

  @Override
  public void messageRead(String address, int size) {
    bytesRead.mark(size);
  }

  @Override
  public void close() {
  }

  @Override
  public HandlerMetric handlerRegistered(String address, boolean replyHandler) {
    handlerCount.inc();
    if (handlerMatcher.match(address)) {
      return new HandlerMetric(address);
    }
    return null;
  }

  @Override
  public void handlerUnregistered(HandlerMetric handler) {
    handlerCount.dec();
    if (handler != null) {
      handler.remove();
    }
  }

  @Override
  public void beginHandleMessage(HandlerMetric handler, boolean local) {
    pending.dec();
    if (local) {
      pendingLocal.dec();
    } else {
      pendingRemote.dec();
    }
    if (handler != null) {
      handler.start = System.nanoTime();
    }
  }

  @Override
  public void endHandleMessage(HandlerMetric handler, Throwable failure) {
    if (handler != null) {
      handler.timer.update(System.nanoTime() - handler.start, TimeUnit.NANOSECONDS);
    }
  }

  @Override
  public void messageSent(String address, boolean publish, boolean local, boolean remote) {
    if (publish) {
      publishedMessages.mark();
      if (local) {
        publishedLocalMessages.mark();
      } else {
        publishedRemoteMessages.mark();
      }
    } else {
      sentMessages.mark();
      if (local) {
        sentLocalMessages.mark();
      } else {
        sentRemoteMessages.mark();
      }
    }
  }

  @Override
  public void messageReceived(String address, boolean publish, boolean local, int handlers) {
    pending.inc(handlers);
    receivedMessages.mark();
    if (local) {
      receivedLocalMessages.mark();
      pendingLocal.inc(handlers);
    } else {
      receivedRemoteMessages.mark();
      pendingRemote.inc(handlers);
    }
    if (handlers > 0) {
      deliveredMessages.mark();
      if (local) {
        deliveredLocalMessages.mark();
      } else {
        deliveredRemoteMessages.mark();
      }
    }
  }

  public class HandlerMetric {

    final String address;
    final Timer timer;
    final String name;
    long start;

    public HandlerMetric(String address) {
      this.address = address;
      this.name = nameOf("handlers", address);
      while (true) {
        HandlerTimer existing = handlerTimers.get(address);
        if (existing != null) {
          HandlerTimer next = existing.inc();
          if (handlerTimers.replace(address, existing, next)) {
            timer = next.timer;
            break;
          }
        } else {
          HandlerTimer created = new HandlerTimer();
          if (handlerTimers.putIfAbsent(address, created) == null) {
            registry.register(name, created.timer);
            timer = created.timer;
            break;
          }
        }
      }
    }

    void remove() {
      while (true) {
        HandlerTimer existing = handlerTimers.get(address);
        HandlerTimer next = existing.dec();
        if (next.refCount == 0) {
          if (handlerTimers.remove(address, existing)) {
            registry.remove(name);
            break;
          }
        } else {
          if (handlerTimers.replace(address, existing, next)) {
            break;
          }
        }
      }
    }
  }

  @Override
  public void replyFailure(String address, ReplyFailure failure) {
    replyFailures.mark();
    meter("messages", "reply-failures", failure.name()).mark();
  }

  static class HandlerTimer {
    final int refCount;
    final Timer timer;
    public HandlerTimer(int refCount, Timer timer) {
      this.refCount = refCount;
      this.timer = timer;
    }

    public HandlerTimer() {
      this(1, new Timer());
    }

    HandlerTimer inc() {
      return new HandlerTimer(refCount + 1, timer);
    }

    HandlerTimer dec() {
      return new HandlerTimer(refCount - 1, timer);
    }

    @Override
    public boolean equals(Object obj) {
      HandlerTimer that = (HandlerTimer) obj;
      return refCount == that.refCount;
    }
  }
}
