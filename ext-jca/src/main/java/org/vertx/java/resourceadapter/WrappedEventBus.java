/**
 *
 */
package org.vertx.java.resourceadapter;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;

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
   public <T> EventBus send(String address, Object message, Handler<AsyncResult<Message<T>>> replyHandler) {
     this.delegate.send(address, message, replyHandler);
     return this;
   }   
   
   @Override
   public <T> EventBus send(String address, Object message, DeliveryOptions options) {
     this.delegate.send(address, message, options);
     return this;
   }
   
   @Override
   public <T> EventBus send(String address, Object message, DeliveryOptions options, Handler<AsyncResult<Message<T>>> replyHandler) {
     this.delegate.send(address, message, options, replyHandler);
     return this;
   }
   
   @Override
   public EventBus publish(String address, Object message) {
     this.delegate.publish(address, message);
     return this;
   }
   
   @Override
   public EventBus publish(String address, Object message, DeliveryOptions options) {
     this.delegate.publish(address, message, options);
     return this;
   }
   
   @Override
   public <T> MessageConsumer<T> consumer(String address) {
     return this.delegate.consumer(address);
   }
   
   @Override
   public <T> MessageConsumer<T> localConsumer(String address) {
     return this.delegate.localConsumer(address);
   }
   
   @Override
   public <T> WriteStream<T> sender(String address) {
     return this.delegate.sender(address);
   }
   
   @Override
   public <T> WriteStream<T> sender(String address, DeliveryOptions options) {
     return this.delegate.sender(address, options);
   }
   
   @Override
   public <T> WriteStream<T> publisher(String address) {
     return this.delegate.publisher(address);
   }
   
   @Override
   public <T> WriteStream<T> publisher(String address, DeliveryOptions options) {
     return this.delegate.publisher(address, options);
   }
   
   @Override
   public EventBus registerCodec(MessageCodec codec) {
     this.delegate.registerCodec(codec);
     return this;
   }
   
   @Override
   public EventBus unregisterCodec(String name) {
     this.delegate.unregisterCodec(name);
     return this;
   }
   
   @Override
   public <T> EventBus registerDefaultCodec(Class<T> clazz, MessageCodec<T, ?> codec) {
     this.delegate.registerDefaultCodec(clazz, codec);
     return this;
   }
   
   @Override
   public EventBus unregisterDefaultCodec(Class clazz) {
     this.delegate.unregisterDefaultCodec(clazz);
     return this;
   }
  
//   @Override
//   public <T> T createProxy(Class<T> clazz, String address) {
//     return this.delegate.createProxy(clazz, address);
//   }
//   
//   @Override
//   public <T> MessageConsumer registerService(T service, String address) {
//     return this.delegate.registerService(service, address);
//   }
   
   @Override
   public String metricBaseName() {
    return delegate.metricBaseName();
   }
   
   @Override
   public Map<String, JsonObject> metrics() {
     return delegate.metrics();
   }

}
