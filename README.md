# Dropwizard Metrics for Vert.x

[![Build Status (5.x)](https://github.com/vert-x3/vertx-dropwizard-metrics/actions/workflows/ci-5.x.yml/badge.svg)](https://github.com/vert-x3/vertx-dropwizard-metrics/actions/workflows/ci-5.x.yml)
[![Build Status (4.x)](https://github.com/vert-x3/vertx-dropwizard-metrics/actions/workflows/ci-4.x.yml/badge.svg)](https://github.com/eclipse-vertx/vertx-codegen/actions/workflows/ci-4.x.yml)

Metrics Service Provider implementations for Vert.x using [Dropwizard metrics](https://github.com/dropwizard/metrics) library.

## Documentation

* [Java documentation](http://vertx.io/docs/vertx-dropwizard-metrics/java/)
* [JavaScript documentation](http://vertx.io/docs/vertx-dropwizard-metrics/js/)
* [Kotlin documentation](http://vertx.io/docs/vertx-dropwizard-metrics/kotlin/)
* [Groovy documentation](http://vertx.io/docs/vertx-dropwizard-metrics/groovy/)
* [Ruby documentation](http://vertx.io/docs/vertx-dropwizard-metrics/ruby/)

## Using Vert.x Dropwizard Metrics as an OSGi Metrics

To use this project as an OSGi bundle check these examples:

* using a bridge classloader to load Vert.x and the VertxMetricFactory using the same classloader:
 https://github.com/vert-x3/issues/issues/178#issuecomment-241643262

* using a class dealing with the Vert.x SPI:
 https://github.com/vert-x3/issues/issues/178#issuecomment-241974500


You can also use Apache Aries SPY-Fly to expose a `io.vertx.core.spi.VertxMetricsFactory` service and manage the classloading for you.


