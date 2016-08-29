# Dropwizard Metrics for Vert.x

[![Build Status](https://vertx.ci.cloudbees.com/buildStatus/icon?job=vert.x3-dropwizard-metrics)](https://vertx.ci.cloudbees.com/view/vert.x-3/job/vert.x3-dropwizard-metrics/)

Metrics Service Provider implementations for Vert.x using [Dropwizard metrics](https://github.com/dropwizard/metrics) library.

## Documentation

See https://github.com/vert-x3/vertx-dropwizard-metrics/blob/master/src/main/asciidoc/java/index.adoc


## Using Vert.x Dropwizard Metrics as an OSGi Metrics

To use this project as an OSGi bundle check these examples:

* using a bridge classloader to load Vert.x and the VertxMetricFactory using the same classloader:
 https://github.com/vert-x3/issues/issues/178#issuecomment-241643262
 
* using a class dealing with the Vert.x SPI:
 https://github.com/vert-x3/issues/issues/178#issuecomment-241974500
 
 
You can also use Apache Aries SPY-Fly to expose a `io.vertx.core.spi.VertxMetricsFactory` service and manage the classloading for you. 
 
 
