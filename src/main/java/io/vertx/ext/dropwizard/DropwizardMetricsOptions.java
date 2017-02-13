/*
 * Copyright (c) 2011-2013 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.dropwizard;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.metrics.MetricsOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Vert.x Dropwizard metrics configuration.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class DropwizardMetricsOptions extends MetricsOptions {
  private static final Logger log = LoggerFactory.getLogger(DropwizardMetricsOptions.class);

  /**
   * The default value of JMX enabled = false
   */
  public static final boolean DEFAULT_JMX_ENABLED = false;

  /**
   * The default monitored handlers : empty by default
   */
  public static final List<Match> DEFAULT_MONITORED_HANDLERS = Collections.emptyList();

  /**
   * The default monitored http server uris : empty by default
   */
  public static final List<Match> DEFAULT_MONITORED_HTTP_SERVER_URIS = Collections.emptyList();

  /**
   * The default monitored http client uris : empty by default
   */
  public static final List<Match> DEFAULT_MONITORED_HTTP_CLIENT_URIS = Collections.emptyList();

  /**
   * The default monitored http client endpoints : empty by default
   */
  public static final List<Match> DEFAULT_MONITORED_HTTP_CLIENT_ENDPOINTS = Collections.emptyList();

  private String registryName;
  private boolean jmxEnabled;
  private String jmxDomain;
  private List<Match> monitoredEventBusHandlers;
  private List<Match> monitoredHttpServerUris;
  private List<Match> monitoredHttpClientUris;
  private List<Match> monitoredHttpClientEndpoints;
  private String configPath;
  private String baseName;

  /**
   * Default constructor
   */
  public DropwizardMetricsOptions() {
    jmxEnabled = DEFAULT_JMX_ENABLED;
    monitoredEventBusHandlers = new ArrayList<>(DEFAULT_MONITORED_HANDLERS);
    monitoredHttpServerUris = new ArrayList<>(DEFAULT_MONITORED_HTTP_SERVER_URIS);
    monitoredHttpClientUris = new ArrayList<>(DEFAULT_MONITORED_HTTP_CLIENT_URIS);
    monitoredHttpClientEndpoints = new ArrayList<>(DEFAULT_MONITORED_HTTP_CLIENT_ENDPOINTS);
  }

  /**
   * Copy constructor with base metrics options
   *
   * @param other The other {@link MetricsOptions} to copy when creating this
   */
  public DropwizardMetricsOptions(MetricsOptions other) {
    super(other);
    jmxEnabled = DEFAULT_JMX_ENABLED;
    monitoredEventBusHandlers = new ArrayList<>(DEFAULT_MONITORED_HANDLERS);
    monitoredHttpServerUris = new ArrayList<>(DEFAULT_MONITORED_HTTP_SERVER_URIS);
    monitoredHttpClientUris = new ArrayList<>(DEFAULT_MONITORED_HTTP_CLIENT_URIS);
    monitoredHttpClientEndpoints = new ArrayList<>(DEFAULT_MONITORED_HTTP_CLIENT_ENDPOINTS);
  }

  /**
   * Copy constructor
   *
   * @param other The other {@link DropwizardMetricsOptions} to copy when creating this
   */
  public DropwizardMetricsOptions(DropwizardMetricsOptions other) {
    super(other);
    registryName = other.getRegistryName();
    jmxEnabled = other.isJmxEnabled();
    jmxDomain = other.getJmxDomain();
    configPath = other.getConfigPath();
    monitoredEventBusHandlers = new ArrayList<>(other.monitoredEventBusHandlers);
    monitoredHttpServerUris = new ArrayList<>(other.monitoredHttpServerUris);
    monitoredHttpClientUris = new ArrayList<>(other.monitoredHttpClientUris);
    monitoredHttpClientEndpoints = new ArrayList<>(other.monitoredHttpClientEndpoints);
  }

  /**
   * Create an instance from a {@link io.vertx.core.json.JsonObject}
   *
   * @param json the JsonObject to create it from
   */
  public DropwizardMetricsOptions(JsonObject json) {
    super(json);
    registryName = json.getString("registryName");
    jmxEnabled = json.getBoolean("jmxEnabled", DEFAULT_JMX_ENABLED);
    jmxDomain = json.getString("jmxDomain");
    configPath = json.getString("configPath");
    if (json.containsKey("monitoredHandlers") && !json.containsKey("monitoredEventBusHandlers")) {
      log.warn("JSON config: monitoredHandlers field is deprecated, use monitoredEventBusHandlers instead");
      monitoredEventBusHandlers = loadMonitored("monitoredHandlers", json);
    } else {
      monitoredEventBusHandlers = loadMonitored("monitoredEventBusHandlers", json);
    }
    if (json.containsKey("monitoredServerUris") && !json.containsKey("monitoredHttpServerUris")) {
      log.warn("JSON config: monitoredServerUris field is deprecated, use monitoredHttpServerUris instead");
      monitoredHttpServerUris = loadMonitored("monitoredServerUris", json);
    } else {
      monitoredHttpServerUris = loadMonitored("monitoredHttpServerUris", json);
    }
    if (json.containsKey("monitoredClientUris") && !json.containsKey("monitoredHttpClientUris")) {
      log.warn("JSON config: monitoredClientUris field is deprecated, use monitoredHttpClientUris instead");
      monitoredHttpClientUris = loadMonitored("monitoredClientUris", json);
    } else {
      monitoredHttpClientUris = loadMonitored("monitoredHttpClientUris", json);
    }
    if (json.containsKey("monitoredClientEndpoints") && !json.containsKey("monitoredHttpClientEndpoints")) {
      log.warn("JSON config: monitoredClientEndpoints field is deprecated, use monitoredHttpClientEndpoints instead");
      monitoredHttpClientEndpoints = loadMonitored("monitoredClientEndpoints", json);
    } else {
      monitoredHttpClientEndpoints = loadMonitored("monitoredHttpClientEndpoints", json);
    }
  }

  private List<Match> loadMonitored(String arrayField, JsonObject json) {
    List<Match> list = new ArrayList<>();

    JsonArray monitored = json.getJsonArray(arrayField, new JsonArray());
    monitored.forEach(object -> {
      if (object instanceof JsonObject) list.add(new Match((JsonObject) object));
    });

    return list;
  }

  /**
   * An optional name used for registering the metrics in the Dropwizard shared registry.
   *
   * @return the registry name
   */
  public String getRegistryName() {
    return registryName;
  }

  /**
   * Set the name used for registering the metrics in the Dropwizard shared registry.
   *
   * @param registryName the name
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions setRegistryName(String registryName) {
    this.registryName = registryName;
    return this;
  }

  /**
   * Will JMX be enabled on the Vert.x instance?
   *
   * @return true if enabled, false if not.
   */
  public boolean isJmxEnabled() {
    return jmxEnabled;
  }

  /**
   * Set whether JMX will be enabled on the Vert.x instance.
   *
   * @param jmxEnabled true if JMX enabled, or false if not.
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions setJmxEnabled(boolean jmxEnabled) {
    this.jmxEnabled = jmxEnabled;
    if (jmxEnabled) {
      setEnabled(true);
    }
    return this;
  }

  /**
   * Get the JMX domain to use when JMX metrics are enabled.
   *
   * @return the JMX domain
   */
  public String getJmxDomain() {
    return jmxDomain;
  }

  /**
   * Set the JMX domain to use when JMX metrics are enabled.
   *
   * @param jmxDomain the JMX domain
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions setJmxDomain(String jmxDomain) {
    // todo test this
    this.jmxDomain = jmxDomain;
    return this;
  }

  /**
   * @return the list of monitored event bus handlers
   */
  public List<Match> getMonitoredEventBusHandlers() {
    return monitoredEventBusHandlers;
  }

  /**
   * Add a monitored event bus handler.
   *
   * @param match the event bus address match
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions addMonitoredEventBusHandler(Match match) {
    monitoredEventBusHandlers.add(match);
    return this;
  }

  /**
   * @return the list of monitored http server uris
   */
  public List<Match> getMonitoredHttpServerUris() {
    return monitoredHttpServerUris;
  }

  /**
   * Add an monitored http server uri.
   *
   * @param match the handler match
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions addMonitoredHttpServerUri(Match match) {
    monitoredHttpServerUris.add(match);
    return this;
  }

  /**
   * @return the list of monitored http client uris
   */
  public List<Match> getMonitoredHttpClientUris() {
    return monitoredHttpClientUris;
  }

  @Override
  public DropwizardMetricsOptions setEnabled(boolean enable) {
    return (DropwizardMetricsOptions) super.setEnabled(enable);
  }

  /**
   * @return the path for a config file to create an Options object from.
   */
  public String getConfigPath() {
    return configPath;
  }

  /**
   * Set the path for a config file that contains options in JSON format, to be used to create a new options object.
   * The file will be looked for on the file system first and then on the classpath if it's not found.
   *
   * @param configPath the file name
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions setConfigPath(String configPath) {
    this.configPath = configPath;
    return this;
  }

  /**
   * Add an monitored http client uri.
   *
   * @param match the handler match
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions addMonitoredHttpClientUri(Match match) {
    monitoredHttpClientUris.add(match);
    return this;
  }

  /**
   * Add an monitored http client endpoint.
   *
   * @param match the handler match
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions addMonitoredHttpClientEndpoint(Match match) {
    monitoredHttpClientEndpoints.add(match);
    return this;
  }

  /**
   * @return the list of monitored http client endpoints
   */
  public List<Match> getMonitoredHttpClientEndpoint() {
    return monitoredHttpClientEndpoints;
  }

  /**
   * Set a custom baseName for metrics.
   *
   * @param baseName the new baseName.
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions setBaseName(String baseName) {
    this.baseName = baseName;
    return this;
  }

  /**
   * @return The custom baseName.
   */
  public String getBaseName() {
    return baseName;
  }
}
