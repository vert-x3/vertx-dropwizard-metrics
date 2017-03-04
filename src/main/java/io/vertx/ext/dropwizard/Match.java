package io.vertx.ext.dropwizard;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * A match for a value.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class Match {

  /**
   * The default value : {@link io.vertx.ext.dropwizard.MatchType#EQUALS}
   */
  public static final MatchType DEFAULT_TYPE = MatchType.EQUALS;

  private String value;
  private MatchType type;
  private String identifier;

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
    value = other.value;
    type = other.type;
  }

  /**
   * Create an instance from a {@link io.vertx.core.json.JsonObject}
   *
   * @param json the JsonObject to create it from
   */
  public Match(JsonObject json) {
    value = json.getString("value");
    type = MatchType.valueOf(json.getString("type", DEFAULT_TYPE.name()));
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
   * @return the matcher identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Set the identifier the human readable name that will be used as a part of
   * registry entry name if the value matches.
   *
   * @param identifier the matcher identifier
   * @return a reference to this, so the API can be used fluently
   */
  public Match setIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }
}
