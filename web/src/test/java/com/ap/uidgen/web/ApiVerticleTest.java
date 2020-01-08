package com.ap.uidgen.web;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import java.net.ServerSocket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ap.uidgen.web.ApiConfiguration;
import com.ap.uidgen.web.ApiConfiguration.ConfigurationKeys;
import com.ap.uidgen.web.ApiMain;

/**
 * Test API endpoint
 * @author aparadis
 * @since x.x.x
 */
@ExtendWith(VertxExtension.class)
public class ApiVerticleTest
{
  private static int port;
  private static WebClient client;

  @BeforeAll
  public static void setup(Vertx vertx, VertxTestContext testContext) {
    final JsonObject config = getConfig();
    port = config.getInteger(ConfigurationKeys.SERVER_PORT.toString());

    client = WebClient.create(vertx);

    final int availableCores = Runtime.getRuntime().availableProcessors();
    ApiMain.deployVerticle(vertx, config, availableCores, testContext.completing());
  }

  @Test
  public void testGetUid(Vertx vertx, VertxTestContext testContext) {
    client
        .get(port, "localhost", "/uidapi/v1/uid/testns")
        .send(ar -> {
          if (ar.succeeded()) {
            // Obtain response
            HttpResponse<Buffer> response = ar.result();
            assertEquals(200, response.statusCode());
            assertTrue(response.bodyAsJsonObject().getString("uid").contains("testns-"));
            testContext.completeNow();
          } else {
            fail();
            testContext.completeNow();
          }
        });
  }

  @Test
  public void testGetUidInvalidNamespace(Vertx vertx, VertxTestContext testContext) {
    client
        .get(port, "localhost", "/uidapi/v1/uid/test--ns")
        .send(ar -> {
          if (ar.succeeded()) {
            // Obtain response
            HttpResponse<Buffer> response = ar.result();
            assertEquals(400, response.statusCode());
            assertTrue(response.bodyAsJsonObject().getInteger("code") == 400);
            testContext.completeNow();
          } else {
            fail();
            testContext.completeNow();
          }
        });
  }

  private static JsonObject getConfig()
  {
    JsonObject config = null;
    try
    {
      ServerSocket socket = new ServerSocket(0);
      int port = socket.getLocalPort();
      socket.close();

      config = new JsonObject();
      config.put(ConfigurationKeys.SERVER_PORT.toString(), port);
      config.put(ConfigurationKeys.UID_GENERATOR.toString(), "snowflake");
      config.put(ConfigurationKeys.CORS_ORIGIN_REGEX.toString(), "*");
      config.put(ConfigurationKeys.BASE_WORKER_ID.toString(), 0);
    }
    catch(Throwable t)
    {
      throw new IllegalStateException(t.getMessage(), t);
    }

    return config;
  }


}
