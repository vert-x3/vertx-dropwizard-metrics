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

import com.codahale.metrics.MetricRegistry;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.JsonGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Vert.x Dropwizard metrics configuration.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
@JsonGen(publicConverter = false, inheritConverter = true)
public class DropwizardMetricsOptions extends MetricsOptions {

  /**
   * The default value of JMX enabled = {@code false}
   */
  public static final boolean DEFAULT_JMX_ENABLED = false;

  /**
   * The default monitored handlers : {@code null}
   */
  public static final List<Match> DEFAULT_MONITORED_HANDLERS = null;

  /**
   * The default monitored http server uris : {@code null}
   */
  public static final List<Match> DEFAULT_MONITORED_HTTP_SERVER_URIS = null;

  /**
   * The default monitored http server routes : {@code null}
   */
  public static final List<Match> DEFAULT_MONITORED_HTTP_SERVER_ROUTES = null;

  /**
   * The default monitored http client uris : {@code null}
   */
  public static final List<Match> DEFAULT_MONITORED_HTTP_CLIENT_URIS = null;

  /**
   * The default monitored http client endpoints : {@code null}
   */
  public static final List<Match> DEFAULT_MONITORED_HTTP_CLIENT_ENDPOINTS = null;

  private String registryName;
  private boolean jmxEnabled;
  private String jmxDomain;
  private List<Match> monitoredEventBusHandlers;
  private List<Match> monitoredHttpServerUris;
  private List<Match> monitoredHttpServerRoutes;
  private List<Match> monitoredHttpClientUris;
  private List<Match> monitoredHttpClientEndpoints;
  private String configPath;
  private String baseName;
  private MetricRegistry metricRegistry;

  /**
   * Default constructor
   */
  public DropwizardMetricsOptions() {
    jmxEnabled = DEFAULT_JMX_ENABLED;
    monitoredEventBusHandlers = DEFAULT_MONITORED_HANDLERS;
    monitoredHttpServerUris = DEFAULT_MONITORED_HTTP_SERVER_URIS;
    monitoredHttpServerRoutes = DEFAULT_MONITORED_HTTP_SERVER_ROUTES;
    monitoredHttpClientUris = DEFAULT_MONITORED_HTTP_CLIENT_URIS;
    monitoredHttpClientEndpoints = DEFAULT_MONITORED_HTTP_CLIENT_ENDPOINTS;
  }

  /**
   * Copy constructor with base metrics options
   *
   * @param other The other {@link MetricsOptions} to copy when creating this
   */
  public DropwizardMetricsOptions(MetricsOptions other) {
    super(other);
    jmxEnabled = DEFAULT_JMX_ENABLED;
    monitoredEventBusHandlers = DEFAULT_MONITORED_HANDLERS;
    monitoredHttpServerUris = DEFAULT_MONITORED_HTTP_SERVER_URIS;
    monitoredHttpServerRoutes = DEFAULT_MONITORED_HTTP_SERVER_ROUTES;
    monitoredHttpClientUris = DEFAULT_MONITORED_HTTP_CLIENT_URIS;
    monitoredHttpClientEndpoints = DEFAULT_MONITORED_HTTP_CLIENT_ENDPOINTS;
  }

  /**
   * Copy constructor
   *
   * @param other The other {@link DropwizardMetricsOptions} to copy when creating this
   */
  public DropwizardMetricsOptions(DropwizardMetricsOptions other) {
    super(other);
    baseName = other.getBaseName();
    registryName = other.getRegistryName();
    jmxEnabled = other.isJmxEnabled();
    jmxDomain = other.getJmxDomain();
    configPath = other.getConfigPath();
    monitoredEventBusHandlers = other.monitoredEventBusHandlers == null ? null : new ArrayList<>(other.monitoredEventBusHandlers);
    monitoredHttpServerUris = other.monitoredHttpServerUris == null ? null : new ArrayList<>(other.monitoredHttpServerUris);
    monitoredHttpServerRoutes = other.monitoredHttpServerRoutes == null ? null : new ArrayList<>(other.monitoredHttpServerRoutes);
    monitoredHttpClientUris = other.monitoredHttpClientUris == null ? null : new ArrayList<>(other.monitoredHttpClientUris);
    monitoredHttpClientEndpoints = other.monitoredHttpClientEndpoints == null ? null : new ArrayList<>(other.monitoredHttpClientEndpoints);
    metricRegistry = other.getMetricRegistry();
  }

  /**
   * Create an instance from a {@link io.vertx.core.json.JsonObject}
   *
   * @param json the JsonObject to create it from
   */
  public DropwizardMetricsOptions(JsonObject json) {
    this();
    DropwizardMetricsOptionsConverter.fromJson(json, this);
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
    if (monitoredEventBusHandlers == null) {
      monitoredEventBusHandlers = new ArrayList<>();
    }
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
    if (monitoredHttpServerUris == null) {
      monitoredHttpServerUris = new ArrayList<>();
    }
    monitoredHttpServerUris.add(match);
    return this;
  }

  /**
   * @return the list of monitored http server routes
   */
  public List<Match> getMonitoredHttpServerRoutes() {
    return monitoredHttpServerRoutes;
  }

  /**
   * Add an monitored http server route.
   *
   * @param match the handler match
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions addMonitoredHttpServerRoute(Match match) {
    if (monitoredHttpServerRoutes == null) {
      monitoredHttpServerRoutes = new ArrayList<>();
    }
    monitoredHttpServerRoutes.add(match);
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
    if (monitoredHttpClientUris == null) {
      monitoredHttpClientUris = new ArrayList<>();
    }
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
    if (monitoredHttpClientEndpoints == null) {
      monitoredHttpClientEndpoints = new ArrayList<>();
    }
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

  /**
   * An optional metric registry used instead of the Dropwizard shared registry.
   *
   * @return the metricRegistry
   */
  public MetricRegistry getMetricRegistry() {
    return metricRegistry;
  }

  /**
   * Set the optional metric registry used instead of the Dropwizard shared registry.
   *
   * @param metricRegistry the metricRegistry
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions setMetricRegistry(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
    return this;
  }

  /**
   * @return a JSON representation of these options
   */
  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    DropwizardMetricsOptionsConverter.toJson(this, json);
    return json;
  }
}
