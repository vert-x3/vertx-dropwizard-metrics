package io.vertx.ext.metrics.impl;

import com.codahale.metrics.Timer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RefCountedTimer extends Timer {

  final AtomicInteger count = new AtomicInteger();

}
