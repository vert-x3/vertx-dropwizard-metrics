package io.vertx.ext.dropwizard;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.dropwizard.DropwizardMetricsOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.dropwizard.DropwizardMetricsOptions} original class using Vert.x codegen.
 */
public class DropwizardMetricsOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, DropwizardMetricsOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "registryName":
          if (member.getValue() instanceof String) {
            obj.setRegistryName((String)member.getValue());
          }
          break;
        case "jmxEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setJmxEnabled((Boolean)member.getValue());
          }
          break;
        case "jmxDomain":
          if (member.getValue() instanceof String) {
            obj.setJmxDomain((String)member.getValue());
          }
          break;
        case "monitoredEventBusHandlers":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredEventBusHandler(new io.vertx.ext.dropwizard.Match((io.vertx.core.json.JsonObject)item));
            });
          }
          break;
        case "monitoredHttpServerUris":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredHttpServerUri(new io.vertx.ext.dropwizard.Match((io.vertx.core.json.JsonObject)item));
            });
          }
          break;
        case "monitoredHttpServerRoutes":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredHttpServerRoute(new io.vertx.ext.dropwizard.Match((io.vertx.core.json.JsonObject)item));
            });
          }
          break;
        case "monitoredHttpClientUris":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredHttpClientUri(new io.vertx.ext.dropwizard.Match((io.vertx.core.json.JsonObject)item));
            });
          }
          break;
        case "configPath":
          if (member.getValue() instanceof String) {
            obj.setConfigPath((String)member.getValue());
          }
          break;
        case "monitoredHttpClientEndpoints":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredHttpClientEndpoint(new io.vertx.ext.dropwizard.Match((io.vertx.core.json.JsonObject)item));
            });
          }
          break;
        case "monitoredHttpClientEndpoint":
          if (member.getValue() instanceof JsonArray) {
          }
          break;
        case "baseName":
          if (member.getValue() instanceof String) {
            obj.setBaseName((String)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(DropwizardMetricsOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(DropwizardMetricsOptions obj, java.util.Map<String, Object> json) {
    if (obj.getRegistryName() != null) {
      json.put("registryName", obj.getRegistryName());
    }
    json.put("jmxEnabled", obj.isJmxEnabled());
    if (obj.getJmxDomain() != null) {
      json.put("jmxDomain", obj.getJmxDomain());
    }
    if (obj.getMonitoredEventBusHandlers() != null) {
      JsonArray array = new JsonArray();
      obj.getMonitoredEventBusHandlers().forEach(item -> array.add(item.toJson()));
      json.put("monitoredEventBusHandlers", array);
    }
    if (obj.getMonitoredHttpServerUris() != null) {
      JsonArray array = new JsonArray();
      obj.getMonitoredHttpServerUris().forEach(item -> array.add(item.toJson()));
      json.put("monitoredHttpServerUris", array);
    }
    if (obj.getMonitoredHttpServerRoutes() != null) {
      JsonArray array = new JsonArray();
      obj.getMonitoredHttpServerRoutes().forEach(item -> array.add(item.toJson()));
      json.put("monitoredHttpServerRoutes", array);
    }
    if (obj.getMonitoredHttpClientUris() != null) {
      JsonArray array = new JsonArray();
      obj.getMonitoredHttpClientUris().forEach(item -> array.add(item.toJson()));
      json.put("monitoredHttpClientUris", array);
    }
    if (obj.getConfigPath() != null) {
      json.put("configPath", obj.getConfigPath());
    }
    if (obj.getMonitoredHttpClientEndpoint() != null) {
      JsonArray array = new JsonArray();
      obj.getMonitoredHttpClientEndpoint().forEach(item -> array.add(item.toJson()));
      json.put("monitoredHttpClientEndpoint", array);
    }
    if (obj.getBaseName() != null) {
      json.put("baseName", obj.getBaseName());
    }
  }
}
