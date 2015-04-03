package io.vertx.ext.dropwizard;

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
}
