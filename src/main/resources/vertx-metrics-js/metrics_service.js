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

/** @module vertx-metrics-js/metrics_service */
var utils = require('vertx-js/util/utils');
var Measured = require('vertx-js/measured');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JMetricsService = io.vertx.ext.metrics.MetricsService;

/**

 @class
*/
var MetricsService = function(j_val) {

  var j_metricsService = j_val;
  var that = this;

  /**
   Will return the metrics that correspond with this measured object.

   @public
   @param o {Measured} 
   @return {Array.<string>} the map of metrics where the key is the name of the metric (excluding the base name) and the value is the json data representing that metric
   */
  this.getMetricsSnapshot = function(o) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      return utils.convReturnMap(j_metricsService.getMetricsSnapshot(o._jdel));
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_metricsService;
};

/**

 @memberof module:vertx-metrics-js/metrics_service

 @return {MetricsService}
 */
MetricsService.getMetrics = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new MetricsService(JMetricsService.getMetrics());
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = MetricsService;