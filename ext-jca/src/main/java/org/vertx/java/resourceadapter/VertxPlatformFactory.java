/**
 *
 */
package org.vertx.java.resourceadapter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.VertxFactory;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.spi.cluster.impl.hazelcast.HazelcastClusterManager;


import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A singleton factory to start a clustered Vert.x platform.
 *
 * One clusterPort/clusterHost pair matches one Vert.x platform.
 *
 * @author Lin Gao <lgao@redhat.com>
 *
 */
public class VertxPlatformFactory
{

   private static Logger log = Logger.getLogger(VertxPlatformFactory.class.getName());

   private static VertxPlatformFactory INSTANCE = new VertxPlatformFactory();

   public static VertxPlatformFactory instance()
   {
      return INSTANCE;
   }

   /**
    * All Vert.x platforms.
    *
    */
   private ConcurrentHashMap<String, Vertx> vertxPlatforms = new ConcurrentHashMap<String, Vertx>();

   /**
    * All Vert.x holders
    */
   private ConcurrentHashSet<VertxHolder> vertxHolders = new ConcurrentHashSet<VertxHolder>();


   private Lock lock = new ReentrantLock();

   private Lock holderLock = new ReentrantLock();

   /**
    * Default private constructor
    */
   private VertxPlatformFactory(){

   }

   /**
    * Creates a Vertx if one is not started yet.
    *
    * @param config the configuration to start a vertx
    * @param lifecyleListener the vertx lifecycle listener
    */
   public void createVertxIfNotStart(final VertxPlatformConfiguration config, final VertxListener lifecyleListener)
   {
      lock.lock();
      Vertx vertx = this.vertxPlatforms.get(config.getVertxPlatformIdentifier());
      if (vertx != null)
      {
         lock.unlock();
         log.log(Level.INFO, "Vert.x platform at: " + config.getVertxPlatformIdentifier() + " has been started.");
         lifecyleListener.whenReady(vertx);
         return;
      }
      try
      {
         Integer clusterPort = config.getClusterPort();
         String clusterHost = config.getClusterHost();

         log.log(Level.INFO, "Starts a Vert.x platform at: " + config.getVertxPlatformIdentifier());

         // either the default-cluster.xml in classpath, or the cluster xml file specified by config.getClusterConfigFile()
         Config hazelcastCfg = loadHazelcastConfig(config);
         HazelcastClusterManager manger = new HazelcastClusterManager();
         manger.setConfig(hazelcastCfg);

         final CountDownLatch vertxStartCount = new CountDownLatch(1);
         VertxOptions options = new VertxOptions();
         options.setClusterHost(clusterHost);
         options.setClusterPort(clusterPort);
         Vertx.vertxAsync(options, new Handler<AsyncResult<Vertx>>()
               {
                  @Override
                  public void handle(final AsyncResult<Vertx> result)
                  {
                     try
                     {
                        if (result.succeeded())
                        {
                           log.log(Level.INFO, "Vert.x Platform at: " + config.getVertxPlatformIdentifier() + " Started Successfully.");
                           vertxPlatforms.putIfAbsent(config.getVertxPlatformIdentifier(), result.result());
                           lifecyleListener.whenReady(result.result());
                        }
                        else if (result.failed())
                        {
                           log.log(Level.SEVERE, "Failed to start Vert.x at: " + config.getVertxPlatformIdentifier());
                           Throwable cause = result.cause();
                           if (cause != null)
                           {
                              throw new RuntimeException(cause);
                           }
                        }
                     }
                     finally
                     {
                        vertxStartCount.countDown();
                     }
                  }
               });
         vertxStartCount.await(); // waiting for the vertx starts up.
      }
      catch(Exception exp)
      {
         throw new RuntimeException(exp);
      }
      finally
      {
         lock.unlock();
      }
   }

   private Config loadHazelcastConfig(VertxPlatformConfiguration config) throws IOException
   {
      String clusterConfigFile = config.getClusterConfigFile();
      InputStream is = null;
      try
      {
         if (clusterConfigFile != null && clusterConfigFile.length() > 0)
         {
            clusterConfigFile = SecurityActions.getExpressValue(clusterConfigFile);
            is = new FileInputStream(clusterConfigFile);
         }
         else
         {
            // we only ship one default-cluster.xml
            is = getClass().getClassLoader().getResourceAsStream("default-cluster.xml");
         }
         return new XmlConfigBuilder(is).build();
      }
      finally
      {
         if (is != null)
         {
            is.close();
         }
      }
   }

   /**
    * Adds VertxHolder to be recorded.
    *
    * @param holder the VertxHolder
    */
   public void addVertxHolder(VertxHolder holder)
   {
      holderLock.lock();
      try
      {
         Vertx vertx = holder.getVertx();
         if (vertxPlatforms.containsValue(vertx))
         {
            if (!this.vertxHolders.contains(holder))
            {
               log.log(Level.INFO, "Adding Vertx Holder: " + holder.toString());
               this.vertxHolders.add(holder);
            }
            else
            {
               log.log(Level.WARNING, "Vertx Holder: " + holder.toString() + " has been added already.");
            }
         }
         else
         {
            log.log(Level.SEVERE, "Vertx Holder: " + holder.toString() + " is out of management.");
         }
      }
      finally
      {
         holderLock.unlock();
      }
   }

   /**
    * Removes the VertxHolder from recorded.
    *
    * @param holder the VertxHolder
    */
   public void removeVertxHolder(VertxHolder holder)
   {
      holderLock.lock();
      try
      {
         if (this.vertxHolders.contains(holder))
         {
            log.log(Level.INFO, "Removing Vertx Holder: " + holder.toString());
            this.vertxHolders.remove(holder);
         }
         else
         {
            log.log(Level.SEVERE, "Vertx Holder: " + holder.toString() + " is out of management.");
         }
      }
      finally
      {
         holderLock.unlock();
      }
   }

   /**
    * Stops the Vert.x Platform Manager and removes it from cache.
    *
    * @param config
    */
   public void stopPlatformManager(VertxPlatformConfiguration config)
   {
      lock.lock();
      try
      {
         Vertx vertx = this.vertxPlatforms.get(config.getVertxPlatformIdentifier());
         if (vertx != null)
         {
            if (isVertxHolded(vertx))
            {
               log.log(Level.WARNING, "Vertx at: " + config.getVertxPlatformIdentifier() + " is taken, will not close it.");
               return;
            }
            log.log(Level.INFO, "Stops the Vert.x platform at: " + config.getVertxPlatformIdentifier());
            this.vertxPlatforms.remove(config.getVertxPlatformIdentifier());
            vertx.close();
         }
         else
         {
            log.log(Level.WARNING, "No Vert.x platform found at: " + config.getVertxPlatformIdentifier());
         }
      }
      finally
      {
         lock.unlock();
      }

   }

   private boolean isVertxHolded(Vertx vertx)
   {
      for (VertxHolder holder: this.vertxHolders)
      {
         if (vertx.equals(holder.getVertx()))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Stops all started Vert.x platforms.
    *
    * Clears all vertx holders.
    */
   void clear()
   {
      lock.lock();
      try
      {
         for (Map.Entry<String, Vertx> entry: this.vertxPlatforms.entrySet())
         {
            log.log(Level.INFO, "Closing Vert.x Platform at address: " + entry.getKey());
            entry.getValue().close();
            log.log(Level.INFO, "Vert.x Platform at address: " + entry.getKey() + " is Closed.");
         }
         this.vertxPlatforms.clear();
         this.vertxHolders.clear();
      }
      finally
      {
         lock.unlock();
      }
   }

   /**
    * The Listener to monitor whether the embedded vert.x runtime is ready.
    *
    */
   public interface VertxListener
   {

      /**
       * When vertx is ready, maybe just started, or have been started already.
       *
       * NOTE: can't call vertxPlatforms related methods within this callback method, which will cause infinite waiting.
       *
       * @param vertx the Vert.x
       */
      void whenReady(Vertx vertx);
   }

}
