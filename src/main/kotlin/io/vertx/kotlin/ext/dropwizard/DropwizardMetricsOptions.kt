package io.vertx.kotlin.ext.dropwizard

import io.vertx.ext.dropwizard.DropwizardMetricsOptions

fun DropwizardMetricsOptions(
        configPath: String? = null,
    enabled: Boolean? = null,
    jmxDomain: String? = null,
    jmxEnabled: Boolean? = null,
    registryName: String? = null): DropwizardMetricsOptions = io.vertx.ext.dropwizard.DropwizardMetricsOptions().apply {

    if (configPath != null) {
        this.configPath = configPath
    }

    if (enabled != null) {
        this.isEnabled = enabled
    }

    if (jmxDomain != null) {
        this.jmxDomain = jmxDomain
    }

    if (jmxEnabled != null) {
        this.isJmxEnabled = jmxEnabled
    }

    if (registryName != null) {
        this.registryName = registryName
    }

}

