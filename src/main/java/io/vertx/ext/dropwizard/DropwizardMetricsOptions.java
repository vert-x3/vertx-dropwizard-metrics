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

  private String name;
  private boolean jmxEnabled;
  private String jmxDomain;
  private List<Match> monitoredEventBusHandlers;
  private List<Match> monitoredHttpServerUris;
  private List<Match> monitoredHttpClientUris;

  /**
   * Default constructor
   */
  public DropwizardMetricsOptions() {
    jmxEnabled = DEFAULT_JMX_ENABLED;
    monitoredEventBusHandlers = new ArrayList<>(DEFAULT_MONITORED_HANDLERS);
    monitoredHttpServerUris = new ArrayList<>(DEFAULT_MONITORED_HTTP_SERVER_URIS);
    monitoredHttpClientUris = new ArrayList<>(DEFAULT_MONITORED_HTTP_CLIENT_URIS);
  }

  /**
   * Copy constructor
   *
   * @param other The other {@link DropwizardMetricsOptions} to copy when creating this
   */
  public DropwizardMetricsOptions(DropwizardMetricsOptions other) {
    super(other);
    name = other.getName();
    jmxEnabled = other.isJmxEnabled();
    jmxDomain = other.getJmxDomain();
    monitoredEventBusHandlers = new ArrayList<>(other.monitoredEventBusHandlers);
  }

  /**
   * Create an instance from a {@link io.vertx.core.json.JsonObject}
   *
   * @param json the JsonObject to create it from
   */
  public DropwizardMetricsOptions(JsonObject json) {
    super(json);
    name = json.getString("name");
    jmxEnabled = json.getBoolean("jmxEnabled", DEFAULT_JMX_ENABLED);
    jmxDomain = json.getString("jmxDomain");
    monitoredEventBusHandlers = new ArrayList<>();
    JsonArray handlerAddressesArray = json.getJsonArray("monitoredHandlers");
    if (handlerAddressesArray != null) {
      for (Object o : handlerAddressesArray) {
        monitoredEventBusHandlers.add(new Match((JsonObject) o));
      }
    }
  }

  /**
   * An optional name used by the metrics implementation for namespacing or registering the metrics.
   *
   * @return the metrics name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name used by the metrics implementation for namespacing or registering the metrics.
   *
   * @param name the name
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions setName(String name) {
    this.name = name;
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
   * @param jmxDomain  the JMX domain
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
   * Add an monitored http client uri.
   *
   * @param match the handler match
   * @return a reference to this, so the API can be used fluently
   */
  public DropwizardMetricsOptions addMonitoredHttpClientUri(Match match) {
    monitoredHttpClientUris.add(match);
    return this;
  }
}
