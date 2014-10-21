# Metrics for Vert.x

This is an implementation of core Vert.x metrics which internally uses the [Dropwizard metrics](https://github.com/dropwizard/metrics) library.

Vert.x core provides an SPI for recording metrics for various core options in the package `io.vertx.core.metrics.spi`
. This project provides an implementation of those interfaces.

If metrics is enabled in Vert.x then when it starts it will look on its classpath for instances of `io.vertx.core.spi
.MetricsVerticleFactory`. This project provides an implementation of that interface.

It also provides a reporter which exposes the metrics as JMX MBeans. 

## Naming

Each measured component listed below (except for Vertx) will have a base name associated with it. Each metric can be retrieved by providing
the fully qualified name <fqn> `baseName` + `.` + `metricName` from Vertx:

 ```java
 vertx.metrics().get("vertx.eventbus.handlers");
 ```

 or from the measured component itself using just the metric name:

 ```java
 vertx.eventBus().metrics().get("handlers");
```

See more examples below on how to retrieve/use metrics for a specific component.

## Data

Below is how each dropwizard metric is represented in JSON. Please refer to the [Dropwizard metrics](https://github.com/dropwizard/metrics)
documentation for detailed information on each metric.

#### Gauge

```javascript
{
  "value" : value // any json value
}
```

#### Counter

```javascript
{
  "count" : 1 // number
}
```

#### Histogram

```javascript
{
  "count"  : 1 // long
  "min"    : 1 // long
  "max"    : 1 // long
  "mean"   : 1.0 // double
  "stddev" : 1.0 // double
  "median" : 1.0 // double
  "75%"    : 1.0 // double
  "95%"    : 1.0 // double
  "98%"    : 1.0 // double
  "99%"    : 1.0 // double
  "99.9%"  : 1.0 // double
}
```

#### Meter

```javascript
{
  "count"             : 1 // long
  "meanRate"          : 1.0 // double
  "oneMinuteRate"     : 1.0 // double
  "fiveMinuteRate"    : 1.0 // double
  "fifteenMinuteRate" : 1.0 // double
  "rate"              : "events/second" // string representing rate
}
```

#### Timer

A timer is basically a combination of Histogram + Meter.

```javascript
{
  // histogram data
  "count"  : 1 // long
  "min"    : 1 // long
  "max"    : 1 // long
  "mean"   : 1.0 // double
  "stddev" : 1.0 // double
  "median" : 1.0 // double
  "75%"    : 1.0 // double
  "95%"    : 1.0 // double
  "98%"    : 1.0 // double
  "99%"    : 1.0 // double
  "99.9%"  : 1.0 // double

  // meter data
  "meanRate"          : 1.0 // double
  "oneMinuteRate"     : 1.0 // double
  "fiveMinuteRate"    : 1.0 // double
  "fifteenMinuteRate" : 1.0 // double
  "rate"              : "events/second" // string representing rate
}
```

## The metrics

The following metrics are currently provided.

*Please note - this can, and will be, improved over time.*

### Vert.x metrics

The following metrics are provided:

* `vertx.event-loop-size` - A [Gauge](#gauge) of the number of threads in the event loop pool
* `vertx.worker-pool-size` - A [Gauge](#gauge) of the number of threads in the worker pool
* `vertx.cluster-host` - A [Gauge](#gauge) of the cluster-host setting
* `vertx.cluster-port` - A [Gauge](#gauge) of the cluster-port setting
* `vertx.verticles` - A [Counter](#counter) of the number of verticles currently deployed

### Event bus metrics

Base name: `vertx.eventbus`

* `handlers` - A [Counter](#counter) of the number of event bus handlers
* `messages.received` - A [Meter](#meter) representing the rate of which messages are being received
* `messages.sent` - A [Meter](#meter) representing the rate of which messages are being sent
* `messages.published` - A [Meter](#meter) representing the rate of which messages are being published
* `messages.reply-failures` - A [Meter](#meter) representing the rate of reply failures

### Http server metrics

Base name: `vertx.http.servers.<host>:<port>`

Http server includes all the metrics of a [Net Server](#net-server-metrics)* plus the following:

* `requests` - A [Timer](#timer) of a request and the rate of it's occurrence
* `<http-method>-requests` - A [Timer](#timer) of a specific http method request and the rate of it's occurrence
  - Examples: `get-requests`, `post-requests`
* `<http-method>-requests./<uri>` - A [Timer](#timer) of a specific http method & URI request and the rate of it's occurrence
  - Examples: `get-requests./some/uri`, `post-requests./some/uri?foo=bar`

\* *For `bytes-read` and `bytes-written` the bytes represent the body of the request/response, so headers, etc are ignored.*

### Http client metrics

Base name: `vertx.http.clients.@<id>`

Http client includes all the metrics of a [Http Server](#http-server-metrics) plus the following:

* `connections.max-pool-size` - A [Gauge](#gauge) of the max connection pool size
* `connections.pool-ratio` - A ratio [Gauge](#gauge) of the open connections / max connection pool size

### Net server metrics

Base name: `vertx.net.servers.<host>:<port>`

* `open-connections` - A [Counter](#counter) of the number of open connections
* `open-connections.<remote-host>` - A [Counter](#counter) of the number of open connections for a particular remote host
* `connections` - A [Timer](#timer) of a connection and the rate of it's occurrence
* `exceptions` - A [Counter](#counter) of the number of exceptions
* `bytes-read` - A [Histogram](#histogram) of the number of bytes read.
* `bytes-written` - A [Histogram](#histogram) of the number of bytes written.

### Net client metrics

Base name: `vertx.net.clients.@<id>`

Net client includes all the metrics of a [Net Server](#net-server-metrics)

### Datagram socket metrics

Base name: `vertx.datagram`

* `sockets` - A [Counter](#counter) of the number of datagram sockets
* `exceptions` - A [Counter](#counter) of the number of exceptions
* `bytes-written` - A [Histogram](#histogram) of the number of bytes written.
* `<host>:<port>.bytes-read` - A [Histogram](#histogram) of the number of bytes read.
  - This metric will only be available if the datagram socket is listening

## Usage

Metrics is disabled by default.

### Embedding

You enable metrics on Vert.x using the `VertxOptions` class you use when creating Vert.x:

    Vertx vertx = Vertx.vertx(new DeploymentOptions().setMetricsEnabled(true));
    
If you want JMX too, then you also need to enabled that:
    
    Vertx vertx = Vertx.vertx(new DeploymentOptions().setMetricsEnabled(true).setJMXEnabled(true);
    
### Command line
    
If running Vert.x from the command line you can enable metrics and JMX by uncommented the JMX_OPTS line in the 
`vertx` or `vertx.bat` script:

    JMX_OPTS="-Dcom.sun.management.jmxremote -Dvertx.options.jmxEnabled=true"
       

### Enabling remote JMX

If you want the metrics to be exposed remotely over JMX, then you need to set, at minimum the following system property:

    com.sun.management.jmxremote
    
If running from the command line this can be done by editing the `vertx` or `vertx.bat` and uncommenting the 
`JMX_OPTS` line.

Please see the [Oracle JMX documentation](http://docs.oracle.com/javase/8/docs/technotes/guides/management/agent.html) for more information on configuring JMX

*If running Vert.x on a public server please be careful about exposing remote JMX access*
