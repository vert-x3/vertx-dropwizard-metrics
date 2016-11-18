package io.vertx.kotlin.ext.dropwizard

import io.vertx.ext.dropwizard.Match
import io.vertx.ext.dropwizard.MatchType

fun Match(
        type: MatchType? = null,
    value: String? = null): Match = io.vertx.ext.dropwizard.Match().apply {

    if (type != null) {
        this.type = type
    }

    if (value != null) {
        this.value = value
    }

}

