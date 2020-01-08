package com.ap.uidgen.web.handlers;

import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.ap.uidgen.core.services.UniqueIdGenerator;
import com.ap.uidgen.web.models.ErrorResponse;
import com.ap.uidgen.web.models.GetIdResponse;

/**
 * @author aparadis
 * @since 1.0.0
 */
public class ApiHandler
{
  private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);

  private static final String NAMESPACE_PARAM_NAME = "namespace";

  private UniqueIdGenerator uidGenerator;

  public ApiHandler(UniqueIdGenerator generator) {
    this.uidGenerator = generator;
  }

  /**
   * Generate a unique id given a valid namespace is provided.
   * @param routingContext
   */
  public void handleGetId(RoutingContext routingContext) {
    logger.info("handleGetId called");

    final String namespace = routingContext.pathParam(NAMESPACE_PARAM_NAME);
    final NamespaceValidator validator = new NamespaceValidator(namespace);

    if(validator.isValid() == false) {
      logger.warn("Invalid namespace provided {}", namespace);
      final ErrorResponse error = ErrorResponse
          .builder()
          .message(validator.getValidationMessage())
          .code(400)
          .build();

      routingContext.response()
          .setStatusCode(400)
          .end(Json.encodePrettily(error));
    }
    else {
      final String newUid = uidGenerator.generateUid(namespace);
      final GetIdResponse response = GetIdResponse.builder().uid(newUid).build();

      routingContext.response()
          .setStatusCode(200)
          .end(Json.encodePrettily(response));
    }
  }
}
