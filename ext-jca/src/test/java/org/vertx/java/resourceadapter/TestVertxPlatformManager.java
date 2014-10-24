package org.vertx.java.resourceadapter;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.impl.JavaVerticleFactory;
import io.vertx.core.spi.VerticleFactory;


public class TestVertxPlatformManager {
   
   private final Vertx vertx;
   
   public TestVertxPlatformManager(Vertx vertx)
   {
      this.vertx = vertx;
      
   }
   
   public void deployAndRunVerticle(String main) throws Exception
   {
      VerticleFactory factory = new JavaVerticleFactory();
      factory.init(vertx);
      Verticle verticle = factory.createVerticle(main, Thread.currentThread().getContextClassLoader());
      verticle.start(null);
   }

 }