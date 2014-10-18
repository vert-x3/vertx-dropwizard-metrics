# Metrics for Vert.x

This is an implementation of core Vert.x metrics which internally uses the [Dropwizard metrics](https://github.com/dropwizard/metrics) library.

Vert.x core provides an SPI for recording metrics for various core options in the package `io.vertx.core.metrics.spi`
. This project provides an implementation of those interfaces.

If metrics is enabled in Vert.x then when it starts it will look on its classpath for instances of `io.vertx.core.spi
.MetricsVerticleFactory`. This project provides an implementation of that interface.

It also provides a reporter which exposes the metrics as JMX MBeans. 

## The metrics

The following metrics are currently provided. 

*Please note - this can, and will be, improved over time.*

### Vert.x metrics

The following metrics are provided:

* `event-loop-size` - the number of threads in the event loop pool
* `worker-pool-size` - the number of threads in the worker pool
* `cluster-host` - the cluster-host setting
* `cluster-port` - the cluster-port setting
* `verticles` - the number of verticles currently deployed

TODO nick please complete this

### Http server metrics

The following metrics are provided:

### Http client metrics

### Net server metrics

### Net client metrics

### Datagram socket metrics

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


    




    
    

    


