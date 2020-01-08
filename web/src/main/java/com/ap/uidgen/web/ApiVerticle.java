package com.ap.uidgen.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;

import java.util.HashSet;
import java.util.Set;

import lombok.NonNull;

import com.ap.uidgen.web.ApiConfiguration.ConfigurationKeys;
import com.ap.uidgen.web.handlers.ApiHandler;
import com.ap.uidgen.web.handlers.FailureHandler;
import com.google.inject.Inject;


public class ApiVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(ApiVerticle.class);

  private ApiHandler handler;
  private FailureHandler failureHandler;

  @Inject
  public ApiVerticle(
      @NonNull final ApiHandler apiHandler,
      @NonNull final FailureHandler failureHandler) {
    this.failureHandler = failureHandler;
    this.handler = apiHandler;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    logger.info("Verticle {} instance starting", ApiVerticle.class.getCanonicalName());

    final int port = config().getInteger(ConfigurationKeys.SERVER_PORT.toString());

    vertx.createHttpServer()
        .requestHandler(createV1Router())
        .listen(port, http -> {
          if (http.succeeded()) {
            startPromise.complete();
            logger.info("Http server started on port {}", port);
          } else {
            logger.error("Failed to start http server");
            startPromise.fail(http.cause());
          }
        });
  }

  @Override
  public void stop() {
    logger.info("Shutting down application");
  }

  private Router createV1Router() {
    Router router = Router.router(vertx);

    // configure cors support
    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("x-requested-with");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("origin");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("accept");
    final String originRegex = config().getString(ConfigurationKeys.CORS_ORIGIN_REGEX.toString());
    router.route().handler(
        CorsHandler
            .create(originRegex)
            .allowedHeaders(allowedHeaders)
            .allowedMethod(HttpMethod.GET));

    // log all requests
    router.route("/uidapi/*").handler(LoggerHandler.create());

    // generate content-type
    router.route("/uidapi/*").handler(ResponseContentTypeHandler.create());

    // generate x-response-time header
    router.route("/uidapi/*").handler(ResponseTimeHandler.create());

    // GET uid/namespace
    router
        .get("/uidapi/v1/uid/:namespace")
        .handler(handler::handleGetId)
        .produces("application/json")
        .failureHandler(failureHandler);

    return router;
  }

}
