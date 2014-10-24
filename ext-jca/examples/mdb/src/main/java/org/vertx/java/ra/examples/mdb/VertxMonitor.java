package org.vertx.java.ra.examples.mdb;

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import org.jboss.ejb3.annotation.ResourceAdapter;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.resourceadapter.inflow.VertxListener;

/**
 * Message-Driven Bean implementation class for: VertxMonitor
 */
@MessageDriven(name = "VertxMonitor", 
       messageListenerInterface = VertxListener.class,
       activationConfig = {
                   @ActivationConfigProperty(propertyName = "address", propertyValue = "inbound-address"),
                   })
@ResourceAdapter("@VERTXRA@")
public class VertxMonitor implements VertxListener {

   private Logger logger = Logger.getLogger(VertxMonitor.class.getName());
   
    /**
     * Default constructor. 
     */
    public VertxMonitor() {
        logger.info("VertxMonitor started.");
    }

   @Override
   public <T> void onMessage(Message<T> message)
   {

      logger.info("Get a message from Vert.x: " + message.toString());
      T body = message.body();
      if (body != null)
      {
         logger.info("Body of the message: " + body.toString());
         message.reply("Hi, Got your message: " + body.toString());
      }
      else
      {
        message.reply("Hi, Got your empty message.");
      }
   }

}
