package io.vertx.ext.dropwizard.impl.shell;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandPack;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MetricsCommandPack implements CommandPack {

  @Override
  public void lookupCommands(Vertx vertx, Handler<AsyncResult<List<Command>>> handler) {
    handler.handle(Future.succeededFuture(Stream.of(MetricsInfo.class, MetricsLs.class).map(Command::create).collect(Collectors.toList())));
  }
}
