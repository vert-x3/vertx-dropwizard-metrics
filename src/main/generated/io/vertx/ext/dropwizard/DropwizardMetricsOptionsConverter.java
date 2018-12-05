package io.vertx.ext.dropwizard;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Converter for {@link io.vertx.ext.dropwizard.DropwizardMetricsOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.dropwizard.DropwizardMetricsOptions} original class using Vert.x codegen.
 */
public class DropwizardMetricsOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, DropwizardMetricsOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "baseName":
          if (member.getValue() instanceof String) {
            obj.setBaseName((String) member.getValue());
          }
          break;
        case "configPath":
          if (member.getValue() instanceof String) {
            obj.setConfigPath((String) member.getValue());
          }
          break;
        case "enabled":
          if (member.getValue() instanceof Boolean) {
            obj.setEnabled((Boolean) member.getValue());
          }
          break;
        case "jmxDomain":
          if (member.getValue() instanceof String) {
            obj.setJmxDomain((String) member.getValue());
          }
          break;
        case "jmxEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setJmxEnabled((Boolean) member.getValue());
          }
          break;
        case "monitoredEventBusHandlers":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>) member.getValue()).forEach(item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredEventBusHandler(new io.vertx.ext.dropwizard.Match((JsonObject) item));
            });
          }
          break;
        case "monitoredHttpClientEndpoint":
          if (member.getValue() instanceof JsonArray) {
          }
          break;
        case "monitoredHttpClientEndpoints":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>) member.getValue()).forEach(item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredHttpClientEndpoint(new io.vertx.ext.dropwizard.Match((JsonObject) item));
            });
          }
          break;
        case "monitoredHttpClientUris":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>) member.getValue()).forEach(item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredHttpClientUri(new io.vertx.ext.dropwizard.Match((JsonObject) item));
            });
          }
          break;
        case "monitoredHttpServerUris":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>) member.getValue()).forEach(item -> {
              if (item instanceof JsonObject)
                obj.addMonitoredHttpServerUri(new io.vertx.ext.dropwizard.Match((JsonObject) item));
            });
          }
          break;
        case "registryName":
          if (member.getValue() instanceof String) {
            obj.setRegistryName((String) member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(DropwizardMetricsOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(DropwizardMetricsOptions obj, java.util.Map<String, Object> json) {
    if (obj.getBaseName() != null) {
      json.put("baseName", obj.getBaseName());
    }
    if (obj.getConfigPath() != null) {
      json.put("configPath", obj.getConfigPath());
    }
    json.put("enabled", obj.isEnabled());
    if (obj.getJmxDomain() != null) {
      json.put("jmxDomain", obj.getJmxDomain());
    }
    json.put("jmxEnabled", obj.isJmxEnabled());
    if (obj.getRegistryName() != null) {
      json.put("registryName", obj.getRegistryName());
    }
  }
}
