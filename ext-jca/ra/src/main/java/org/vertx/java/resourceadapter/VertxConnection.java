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

import javax.resource.ResourceException;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.shareddata.SharedData;

/**
 * VertxPlatform represents a Vert.x platform.
 *
 * @version $Revision: $
 */
public interface VertxConnection
{
   /** 
    * Get Vert.x distributed EventBus from the Vert.x platform.
    *
    * <p>
    * <b>NOTE: eventBus().close() method does nothing, it is managed by resource adapter.
    * 
    * @return EventBus instance
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   public EventBus eventBus() throws ResourceException;
   
   /**
    * Gets shared data from Vert.x platform.
    * <p>
    * <b>NOTE: Only SharedData in local node is supported now!</b>
    * 
    * @return the SharedData
    * @throws ResourceException Thrown if can't get the shared data
    */
   public SharedData getSharedData() throws ResourceException;
   
   /**
    * Closes the connection.
    * 
    * The close action does nothing about the underline Vert.x platform.
    * 
    * After this method call, next eventBus() and getSharedData() will throw ResourceException. 
    * 
    * @throws ResourceException Thrown if the connection failed close.
    */
   public void close() throws ResourceException;

}
