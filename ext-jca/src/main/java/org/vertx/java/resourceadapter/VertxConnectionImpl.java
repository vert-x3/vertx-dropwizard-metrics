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

import java.util.logging.Logger;

import javax.resource.ResourceException;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.SharedData;

/**
 * VertxPlatformImpl
 *
 * @version $Revision: $
 */
public class VertxConnectionImpl implements VertxConnection
{

   /** The logger */
   private static Logger log = Logger.getLogger(VertxConnectionImpl.class.getName());

   /** ManagedConnection **/
   private VertxManagedConnection mc;

   /**
    *
    * @param mc
    */
   public VertxConnectionImpl(VertxManagedConnection mc)
   {
      this.mc = mc;
   }

   /**
    * Get connection from factory
    *
    * @return VertxConnection instance
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   @Override
   public EventBus eventBus() throws ResourceException
   {
      log.finest("getConnection()");
      if (this.mc != null)
      {
         return this.mc.getEventBus();
      }
      throw new ResourceException("Vertx Managed Connection has been closed.");
   }

   @Override
   public void close() throws ResourceException
   {
      this.mc.closeHandle(this);
      this.mc = null;
   }

   @Override
   public SharedData getSharedData() throws ResourceException
   {
      if (this.mc != null)
      {
         return this.mc.getSharedData();
      }
      throw new ResourceException("Vertx Managed Connection has been closed.");
   }

}
