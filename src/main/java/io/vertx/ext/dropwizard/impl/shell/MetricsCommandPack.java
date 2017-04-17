package io.vertx.ext.dropwizard.impl.shell;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandResolver;
import io.vertx.ext.shell.spi.CommandResolverFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MetricsCommandPack implements CommandResolverFactory {

  @Override
  public void resolver(Vertx vertx, Handler<AsyncResult<CommandResolver>> handler) {
    handler.handle(Future.succeededFuture(() -> Stream.of(MetricsInfo.class, MetricsLs.class, MetricsHistogram.class)
      .map(cmd -> Command.create(vertx, cmd)).collect(Collectors.toList())));
  }
}
