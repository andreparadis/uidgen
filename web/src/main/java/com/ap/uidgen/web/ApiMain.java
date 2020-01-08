package com.ap.uidgen.web;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.ap.uidgen.web.guice.ApiModule;
import com.ap.uidgen.web.guice.GuiceVerticleFactory;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Slf4jReporter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


/**
 * @author aparadis
 * @since 1.0.0
 */
public class ApiMain
{
  public static void main(String[] args) {

    // set logging override for SLF4j.
    System.setProperty(
        "vertx.logger-delegate-factory-class-name",
        "io.vertx.core.logging.SLF4JLogDelegateFactory");

    // set metrics
    String registryName = "registry";
    MetricRegistry registry = SharedMetricRegistries.getOrCreate(registryName);
    SharedMetricRegistries.setDefault(registryName);

    Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
        .outputTo(LoggerFactory.getLogger("uidapi-metrics"))
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
    //reporter.start(30, TimeUnit.SECONDS);

    // Initialize vertx with the metric registry
    DropwizardMetricsOptions metricsOptions = new DropwizardMetricsOptions()
        .setEnabled(true)
        .setMetricRegistry(registry);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(metricsOptions);

    // init vertx runtime
    Vertx vertx = Vertx.vertx(vertxOptions);

    // deploy one verticle per core available
    final int availableCores = Runtime.getRuntime().availableProcessors();

    // load configuration and deploy verticle.
    ConfigRetrieverOptions configRetrieverOptions = ApiConfiguration.getConfigRetrieverOptions();
    ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);
    configRetriever.getConfig(config -> {
      deployVerticle(vertx, config.result(), availableCores, null);
    });
  }

  public static void deployVerticle(
      final Vertx vertx,
      final JsonObject config,
      int nbCores,
      final Handler<AsyncResult<String>> asyncAssertSuccess) {

    // set guice verticle factory
    GuiceVerticleFactory guiceVerticleFactory =
        new GuiceVerticleFactory(createInjector(vertx, config));
    vertx.registerVerticleFactory(guiceVerticleFactory);
    DeploymentOptions deploymentOptions = new DeploymentOptions()
        .setInstances(nbCores)
        .setConfig(config);

    if(asyncAssertSuccess != null) {
      vertx.deployVerticle(getPrefixedVerticleName(), deploymentOptions, asyncAssertSuccess);
    }
    else {
      vertx.deployVerticle(getPrefixedVerticleName(), deploymentOptions);
    }
  }

  private static String getPrefixedVerticleName() {
    return GuiceVerticleFactory.PREFIX + ":" + ApiVerticle.class.getCanonicalName();
  }

  private static Injector createInjector(Vertx vertx, JsonObject config) {
    return Guice.createInjector(getModules(vertx, config));
  }

  private static List<Module> getModules(Vertx vertx, JsonObject config) {
    List<Module> modules = new LinkedList<>();
    modules.add(new ApiModule(vertx, config));
    return modules;
  }
}
