/**
 * 
 */
package org.vertx.java.resourceadapter;

import org.vertx.java.core.Vertx;

/**
 * Vertx Holder represents a resource which has a reference to the Vertx.
 * 
 * @author Lin Gao <lgao@redhat.com>
 *
 */
public interface VertxHolder
{
   
   /**
    * Gets the Vertx it holds.
    * 
    * @return the Vertx platform.
    */
   Vertx getVertx();
   

}
