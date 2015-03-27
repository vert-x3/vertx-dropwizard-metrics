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

package io.vertx.ext.metrics.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;
import io.vertx.ext.metrics.HandlerMatcher;
import io.vertx.ext.metrics.MetricsServiceOptions;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class EventBusMetricsImpl extends AbstractMetrics implements EventBusMetrics<EventBusMetricsImpl.HandlerMetric> {

  private final ConcurrentMap<String, HandlerTimer> handlerTimers = new ConcurrentHashMap<>();
  private final Set<String> monitoredHandlers;
  private final Pattern[] matchers;
  private final Counter handlerCount;
  private final Counter pending;
  private final Counter pendingLocal;
  private final Counter pendingRemote;
  private final Meter bytesRead;
  private final Meter bytesWritten;
  private final Meter receivedMessages;
  private final Meter receivedLocalMessages;
  private final Meter receivedRemoteMessages;
  private final Meter sentMessages;
  private final Meter sentLocalMessages;
  private final Meter sentRemoteMessages;
  private final Meter publishedMessages;
  private final Meter publishedLocalMessages;
  private final Meter publishedRemoteMessages;
  private final Meter deliveredMessages;
  private final Meter deliveredLocalMessages;
  private final Meter deliveredRemoteMessages;
  private final Meter replyFailures;


  EventBusMetricsImpl(AbstractMetrics metrics, String baseName, MetricsServiceOptions options) {
    super(metrics.registry(), baseName);

    handlerCount = counter("handlers");
    pending = counter("messages", "pending");
    pendingLocal = counter("messages", "pending-local");
    pendingRemote = counter("messages", "pending-remote");
    receivedMessages = meter("messages", "received");
    receivedLocalMessages = meter("messages", "received-local");
    receivedRemoteMessages = meter("messages", "received-remote");
    deliveredMessages = meter("messages", "delivered");
    deliveredLocalMessages = meter("messages", "delivered-local");
    deliveredRemoteMessages = meter("messages", "delivered-remote");
    sentMessages = meter("messages", "sent");
    sentLocalMessages = meter("messages", "sent-local");
    sentRemoteMessages = meter("messages", "sent-remote");
    publishedMessages = meter("messages", "published");
    publishedLocalMessages = meter("messages", "published-local");
    publishedRemoteMessages = meter("messages", "published-remote");
    replyFailures = meter("messages", "reply-failures");
    bytesRead = meter("messages", "bytes-read");
    bytesWritten = meter("messages", "bytes-written");
    monitoredHandlers = new HashSet<>();
    for (HandlerMatcher matcher : options.getMonitoredHandlers()) {
      if (!matcher.isRegex() && matcher.getAddress() != null) {
        monitoredHandlers.add(matcher.getAddress());
      }
    }
    matchers = options.getMonitoredHandlers().stream().
        filter(matcher -> matcher.isRegex() && matcher.getAddress() != null).
        map(matcher -> Pattern.compile(matcher.getAddress())).
        toArray(Pattern[]::new);
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
    if (monitoredHandlers.size() > 0 && monitoredHandlers.contains(address)) {
      return new HandlerMetric(address);
    }
    if  (matchers.length > 0) {
      for (Pattern pattern : matchers) {
        if (pattern.matcher(address).matches()) {
          return new HandlerMetric(address);
        }
      }
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
