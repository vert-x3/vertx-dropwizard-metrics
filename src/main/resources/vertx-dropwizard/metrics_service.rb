require 'vertx/measured'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.dropwizard.MetricsService
module VertxDropwizard
  #  The metrics service mainly allows to return a snapshot of measured objects.
  class MetricsService
    # @private
    # @param j_del [::VertxDropwizard::MetricsService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxDropwizard::MetricsService] the underlying java delegate
    def j_del
      @j_del
    end
    #  Creates a metric service for a given {::Vertx::Vertx} instance.
    # @param [::Vertx::Vertx] vertx the vertx instance
    # @return [::VertxDropwizard::MetricsService] the metrics service
    def self.create(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::VertxDropwizard::MetricsService.new(Java::IoVertxExtDropwizard::MetricsService.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del))
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx)"
    end
    #  Will return the metrics that correspond with this measured object, null if no metrics is available.<p/>
    # 
    #  Note: in the case of scaled servers, the JsonObject returns an aggregation of the metrics as the
    #  dropwizard backend reports to a single server.
    # @param [::Vertx::Measured] o
    # @return [Hash{String => Object}] the map of metrics where the key is the name of the metric (excluding the base name) and the value is the json data representing that metric
    def get_metrics_snapshot(o=nil)
      if o.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:getMetricsSnapshot, [Java::IoVertxCoreMetrics::Measured.java_class]).call(o.j_del) != nil ? JSON.parse(@j_del.java_method(:getMetricsSnapshot, [Java::IoVertxCoreMetrics::Measured.java_class]).call(o.j_del).encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_metrics_snapshot(o)"
    end
    #  @param measured the measure object
    # @param [::Vertx::Measured] measured
    # @return [String] the base name of the measured object
    def get_base_name(measured=nil)
      if measured.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:getBaseName, [Java::IoVertxCoreMetrics::Measured.java_class]).call(measured.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling get_base_name(measured)"
    end
  end
end
