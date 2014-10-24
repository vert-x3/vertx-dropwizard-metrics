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

import org.vertx.java.resourceadapter.inflow.VertxActivation;
import org.vertx.java.resourceadapter.inflow.VertxActivationSpec;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

/**
 * VertxResourceAdapter is the Resource Adapter used to interact with a Vert.x cluster.
 * 
 * It uses a distributed event bus from Vert.x to send message, and receives message by registering a Vert.x Handler.
 *
 * @version $Revision: $
 */
@Connector(
   reauthenticationSupport = false,
   displayName = {"Vert.x Resource Adapter"},
   description = {"VertxResourceAdapter is the Resource Adapter used to interact with a Vert.x cluster."},
   eisType = "vertx",
   transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction)
public class VertxResourceAdapter implements ResourceAdapter, java.io.Serializable
{

   /**
    * serialVersionUID
    */
   private static final long serialVersionUID = 1130617878526175034L;

   /** The logger */
   private static Logger log = Logger.getLogger(VertxResourceAdapter.class.getName());

   /** The activations by activation spec */
   private ConcurrentHashMap<VertxActivationSpec, VertxActivation> activations;
   
   private WorkManager workManager;

   /**
    * Default constructor
    */
   public VertxResourceAdapter()
   {
      this.activations = new ConcurrentHashMap<VertxActivationSpec, VertxActivation>();
   }

   /**
    * This is called during the activation of a message endpoint.
    *
    * @param endpointFactory A message endpoint factory instance.
    * @param spec An activation spec JavaBean instance.
    * @throws ResourceException generic exception 
    */
   public void endpointActivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec) throws ResourceException
   {
      VertxActivation activation = new VertxActivation(this, endpointFactory, (VertxActivationSpec)spec);
      activations.put((VertxActivationSpec)spec, activation);
      activation.start();

      log.finest("endpointActivation()");

   }

   /**
    * This is called when a message endpoint is deactivated. 
    *
    * @param endpointFactory A message endpoint factory instance.
    * @param spec An activation spec JavaBean instance.
    */
   public void endpointDeactivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec)
   {
      VertxActivation activation = activations.remove(spec);
      if (activation != null)
         activation.stop();

      log.finest("endpointDeactivation()");

   }

   /**
    * This is called when a resource adapter instance is bootstrapped.
    *
    * @param ctx A bootstrap context containing references 
    * @throws ResourceAdapterInternalException indicates bootstrap failure.
    */
   public void start(BootstrapContext ctx)
      throws ResourceAdapterInternalException
   {
      log.finest("sets up configuration.");
      this.workManager = ctx.getWorkManager();
   }
   
   public WorkManager getWorkManager()
   {
      return workManager;
   }
   
   /**
    * This is called when a resource adapter instance is undeployed or
    * during application server shutdown. 
    * 
    * It will stop all Vert.x embedded platform.
    * 
    */
   public void stop()
   {
      log.finest("stop()");
      this.workManager = null;
      this.activations.clear();
      VertxPlatformFactory.instance().clear();
      
      // it seems after stop() is called, 
      // there are still some background threads running on vert.x
      // waiting for 1 second
      try
      {
         Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
         ;
      }
   }

   /**
    * This method is called by the application server during crash recovery.
    *
    * @param specs An array of ActivationSpec JavaBeans 
    * @throws ResourceException generic exception 
    * @return An array of XAResource objects
    */
   public XAResource[] getXAResources(ActivationSpec[] specs)
      throws ResourceException
   {
      log.finest("getXAResources()");
      return null;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (getClass() != obj.getClass())
         return false;
      VertxResourceAdapter other = (VertxResourceAdapter) obj;
      return super.equals(other);
   }
   
}
