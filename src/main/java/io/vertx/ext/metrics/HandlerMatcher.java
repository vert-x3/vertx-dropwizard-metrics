package io.vertx.ext.metrics;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * A matcher for an handler.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class HandlerMatcher {

  /**
   * The default regex value : false
   */
  public static final boolean DEFAULT_REGEX = false;

  private String address;
  private boolean regex;

  /**
   * Default constructor
   */
  public HandlerMatcher() {
    regex = DEFAULT_REGEX;
  }

  /**
   * Copy constructor
   *
   * @param other The other {@link io.vertx.ext.metrics.HandlerMatcher} to copy when creating this
   */
  public HandlerMatcher(HandlerMatcher other) {
    address = other.address;
    regex = other.regex;
  }

  /**
   * Create an instance from a {@link io.vertx.core.json.JsonObject}
   *
   * @param json the JsonObject to create it from
   */
  public HandlerMatcher(JsonObject json) {
    address = json.getString("address");
    regex = json.getBoolean("regex", DEFAULT_REGEX);
  }

  /**
   * @return the matched address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Set the matched address.
   *
   * @param address the address to match
   * @return a reference to this, so the API can be used fluently
   */
  public HandlerMatcher setAddress(String address) {
    this.address = address;
    return this;
  }

  /**
   * @return true if the address is matched as a regex
   */
  public boolean isRegex() {
    return regex;
  }

  /**
   * Set the address to be matched as a Java regular expression.
   *
   * @param regex true to match as a regex
   * @return a reference to this, so the API can be used fluently
   */
  public HandlerMatcher setRegex(boolean regex) {
    this.regex = regex;
    return this;
  }
}
