package io.vertx.ext.dropwizard;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

/**
 * A match for a value.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
@JsonGen(publicConverter = false)
public class Match {

  /**
   * The default value : {@link io.vertx.ext.dropwizard.MatchType#EQUALS}
   */
  public static final MatchType DEFAULT_TYPE = MatchType.EQUALS;

  private String value;
  private MatchType type;
  private String alias;

  /**
   * Default constructor
   */
  public Match() {
    type = DEFAULT_TYPE;
  }

  /**
   * Copy constructor
   *
   * @param other The other {@link Match} to copy when creating this
   */
  public Match(Match other) {
    alias = other.alias;
    value = other.value;
    type = other.type;
  }

  /**
   * Create an instance from a {@link io.vertx.core.json.JsonObject}
   *
   * @param json the JsonObject to create it from
   */
  public Match(JsonObject json) {
    this();
    MatchConverter.fromJson(json, this);
  }

  /**
   * @return the matched value
   */
  public String getValue() {
    return value;
  }

  /**
   * Set the matched value.
   *
   * @param value the value to match
   * @return a reference to this, so the API can be used fluently
   */
  public Match setValue(String value) {
    this.value = value;
    return this;
  }

  /**
   * @return the matcher type
   */
  public MatchType getType() {
    return type;
  }

  /**
   * Set the type of matching to apply.
   *
   * @param type the matcher type
   * @return a reference to this, so the API can be used fluently
   */
  public Match setType(MatchType type) {
    this.type = type;
    return this;
  }

  /**
   * @return the matcher alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * Set the alias the human readable name that will be used as a part of
   * registry entry name when the value matches.
   *
   * @param alias the matcher alias
   * @return a reference to this, so the API can be used fluently
   */
  public Match setAlias(String alias) {
    this.alias = alias;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    MatchConverter.toJson(this, json);
    return json;
  }
}
