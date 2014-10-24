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
 *
 */
package org.vertx.java.resourceadapter.examples.web;


import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

/**
 * A sample verticle used to show how client javascript communicates with MDB using distributed eventbus.
 *
 * @author Lin Gao <lgao@redhat.com>
 * @author <a href="http://tfox.org">Tim Fox</a>
 * 
 */
public class ExampleWebServer extends Verticle {

  private static final String ADDRESS = "sockjs-server-address";
  
  public void start() {
     final Logger logger = container.logger();
     
     HttpServer server = vertx.createHttpServer();
     
     server.requestHandler(new Handler<HttpServerRequest>()
      {
           public void handle(HttpServerRequest req) {
              String file = "";
              if (req.path().equals("/")) {
                file = "index.html";
              } else if (!req.path().contains("..")) {
                file = req.path();
              }
              req.response().sendFile("web/" + file);  
           };
      });
     
     vertx.eventBus().registerHandler(ADDRESS, new Handler<Message<?>>()
      {
           public void handle(Message<?> msg) {
              String body = msg.body().toString();
              logger.info("Got message: " + body + "\tReply it now.");
              msg.reply("Got your message: " + body); // just return back what it received.
           };
      });
     
     logger.info("Register handler on address: " + ADDRESS);
     
     JsonObject config = new JsonObject().putString("prefix", "/eventbus");
     
     // currently permit all messages passed in/out, just do this for example.
     vertx.createSockJSServer(server).bridge(config, new JsonArray().add(new JsonObject()),
           new JsonArray().add(new JsonObject()));
     int port = Integer.getInteger("vertx.http.port", 8090);
     server.listen(port);
     logger.info("Server Started at port: " + port);
  }
  
}
