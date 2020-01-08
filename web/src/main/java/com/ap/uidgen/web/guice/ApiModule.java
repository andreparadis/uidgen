package com.ap.uidgen.web.guice;


import com.ap.uidgen.core.services.SequentialWorkerIdStore;
import com.ap.uidgen.core.services.uuid.UUIDUniqueIdGenerator;
import com.ap.uidgen.core.services.snowflake.SnowflakeUniqueIdGenerator;
import com.ap.uidgen.core.services.UniqueIdGenerator;
import com.ap.uidgen.core.services.WorkerIdStore;
import com.ap.uidgen.web.ApiConfiguration.ConfigurationKeys;
import com.ap.uidgen.web.handlers.ApiHandler;
import com.ap.uidgen.web.handlers.FailureHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;

import java.security.InvalidParameterException;
import java.util.Optional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Module defining dependencies for our verticle.
 *
 * @author aparadis
 * @since 1.0.0
 */
@Slf4j
public class ApiModule extends AbstractModule {

  private final Vertx vertx;
  private final JsonObject config;

  public ApiModule(@NonNull Vertx vertx, @NonNull JsonObject config) {
    this.vertx = vertx;
    this.config = config;
  }

  @Override
  protected void configure() {
    bind(Vertx.class).toInstance(this.vertx);
    bind(EventBus.class).toInstance(this.vertx.eventBus());
    bind(FileSystem.class).toInstance(this.vertx.fileSystem());
    bind(SharedData.class).toInstance(this.vertx.sharedData());
    bind(JsonObject.class).toInstance(this.config);
  }

  @Provides
  public ApiHandler provideApiHandler(@NonNull final UniqueIdGenerator generator) {
    return new ApiHandler(generator);
  }

  @Provides
  public FailureHandler provideFailureHandler() {
    return new FailureHandler();
  }

  @Provides
  public UniqueIdGenerator provideUidGenerator(
      @NonNull final JsonObject config,
      @NonNull final WorkerIdStore idStore) {

    UniqueIdGenerator implementation;

    final String generatorName = config.getString(ConfigurationKeys.UID_GENERATOR.toString());
    switch(generatorName) {
      case "uuid":
        implementation = buildUUIDNamespacedUniqueIdGenerator();
        break;
      case "snowflake":
        implementation = buildSnowflakeNamespacedUniqueIdGenerator(idStore);
        break;
      default:
        throw new InvalidParameterException("implementation not supported: " + generatorName);
    }
    return implementation;
  }

  @Provides
  @Singleton
  public WorkerIdStore provideWorkerIdStore() {
    int baseIndex = config.getInteger(ConfigurationKeys.BASE_WORKER_ID.toString());
    return new SequentialWorkerIdStore(baseIndex);
  }

  private UUIDUniqueIdGenerator buildUUIDNamespacedUniqueIdGenerator() {
    log.info("Building instance of id generator: {}",
        UUIDUniqueIdGenerator.class.getCanonicalName());

    return new UUIDUniqueIdGenerator();
  }

  private SnowflakeUniqueIdGenerator buildSnowflakeNamespacedUniqueIdGenerator(
      final WorkerIdStore idStore) {

    final Optional<Integer> workerId = idStore.getAvailableWorkerId();
    if(!workerId.isPresent()) {
      log.error("Unable to get available id from worker id store");
      throw new IllegalStateException("Could not get available worker id from store");
    }

    log.info("Building instance of id generator {} with worker id {}",
        SnowflakeUniqueIdGenerator.class.getCanonicalName(),
        workerId.get());

    return new SnowflakeUniqueIdGenerator(workerId.get());
  }
}
