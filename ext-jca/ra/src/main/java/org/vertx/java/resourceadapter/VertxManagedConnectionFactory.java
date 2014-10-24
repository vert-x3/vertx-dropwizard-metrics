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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

import org.vertx.java.core.Vertx;

/**
 * The outbound of the resource adapter.
 * 
 * Each active *-ra.xml deployment may have different vertx platform.
 *
 * @version $Revision: $
 */
@ConnectionDefinition(connectionFactory = VertxConnectionFactory.class,
   connectionFactoryImpl = VertxConnectionFactoryImpl.class,
   connection = VertxConnection.class,
   connectionImpl = VertxConnectionImpl.class)
public class VertxManagedConnectionFactory extends AbstractJcaBase implements ManagedConnectionFactory, ResourceAdapterAssociation, VertxPlatformFactory.VertxListener
{

   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** The logger */
   private static Logger log = Logger.getLogger(VertxManagedConnectionFactory.class.getName());

   /** The resource adapter */
   private ResourceAdapter ra;

   /** The logwriter */
   private PrintWriter logwriter;
   
   private Vertx vertx;

   /**
    * Default constructor
    */
   public VertxManagedConnectionFactory()
   {

   }

   /**
    * Creates a Connection Factory instance. 
    *
    * @param cxManager ConnectionManager to be associated with created EIS connection factory instance
    * @return EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory instance
    * @throws ResourceException Generic exception
    */
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      log.finest("createConnectionFactory()");
      return new VertxConnectionFactoryImpl(this, cxManager);
   }

   /**
    * Creates a Connection Factory instance. 
    *
    * @return EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory instance
    * @throws ResourceException Generic exception
    */
   public Object createConnectionFactory() throws ResourceException
   {
      return createConnectionFactory( new VertxConnectionManager());
   }

   /**
    * Creates a new physical connection to the underlying EIS resource manager.
    *
    * @param subject Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @throws ResourceException generic exception
    * @return ManagedConnection instance 
    */
   public ManagedConnection createManagedConnection(Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      VertxPlatformConfiguration config = getVertxPlatformConfig();
      VertxPlatformFactory.instance().createVertxIfNotStart(config, this);
      long current = System.currentTimeMillis();
      while (this.vertx == null)
      {
         if (config.getTimeout() != null)
         {
            long now = System.currentTimeMillis();
            if (now - current > config.getTimeout())
            {
               throw new ResourceException("No Vert.x starts up within timeout: " + config.getTimeout() + " milliseconds");
            }
         }
         try
         {
            Thread.sleep(50);
         }
         catch (InterruptedException e)
         {
            
         }
      }
      log.log(Level.FINEST, "Creating a VertxManagedConnction with a Vertx platform.");
      return new VertxManagedConnection(this, vertx);
   }
   
   @Override
   public void whenReady(Vertx vertx)
   {
      this.vertx = vertx;
   }

   /**
    * Returns a matched connection from the candidate set of connections. 
    *
    * @param connectionSet Candidate connection set
    * @param subject Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @throws ResourceException generic exception
    * @return ManagedConnection if resource adapter finds an acceptable match otherwise null 
    */
   public ManagedConnection matchManagedConnections(Set connectionSet,
         Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.finest("matchManagedConnections()");
      ManagedConnection result = null;
      Iterator<?> it = connectionSet.iterator();
      while (result == null && it.hasNext())
      {
         ManagedConnection mc = (ManagedConnection)it.next();
         if (mc instanceof VertxManagedConnection)
         {
            VertxManagedConnection vertMC = (VertxManagedConnection)mc;
            if (this.equals(vertMC.getManagementConnectionFactory()))
            {
               // same MCF represents same Vertx platform
               result = mc;
               break;
            }
         }
      }
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((vertx == null) ? 0 : vertx.hashCode());
      return result;
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
      VertxManagedConnectionFactory other = (VertxManagedConnectionFactory) obj;
      if (vertx == null)
      {
         if (other.vertx != null)
            return false;
      }
      else if (!vertx.equals(other.vertx))
         return false;
      return true;
   }

   /**
    * Get the log writer for this ManagedConnectionFactory instance.
    *
    * @return PrintWriter
    * @throws ResourceException generic exception
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      log.finest("getLogWriter()");
      return logwriter;
   }

   /**
    * Set the log writer for this ManagedConnectionFactory instance.
    *
    * @param out PrintWriter - an out stream for error logging and tracing
    * @throws ResourceException generic exception
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      log.finest("setLogWriter()");
      logwriter = out;
   }

   /**
    * Get the resource adapter
    *
    * @return The handle
    */
   public ResourceAdapter getResourceAdapter()
   {
      log.finest("getResourceAdapter()");
      return ra;
   }

   /**
    * Set the resource adapter
    *
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      log.finest("setResourceAdapter()");
      this.ra = ra;
   }


}
