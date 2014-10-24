/**
 * 
 */
package org.vertx.java.resourceadapter.inflow;

import org.vertx.java.core.eventbus.Message;

/**
 * 
 * MDB message listener interface for the Vert.x platform.
 * 
 * @author Lin Gao <lgao@redhat.com>
 *
 */
public interface VertxListener
{

   /**
    * On Vertx Message.
    * 
    * @param message the message sent from vertx platform.
    */
   <T> void onMessage(Message<T> message);
   
}
