/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.vertx.java.resourceadapter;

import static org.junit.Assert.assertNotNull;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import java.util.UUID;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ConnectorTestCase
 *
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class ConnectorTestCase
{
   private static String deploymentName = "ConnectorTestCase";
   
   private boolean testGetConnectionCompleted = false;
   
   @Before
   public void setUp()
   {
      System.setProperty("vertx.clusterManagerFactory", FakeClusterManagerFactory.class.getName());
   }

   /**
    * Define the deployment
    *
    * @return The deployment archive
    */
   @Deployment
   public static ResourceAdapterArchive createDeployment()
   {
      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, deploymentName + ".rar");
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addClasses(VertxResourceAdapter.class, VertxManagedConnectionFactory.class, VertxManagedConnection.class, 
         VertxConnectionFactory.class, VertxConnectionFactoryImpl.class, VertxConnection.class, VertxConnectionImpl.class);
      raa.addAsLibrary(ja);

      raa.addAsManifestResource("META-INF/ironjacamar.xml", "ironjacamar.xml");
      return raa;
   }

   /** Resource */
   @Resource(mappedName = "java:/eis/VertxConnectionFactory")
   private VertxConnectionFactory connectionFactory;

   @Resource(mappedName = "java:/eis/VertxConnectionFactory")
   private VertxConnectionFactory connectionFactory2;
   
   private Vertx vertx;
   
   /**
    * Test getConnection
    *
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testGetConnection() throws Throwable
   {
//      assertNotNull(connectionFactory);
//      
//      final EventBus eventBus = connectionFactory.getVertxConnection().eventBus();
//      assertNotNull(eventBus);
//      
//      Assert.assertEquals(eventBus.getClass(), WrappedEventBus.class);
//      
//      VertxPlatformConfiguration config = new VertxPlatformConfiguration();
//      config.setClusterHost("localhost");
//      config.setClusterPort(0);
//      
//      // Vertx has started already.
//      VertxPlatformFactory.instance().createVertxIfNotStart(config, new VertxPlatformFactory.VertxListener()
//      {
//         @Override
//         public void whenReady(Vertx vertx)
//         {
//            ConnectorTestCase.this.vertx = vertx;
//         }
//         
//      });
//      
//      TestVertxPlatformManager testPlatformManager = new TestVertxPlatformManager(vertx);
//      testPlatformManager.deployAndRunVerticle(OutboundTestVerticle.class.getName());
//      Handler<Message<String>> msg = h -> {
//                  
//      };
//      
//      eventBus.send("outbound-address", "JCA", new Handler<AsyncResult<Message<String>>>(){
//        
//        @Override
//        public void handle(AsyncResult<Message<String>> event) {
//          
//          String body = event.result().body();
//          try
//          {
//             Assert.assertEquals("Hello JCA from Outbound", body);
//          }
//          finally
//          {
//             testGetConnectionCompleted = true;
//          }
//        }
//        
//      });          
//      while(!testGetConnectionCompleted)
//      {
//         Thread.sleep(1000);
//      }
//      
//      Assert.assertTrue(this.testGetConnectionCompleted);
//      testCompleted();
   }
   
   
   private void testCompleted()
   {
      this.vertx.close();
      this.vertx = null;
   }
   
}
