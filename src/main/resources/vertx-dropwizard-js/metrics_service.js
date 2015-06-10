/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module vertx-dropwizard-js/metrics_service */
var utils = require('vertx-js/util/utils');
var Measured = require('vertx-js/measured');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JMetricsService = io.vertx.ext.dropwizard.MetricsService;

/**
 The metrics service mainly allows to return a snapshot of measured objects.

 @class
*/
var MetricsService = function(j_val) {

  var j_metricsService = j_val;
  var that = this;

  /**
   Will return the metrics that correspond with this measured object, null if no metrics is available.<p/>
  
   Note: in the case of scaled servers, the JsonObject returns an aggregation of the metrics as the
   dropwizard backend reports to a single server.

   @public
   @param o {Measured} 
   @return {Object} the map of metrics where the key is the name of the metric (excluding the base name) and the value is the json data representing that metric
   */
  this.getMetricsSnapshot = function(o) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      return utils.convReturnJson(j_metricsService["getMetricsSnapshot(io.vertx.core.metrics.Measured)"](o._jdel));
    } else utils.invalidArgs();
  };

  /**
   @param measured the measure object

   @public
   @param measured {Measured} 
   @return {string} the base name of the measured object
   */
  this.getBaseName = function(measured) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      return j_metricsService["getBaseName(io.vertx.core.metrics.Measured)"](measured._jdel);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_metricsService;
};

/**
 Creates a metric service for a given {@link Vertx} instance.

 @memberof module:vertx-dropwizard-js/metrics_service
 @param vertx {Vertx} the vertx instance 
 @return {MetricsService} the metrics service
 */
MetricsService.create = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(JMetricsService["create(io.vertx.core.Vertx)"](vertx._jdel), MetricsService);
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = MetricsService;