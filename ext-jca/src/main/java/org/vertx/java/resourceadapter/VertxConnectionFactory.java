/**
 * 
 */
package org.vertx.java.resourceadapter;

import java.io.Serializable;

import javax.resource.Referenceable;
import javax.resource.ResourceException;

/**
 * 
 * The connection factory exposed to get a connection to the Vertx platform.
 * 
 * @author Lin Gao <lgao@redhat.com>
 *
 */
public interface VertxConnectionFactory extends Serializable, Referenceable 
{

   /**
    * Gets the Vert.x Platform.
    * 
    * @return a VertxPlatform instance
    * @throws ResourceException Thrown if a connection can't be obtained
    */
   public VertxConnection getVertxConnection() throws ResourceException;
   
   
}
