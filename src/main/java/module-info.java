import io.vertx.core.spi.VertxServiceProvider;

module io.vertx.metrics.dropwizard {

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;

  requires com.codahale.metrics;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires java.management;

  exports io.vertx.ext.dropwizard;
  exports io.vertx.ext.dropwizard.reporters;
  exports io.vertx.ext.dropwizard.impl to io.vertx.metrics.dropwizard.tests;

  provides io.vertx.core.spi.VertxServiceProvider with io.vertx.ext.dropwizard.DropwizardVertxMetricsFactory;

}
