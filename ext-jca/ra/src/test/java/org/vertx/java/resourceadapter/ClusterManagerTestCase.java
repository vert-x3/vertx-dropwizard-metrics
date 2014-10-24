/**
 * 
 */
package org.vertx.java.resourceadapter;

import java.util.ServiceLoader;


import org.junit.Assert;
import org.junit.Test;
import org.vertx.java.core.spi.cluster.ClusterManagerFactory;
import org.vertx.java.spi.cluster.impl.hazelcast.ProgrammableClusterManagerFactory;

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
      ServiceLoader<ClusterManagerFactory> cluterManagerFactoryLoader = ServiceLoader.load(ClusterManagerFactory.class);
      Assert.assertNotNull(cluterManagerFactoryLoader);
      Assert.assertTrue(cluterManagerFactoryLoader.iterator().hasNext());
      ClusterManagerFactory clusterManagerFactory = cluterManagerFactoryLoader.iterator().next();
      Assert.assertNotNull(clusterManagerFactory);
      Assert.assertTrue(clusterManagerFactory.getClass().equals(ProgrammableClusterManagerFactory.class));
      
   }

}
