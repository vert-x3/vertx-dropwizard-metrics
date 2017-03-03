package io.vertx.kotlin.ext.dropwizard

import io.vertx.ext.dropwizard.Match
import io.vertx.ext.dropwizard.MatchType

/**
 * A function providing a DSL for building [io.vertx.ext.dropwizard.Match] objects.
 *
 * A match for a value.
 *
 * @param type  Set the type of matching to apply.
 * @param value  Set the matched value.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.dropwizard.Match original] using Vert.x codegen.
 */
fun Match(
  type: MatchType? = null,
  value: String? = null): Match = io.vertx.ext.dropwizard.Match().apply {

  if (type != null) {
    this.setType(type)
  }
  if (value != null) {
    this.setValue(value)
  }
}

