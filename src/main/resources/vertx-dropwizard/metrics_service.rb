require 'vertx/measured'
require 'vertx/vertx'
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
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == MetricsService
    end
    def @@j_api_type.wrap(obj)
      MetricsService.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtDropwizard::MetricsService.java_class
    end
    #  Creates a metric service for a given {::Vertx::Vertx} instance.
    # @param [::Vertx::Vertx] vertx the vertx instance
    # @return [::VertxDropwizard::MetricsService] the metrics service
    def self.create(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtDropwizard::MetricsService.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxDropwizard::MetricsService)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{vertx})"
    end
    # @param [::Vertx::Measured] measured the measure object
    # @return [String] the base name of the measured object
    def get_base_name(measured=nil)
      if measured.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:getBaseName, [Java::IoVertxCoreMetrics::Measured.java_class]).call(measured.j_del)
      end
      raise ArgumentError, "Invalid arguments when calling get_base_name(#{measured})"
    end
    # @return [Set<String>] the known metrics names by this service
    def metrics_names
      if !block_given?
        return ::Vertx::Util::Utils.to_set(@j_del.java_method(:metricsNames, []).call()).map! { |elt| elt }
      end
      raise ArgumentError, "Invalid arguments when calling metrics_names()"
    end
    #  Will return the metrics that begins with the <code>baseName</code>, null if no metrics is available.<p/>
    # 
    #  Note: in the case of scaled servers, the JsonObject returns an aggregation of the metrics as the
    #  dropwizard backend reports to a single server.
    # @overload getMetricsSnapshot(measured)
    #   @param [::Vertx::Measured] measured 
    # @overload getMetricsSnapshot(baseName)
    #   @param [String] baseName 
    # @return [Hash{String => Object}] the map of metrics where the key is the name of the metric and the value is the json data representing that metric
    def get_metrics_snapshot(param_1=nil)
      if param_1.class.method_defined?(:j_del) && !block_given?
        return @j_del.java_method(:getMetricsSnapshot, [Java::IoVertxCoreMetrics::Measured.java_class]).call(param_1.j_del) != nil ? JSON.parse(@j_del.java_method(:getMetricsSnapshot, [Java::IoVertxCoreMetrics::Measured.java_class]).call(param_1.j_del).encode) : nil
      elsif param_1.class == String && !block_given?
        return @j_del.java_method(:getMetricsSnapshot, [Java::java.lang.String.java_class]).call(param_1) != nil ? JSON.parse(@j_del.java_method(:getMetricsSnapshot, [Java::java.lang.String.java_class]).call(param_1).encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_metrics_snapshot(#{param_1})"
    end
  end
end
