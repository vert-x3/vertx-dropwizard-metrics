package io.vertx.ext.dropwizard;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonDecoder;

/**
 * Converter and Codec for {@link io.vertx.ext.dropwizard.Match}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.dropwizard.Match} original class using Vert.x codegen.
 */
public class MatchConverter implements JsonDecoder<Match, JsonObject> {

  public static final MatchConverter INSTANCE = new MatchConverter();

  @Override public Match decode(JsonObject value) { return (value != null) ? new Match(value) : null; }

  @Override public Class<Match> getTargetClass() { return Match.class; }
}
