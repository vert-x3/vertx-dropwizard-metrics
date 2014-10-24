package org.vertx.java.resourceadapter;

import org.vertx.java.core.impl.DefaultVertx;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.VerticleFactory;
import org.vertx.java.platform.impl.java.JavaVerticleFactory;


public class TestVertxPlatformManager {
   
   private final DefaultVertx vertx;
   
   public TestVertxPlatformManager(DefaultVertx vertx)
   {
      this.vertx = vertx;
      
   }
   
   public void deployAndRunVerticle(String main) throws Exception
   {
      VerticleFactory factory = new JavaVerticleFactory();
      factory.init(vertx, null, Thread.currentThread().getContextClassLoader());
      Verticle verticle = factory.createVerticle(main);
      verticle.start();
   }

 }