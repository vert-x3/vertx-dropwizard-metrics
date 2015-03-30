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

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class Registry extends MetricRegistry {

  public void shutdown() {
    removeMatching((name, metric) -> true);
  }

  public Throughput throughput(String name) {
    final Metric metric = getMetrics().get(name);
    if (metric instanceof Throughput) {
      return (Throughput) metric;
    } else if (metric == null) {
      try {
        return register(name, new Throughput());
      } catch (IllegalArgumentException e) {
        final Metric added = getMetrics().get(name);
        if (added instanceof Throughput) {
          return (Throughput) added;
        }
      }
    }
    throw new IllegalArgumentException(name + " is already used for a different type of metric");
  }
}
