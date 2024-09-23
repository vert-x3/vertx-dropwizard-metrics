package io.vertx.ext.dropwizard;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.dropwizard.Match}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.dropwizard.Match} original class using Vert.x codegen.
 */
public class MatchConverter {

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Match obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "value":
          if (member.getValue() instanceof String) {
            obj.setValue((String)member.getValue());
          }
          break;
        case "type":
          if (member.getValue() instanceof String) {
            obj.setType(io.vertx.ext.dropwizard.MatchType.valueOf((String)member.getValue()));
          }
          break;
        case "alias":
          if (member.getValue() instanceof String) {
            obj.setAlias((String)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(Match obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(Match obj, java.util.Map<String, Object> json) {
    if (obj.getValue() != null) {
      json.put("value", obj.getValue());
    }
    if (obj.getType() != null) {
      json.put("type", obj.getType().name());
    }
    if (obj.getAlias() != null) {
      json.put("alias", obj.getAlias());
    }
  }
}
