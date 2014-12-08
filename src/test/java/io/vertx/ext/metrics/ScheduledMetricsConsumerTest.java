package io.vertx.ext.metrics;

import io.vertx.core.VertxOptions;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.test.core.TestUtils.assertIllegalArgumentException;
import static io.vertx.test.core.TestUtils.assertNullPointerException;

public class ScheduledMetricsConsumerTest extends MetricsTestBase {

  @Override
  protected VertxOptions getOptions() {
    return new VertxOptions().setMetricsEnabled(true);
  }

  @Test
  public void testArgumentValidation() throws Exception {
    assertNullPointerException(() -> new ScheduledMetricsConsumer(null));
    assertNullPointerException(() -> new ScheduledMetricsConsumer(null, vertx));
    assertNullPointerException(() -> new ScheduledMetricsConsumer(vertx, null));

    ScheduledMetricsConsumer consumer = new ScheduledMetricsConsumer(vertx);
    assertNullPointerException(() -> consumer.filter(null));
    assertIllegalArgumentException(() -> consumer.start(0, TimeUnit.SECONDS, (name, metric) -> {}));
    assertIllegalArgumentException(() -> consumer.start(-1, TimeUnit.SECONDS, (name, metric) -> {}));
    assertNullPointerException(() -> consumer.start(1, null, (name, metric) -> {}));
    assertNullPointerException(() -> consumer.start(1, TimeUnit.SECONDS, null));
  }

  @Test
  public void testUnfiltered() throws Exception {
    int messages = 18;
    AtomicInteger count = new AtomicInteger(messages);
    ScheduledMetricsConsumer consumer = new ScheduledMetricsConsumer(vertx);

    consumer.start(300, TimeUnit.MILLISECONDS, (name, metric) -> {
      if (count.get() == 0 && name.equals(vertx.eventBus().metricBaseName() + ".messages.sent")) {
        assertCount(metric, (long) messages);
        testComplete();
      }
    });

    sendMessages(messages, count);

    await();
  }

  @Test
  public void testScheduledMetricConsumer() {
    int messages = 18;
    AtomicInteger count = new AtomicInteger(messages);
    String baseName = vertx.eventBus().metricBaseName();

    ScheduledMetricsConsumer consumer = new ScheduledMetricsConsumer(vertx).filter((name, metric) -> {
      return name.startsWith(baseName);
    });

    consumer.start(300, TimeUnit.MILLISECONDS, (name, metric) -> {
      assertTrue(name.startsWith(baseName));
      if (count.get() == 0) {
        if (name.equals(baseName + ".messages.sent")) {
          assertCount(metric, (long) messages);
          testComplete();
        }
      }
    });

    sendMessages(messages, count);

    await();
  }

  private void sendMessages(int count, AtomicInteger counter) {
    for (int i = 0; i < count; i++) {
      vertx.eventBus().send("foo", "Hello");
      counter.decrementAndGet();
    }
  }

  @Test
  public void testListeningToMultipleMeasured() throws Exception {
    int sentMessages = 5;
    int publishedMessages = 7;
    AtomicInteger sentCount = new AtomicInteger(sentMessages);
    AtomicInteger publishedCount = new AtomicInteger(publishedMessages);
    CountDownLatch latch = new CountDownLatch(2);
    String sentName = vertx.eventBus().metricBaseName() + ".messages.sent";
    String publishedName = vertx.eventBus().metricBaseName() + ".messages.published";
    ScheduledMetricsConsumer consumer = new ScheduledMetricsConsumer(vertx);
    consumer.filter((name, metric) -> name.equals(sentName));
    consumer.filter((name, metric) -> name.equals(publishedName));

    final AtomicInteger receivedSentMessages = new AtomicInteger(0);
    final AtomicInteger receivedPublishedMessages = new AtomicInteger(0);

    consumer.start(300, TimeUnit.MILLISECONDS, (name, metric) -> {
      assertTrue(name.equals(sentName) || name.equals(publishedName));
      if (name.equals(sentName)) {
        receivedSentMessages.set(getCount(metric).intValue());
        latch.countDown();
      }
      if (name.equals(publishedName)) {
        receivedPublishedMessages.set(getCount(metric).intValue());
        latch.countDown();
      }
    });

    sendMessages(sentMessages, sentCount);
    publishMessages(publishedMessages, publishedCount);

    awaitLatch(latch);

    assertEquals(sentMessages, receivedSentMessages.intValue());
    assertEquals(publishedMessages, receivedPublishedMessages.intValue());
  }

  private void publishMessages(int count, AtomicInteger counter) {
    for (int i = 0; i < count; i++) {
      vertx.eventBus().publish("foo", "Hello");
      counter.decrementAndGet();
    }
  }
  
}
