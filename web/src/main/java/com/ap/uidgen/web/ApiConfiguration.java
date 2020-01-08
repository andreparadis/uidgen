package com.ap.uidgen.web;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author aparadis
 * @since x.x.x
 */
public class ApiConfiguration
{
  private static final String FILE_NAME = "default.properties";

  public enum ConfigurationKeys {
    SERVER_PORT,
    BASE_WORKER_ID,
    CORS_ORIGIN_REGEX,
    UID_GENERATOR
  }

  public static ConfigRetrieverOptions getConfigRetrieverOptions() {
    // set classpath file options (all default values)
    JsonObject classpathFileConfiguration = new JsonObject().put("path", FILE_NAME);
    ConfigStoreOptions classpathFile =
        new ConfigStoreOptions()
            .setType("file")
            .setFormat("properties")
            .setConfig(classpathFileConfiguration);

    // set regular file system options (allows override using a file)
    JsonObject envFileConfiguration = new JsonObject().put("path", "/etc/uidgen/" + FILE_NAME);
    ConfigStoreOptions envFile =
        new ConfigStoreOptions()
            .setType("file")
            .setFormat("properties")
            .setConfig(envFileConfiguration)
            .setOptional(true);

    // set env variable names based on keys (allows overrides using env variables)
    JsonArray envVarKeys = new JsonArray();
    for (ConfigurationKeys key : ConfigurationKeys.values()) {
      envVarKeys.add(key.name());
    }
    JsonObject envVarConfiguration = new JsonObject().put("keys", envVarKeys);
    ConfigStoreOptions environment = new ConfigStoreOptions()
        .setType("env")
        .setConfig(envVarConfiguration)
        .setOptional(true);

    // merge all locations.
    return new ConfigRetrieverOptions()
        .addStore(classpathFile)
        .addStore(environment)
        .addStore(envFile);
  }
}
