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
package org.vertx.java.resourceadapter.inflow;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.vertx.java.resourceadapter.AbstractJcaBase;

/**
 * VertxActivationSpec
 *
 * @version $Revision: $
 */
@Activation(messageListeners = { VertxListener.class })
public class VertxActivationSpec extends AbstractJcaBase implements ActivationSpec
{

   /** The logger */
   private static Logger log = Logger.getLogger(VertxActivationSpec.class.getName());

   /** The resource adapter */
   private ResourceAdapter ra;
   
   private String address;
   

   /**
    * @return the address
    */
   public String getAddress()
   {
      return address;
   }

   /**
    * @param address the address to set
    */
   @ConfigProperty
   public void setAddress(String address)
   {
      this.address = address;
   }

   /**
    * Default constructor
    */
   public VertxActivationSpec()
   {

   }

   /**
    * This method may be called by a deployment tool to validate the overall
    * activation configuration information provided by the endpoint deployer.
    *
    * @throws InvalidPropertyException indicates invalid configuration property settings.
    */
   public void validate() throws InvalidPropertyException
   {
      log.finest("validate()");
      if (this.address == null || this.address.length() == 0)
      {
         throw new InvalidPropertyException("Address must be specified.");
      }
      if (this.getClusterConfigFile() == null || this.getClusterConfigFile().length() == 0)
      {
         log.log(Level.WARNING, "Cluster configuration file is not specified, Will use default-cluster.xml provided by the resource adapter.");
      }
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
      VertxActivationSpec other = (VertxActivationSpec) obj;
      return super.equals(other);
   }

}
