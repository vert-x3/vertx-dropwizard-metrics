package org.vertx.java.resourceadapter;

/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

/**
 * Listens on address: "outbound-address"
 * 
 * Reply a hello message
 * 
 * @author Lin Gao <lgao@redhat.com>
 *
 */
public class OutboundTestVerticle extends Verticle
{

   public void start()
   {
      this.vertx.eventBus().registerHandler("outbound-address", new Handler<Message<String>>()
      {
         public void handle(Message<String> message)
         {
            String string = message.body();
            if (string != null && string.length() > 0)
            {
               message.reply("Hello " + string + " from Outbound");
            }
         }
      });

   }
}
