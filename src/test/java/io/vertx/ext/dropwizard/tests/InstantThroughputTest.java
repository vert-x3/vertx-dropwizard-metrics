package io.vertx.ext.dropwizard.tests;

import io.vertx.ext.dropwizard.impl.InstantThroughput;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class InstantThroughputTest {

  @Test
  public void testCompute() throws Exception {
    InstantThroughput throughput = new InstantThroughput();
    for (int i = 0;i < 1000;i++) {
      throughput.mark();
      assertEquals(i + 1, throughput.count());
    }
    Thread.sleep(1000);
    assertEquals(1000, throughput.count());
    Thread.sleep(1000);
    assertEquals(0, throughput.count());
  }

  @Test
  public void testPrevCountResetAfterTwoSeconds() throws Exception {
    InstantThroughput throughput = new InstantThroughput();
    // Mark some items so count > 0
    for (int i = 0; i < 50; i++) {
      throughput.mark();
    }

    // Wait 1.5s (between 1-2s): triggers the < TWO_SECS branch -> prevCount = count (50)
    Thread.sleep(1500);
    // count() calls check(): prevCount=50, timestamp reset, count=0. Returns 50.
    assertEquals(50, throughput.count());

    // Wait > 2 seconds from the timestamp reset above: triggers > TWO_SECS branch
    // prevCount is 50 > 0, so it gets reset to 0
    Thread.sleep(2100);
    assertEquals(0, throughput.count());
  }

  @Test
  public void testNoResetWhenPrevCountIsZero() throws Exception {
    InstantThroughput throughput = new InstantThroughput();
    // Mark a few items
    for (int i = 0; i < 5; i++) {
      throughput.mark();
    }

    // Wait 1.5s: triggers < TWO_SECS branch -> prevCount = 5
    Thread.sleep(1500);
    assertEquals(5, throughput.count());

    // Wait > 2s: > TWO_SECS branch, prevCount (5) > 0 -> reset to 0
    Thread.sleep(2100);
    assertEquals(0, throughput.count());

    // Now prevCount is 0; wait > TWO_SECS again
    // prevCount is already 0, so the > TWO_SECS branch should not change it
    Thread.sleep(2100);
    assertEquals(0, throughput.count());
  }

  @Test
  public void testMarkAfterLongPause() throws Exception {
    InstantThroughput throughput = new InstantThroughput();
    // Mark items
    for (int i = 0; i < 100; i++) {
      throughput.mark();
    }

    // Wait 1.5s: triggers < TWO_SECS branch -> prevCount = 100
    Thread.sleep(1500);
    assertEquals(100, throughput.count());

    // Wait > 2s: > TWO_SECS branch, prevCount (100) > 0 -> reset to 0
    Thread.sleep(2100);
    assertEquals(0, throughput.count());

    // Mark new items after the long pause
    for (int i = 0; i < 10; i++) {
      throughput.mark();
    }
    // prevCount is still 0; need to wait for next check cycle to pick up new count
    Thread.sleep(1500);
    assertEquals(10, throughput.count());
  }
}
