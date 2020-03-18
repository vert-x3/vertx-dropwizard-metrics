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
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.ThroughputMeter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class EventBusMetricsImpl extends AbstractMetrics implements EventBusMetrics<EventBusMetricsImpl.HandlerMetric> {

  private final HandlerMetric ignoredHandler = new HandlerMetric(null, false, true);
  private final HandlerMetric noMatchHandler = new HandlerMetric(null, true, false);
  private final ConcurrentMap<String, HandlerTimer> handlerTimers = new ConcurrentHashMap<>();
  private final Matcher handlerMatcher;
  private final Counter handlerCount;
  private final Counter pending;
  private final Counter pendingLocal;
  private final Counter pendingRemote;
  private final Counter discarded;
  private final Counter discardedLocal;
  private final Counter discardedRemote;
  private final Meter bytesRead;
  private final Meter bytesWritten;
  private final ThroughputMeter receivedMessages;
  private final ThroughputMeter receivedLocalMessages;
  private final ThroughputMeter receivedRemoteMessages;
  private final ThroughputMeter sentMessages;
  private final ThroughputMeter sentLocalMessages;
  private final ThroughputMeter sentRemoteMessages;
  private final ThroughputMeter publishedMessages;
  private final ThroughputMeter publishedLocalMessages;
  private final ThroughputMeter publishedRemoteMessages;
  private final ThroughputMeter deliveredMessages;
  private final ThroughputMeter deliveredLocalMessages;
  private final ThroughputMeter deliveredRemoteMessages;
  private final Meter replyFailures;

  EventBusMetricsImpl(AbstractMetrics metrics, String baseName, DropwizardMetricsOptions options) {
    super(metrics.registry(), baseName);

    handlerCount = counter("handlers");
    pending = counter("messages", "pending");
    pendingLocal = counter("messages", "pending-local");
    pendingRemote = counter("messages", "pending-remote");
    discarded = counter("messages", "discarded");
    discardedLocal = counter("messages", "discarded-local");
    discardedRemote = counter("messages", "discarded-remote");
    receivedMessages = throughputMeter("messages", "received");
    receivedLocalMessages = throughputMeter("messages", "received-local");
    receivedRemoteMessages = throughputMeter("messages", "received-remote");
    deliveredMessages = throughputMeter("messages", "delivered");
    deliveredLocalMessages = throughputMeter("messages", "delivered-local");
    deliveredRemoteMessages = throughputMeter("messages", "delivered-remote");
    sentMessages = throughputMeter("messages", "sent");
    sentLocalMessages = throughputMeter("messages", "sent-local");
    sentRemoteMessages = throughputMeter("messages", "sent-remote");
    publishedMessages = throughputMeter("messages", "published");
    publishedLocalMessages = throughputMeter("messages", "published-local");
    publishedRemoteMessages = throughputMeter("messages", "published-remote");
    replyFailures = meter("messages", "reply-failures");
    bytesRead = meter("messages", "bytes-read");
    bytesWritten = meter("messages", "bytes-written");
    handlerMatcher = new Matcher(options.getMonitoredEventBusHandlers());
  }

  private static boolean isInternal(String address) {
    return address.startsWith("__vertx.");
  }

  @Override
  public void messageWritten(String address, int size) {
    if (!isInternal(address)) {
      bytesWritten.mark(size);
    }
  }

  @Override
  public void messageRead(String address, int size) {
    if (!isInternal(address)) {
      bytesRead.mark(size);
    }
  }

  @Override
  public void close() {
  }

  @Override
  public HandlerMetric handlerRegistered(String address, String repliedAddress) {
    if (isInternal(address)) {
      return ignoredHandler;
    }
    handlerCount.inc();
    String match = handlerMatcher.matches(address);
    if (match != null) {
      return new HandlerMetric(match, false, false);
    }
    return noMatchHandler;
  }

  @Override
  public void handlerUnregistered(HandlerMetric handler) {
    if (handler.ignored) {
      return;
    }
    handlerCount.dec();
    if (!handler.noMatch) {
      handler.remove();
    }
  }

  @Override
  public void scheduleMessage(HandlerMetric handler, boolean local) {
    if (handler.ignored) {
      return;
    }
    pending.inc();
    if (local) {
      pendingLocal.inc();
      if (!handler.noMatch) {
        handler.pendingLocalCount++;
      }
    } else {
      pendingRemote.inc();
      if (!handler.noMatch) {
        handler.pendingRemoteCount++;
      }
    }
  }

  @Override
  public void discardMessage(HandlerMetric handler, boolean local, Message<?> msg) {
    if (handler.ignored) {
      return;
    }
    pending.dec();
    discarded.inc();
    if (local) {
      discardedLocal.inc();
      pendingLocal.dec();
      if (!handler.noMatch) {
        handler.pendingLocalCount--;
      }
    } else {
      discardedRemote.inc();
      pendingRemote.dec();
      if (!handler.noMatch) {
        handler.pendingRemoteCount--;
      }
    }
  }

  @Override
  public void beginHandleMessage(HandlerMetric handler, boolean local) {
    if (handler.ignored) {
      return;
    }
    pending.dec();
    if (local) {
      pendingLocal.dec();
    } else {
      pendingRemote.dec();
    }
    if (!handler.noMatch) {
      handler.start = System.nanoTime();
      if (local) {
        handler.pendingLocalCount--;
      } else {
        handler.pendingRemoteCount--;
      }
    }
  }

  @Override
  public void endHandleMessage(HandlerMetric handler, Throwable failure) {
    if (!handler.noMatch && !handler.ignored) {
      handler.timer.update(System.nanoTime() - handler.start, TimeUnit.NANOSECONDS);
    }
  }

  @Override
  public void messageSent(String address, boolean publish, boolean local, boolean remote) {
    if (!isInternal(address)) {
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
  }

  @Override
  public void messageReceived(String address, boolean publish, boolean local, int handlers) {
    if (!isInternal(address)) {
      receivedMessages.mark();
      if (local) {
        receivedLocalMessages.mark();
      } else {
        receivedRemoteMessages.mark();
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
  }

  public class HandlerMetric {
    final String address;
    final Timer timer;
    final String name;
    final boolean noMatch;
    final boolean ignored;
    long start;
    long pendingLocalCount;
    long pendingRemoteCount;

    public HandlerMetric(String address, boolean noMatch, boolean ignored) {
      this.address = address;
      this.noMatch = noMatch;
      this.ignored = ignored;
      if (noMatch || ignored) {
        this.timer = null;
        this.name = null;
        return;
      }
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
      if (!noMatch && !ignored) {
        while (true) {
          EventBusMetricsImpl.this.pending.dec(pendingLocalCount + pendingRemoteCount);
          EventBusMetricsImpl.this.pendingLocal.dec(pendingLocalCount);
          EventBusMetricsImpl.this.pendingRemote.dec(pendingRemoteCount);
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
