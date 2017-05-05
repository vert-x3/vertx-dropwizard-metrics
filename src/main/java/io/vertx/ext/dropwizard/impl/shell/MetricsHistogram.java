package io.vertx.ext.dropwizard.impl.shell;

import io.vertx.core.Vertx;
import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.shell.command.AnnotatedCommand;
import io.vertx.ext.shell.command.CommandProcess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

@Name("metrics-histogram")
@Summary("Show histogram metrics for the current Vert.x instance in real time")
public class MetricsHistogram extends AnnotatedCommand {
  private String name;
  private List<String> columns;
  private List<String> headerColumns = new ArrayList<>(Arrays.asList("NAME", "COUNT", "MEDIAN"));
  private List<String> availableColumns = Arrays.asList("MIN", "MAX", "75%", "95%", "99%", "99.9%");

  @Argument(index = 0, argName = "name")
  @Description("The histogram metrics name, can be a prefix or a precise name")
  public void setName(String name) {
    this.name = name;
  }

  @Argument(index = 1, argName = "columns", required = false)
  @Description("The configurable histogram metrics columns to show [min max 75% 95% 98% 99% 99.9%]")
  public void setColumns(List<String> columns) {
    this.columns = columns;
  }

  @Override
  public void process(CommandProcess process) {
    Vertx vertx = process.vertx();
    MetricsService metrics = MetricsService.create(vertx);

    headerColumns.addAll(columns
      .stream()
      .map(String::trim)
      .distinct()
      .map(String::toUpperCase)
      .filter(f -> availableColumns.contains(f))
      .collect(Collectors.toList()));

    StringBuilder output = new StringBuilder();
    Formatter formatter = new Formatter(output);

    // For NAME, COUNT and MEDIAN (Default)
    StringBuilder format = new StringBuilder();
    format.append(" ");
    format.append("%1$-50s %2$-10s %3$-10s");

    // For MIN, MAX, 75%, 95%, 99% and 99.9% (Configurable)
    if(headerColumns.size() > 3) {
      for (int i = 3; i < headerColumns.size(); i++) {
        format.append(" %" + (i + 1) + "$-10s");
      }
    }

    long timer = vertx.setPeriodic(1000, id -> {

      Map<String, Object> histogramMetrics = metrics.getMetricsSnapshot(name)
        .stream()
        .filter(e -> ((JsonObject) e.getValue()).getString("type").equals("histogram"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      List<String> histogramKeys = new ArrayList<>(histogramMetrics.keySet());

      for (int i = 1; i <= process.height(); i++) {
        output.append("\033[").append(i).append(";1H\033[K");
        if(i == 1) {
          formatter.format(format.toString(), headerColumns.toArray());
        } else {
          int index = i - 2;
          if (index < histogramKeys.size()) {
            String key = histogramKeys.get(index);
            JsonObject data = (JsonObject) histogramMetrics.get(key);
            List<Object> dataColumns = new ArrayList<>();
            dataColumns.add(key);
            for (String header : headerColumns) {
              if(header.equals("NAME"))
                continue;
              dataColumns.add(data.getValue(header.toLowerCase()));
            }
            formatter.format(format.toString(), dataColumns.toArray());
          }
        }
      }
      process.write(output.toString());
    });
    process.interruptHandler(i -> {
      vertx.cancelTimer(timer);
      process.end();
    });
  }
}