/**
 * 
 */
package org.vertx.java.resourceadapter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Lin Gao <lgao@redhat.com>
 *
 */
@SuppressWarnings("rawtypes")
public class WrappedEventBus implements EventBus
{

   /** The logger */
   private static Logger log = Logger.getLogger(WrappedEventBus.class.getName());
   
   private final EventBus delegate;
   
   public WrappedEventBus(EventBus bus)
   {
      super();
      if (bus == null)
      {
         throw new IllegalArgumentException("EventBus can't be null.");
      }
      this.delegate = bus;
   }

   @Override
   public void close(Handler<AsyncResult<Void>> doneHandler)
   {
      log.log(Level.WARNING, "Won't close the EventBus, it is managed by resource adapter.");
   }

   @Override
   public EventBus send(String address, Object message)
   {
      this.delegate.send(address, message);
      return this;
   }

   
   @Override
   public EventBus send(String address, Object message, Handler<Message> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Object message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus send(String address, JsonObject message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, JsonObject message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, JsonObject message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, JsonArray message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, JsonArray message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
       this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
       return this;
   }

   @Override
   public EventBus send(String address, JsonArray message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Buffer message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Buffer message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Buffer message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, byte[] message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, byte[] message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, byte[] message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, String message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, String message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, String message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Integer message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Integer message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Integer message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Long message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Long message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Long message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Float message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Float message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Float message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Double message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Double message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Double message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Boolean message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Boolean message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Boolean message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Short message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Short message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Short message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Character message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Character message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Character message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public <T> EventBus send(String address, Byte message, Handler<Message<T>> replyHandler)
   {
      this.delegate.send(address, message, replyHandler);
      return this;
   }

   @Override
   public <T> EventBus sendWithTimeout(String address, Byte message, long timeout,
         Handler<AsyncResult<Message<T>>> replyHandler)
   {
      this.delegate.sendWithTimeout(address, message, timeout, replyHandler);
      return this;
   }

   @Override
   public EventBus send(String address, Byte message)
   {
      this.delegate.send(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Object message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, JsonObject message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, JsonArray message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Buffer message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, byte[] message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, String message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Integer message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Long message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Float message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Double message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Boolean message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Short message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Character message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus publish(String address, Byte message)
   {
      this.delegate.publish(address, message);
      return this;
   }

   @Override
   public EventBus unregisterHandler(String address, Handler<? extends Message> handler,
         Handler<AsyncResult<Void>> resultHandler)
   {
      this.delegate.unregisterHandler(address, handler, resultHandler);
      return this;
   }

   @Override
   public EventBus unregisterHandler(String address, Handler<? extends Message> handler)
   {
      this.delegate.unregisterHandler(address, handler);
      return this;
   }

   @Override
   public EventBus registerHandler(String address, Handler<? extends Message> handler,
         Handler<AsyncResult<Void>> resultHandler)
   {
      this.registerHandler(address, handler, resultHandler);
      return this;
   }

   @Override
   public EventBus registerHandler(String address, Handler<? extends Message> handler)
   {
      this.delegate.registerHandler(address, handler);
      return this;
   }

   @Override
   public EventBus registerLocalHandler(String address, Handler<? extends Message> handler)
   {
      this.delegate.registerLocalHandler(address, handler);
      return this;
   }

   @Override
   public EventBus setDefaultReplyTimeout(long timeoutMs)
   {
      this.delegate.setDefaultReplyTimeout(timeoutMs);
      return this;
   }

   @Override
   public long getDefaultReplyTimeout()
   {
      return this.delegate.getDefaultReplyTimeout();
   }

}
