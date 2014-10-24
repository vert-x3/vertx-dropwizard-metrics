/**
 * 
 */
package org.vertx.java.resourceadapter;

import io.vertx.spi.cluster.impl.hazelcast.HazelcastClusterManager;

import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case of ClusterManagerFactory choice.
 * 
 * @author Lin Gao <lgao@redhat.com>
 *
 */
public class ClusterManagerTestCase
{

   /**
    * We use ProgrammableClusterManagerFactory to load customer hazelcast configuration file.
    */
   @Test
   public void testUsingProgrammableClusterManagerFactory()
   {
//      ServiceLoader<HazelcastClusterManager> cluterManagerFactoryLoader = ServiceLoader.load(HazelcastClusterManager.class);
//      Assert.assertNotNull(cluterManagerFactoryLoader);
//      Assert.assertTrue(cluterManagerFactoryLoader.iterator().hasNext());
//      HazelcastClusterManager clusterManagerFactory = cluterManagerFactoryLoader.iterator().next();
//      Assert.assertNotNull(clusterManagerFactory);
//      Assert.assertTrue(clusterManagerFactory.getClass().equals(HazelcastClusterManager.class));
      
   }

}
