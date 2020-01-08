package com.ap.uidgen.web.handlers;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * @author aparadis
 * @since x.x.x
 */
public class FailureHandler implements Handler<RoutingContext>
{
  private static final Logger logger = LoggerFactory.getLogger(FailureHandler.class);

  public void handle(RoutingContext context) {
    Throwable thrown = context.failure();
    recordError(thrown);
    context.response().setStatusCode(500).end();
  }

  private void recordError(Throwable throwable) {
    logger.error("error processing request", throwable);
  }
}
