package com.ap.uidgen.web.guice;

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;

import lombok.NonNull;

import com.google.inject.Injector;

/**
 * @author aparadis
 * @since x.x.x
 */
public class GuiceVerticleFactory implements VerticleFactory
{
  public static final String PREFIX = "java-guice";

  private final Injector injector;

  public GuiceVerticleFactory(@NonNull final Injector injector) {
    this.injector = injector;
  }

  @Override
  public String prefix()
  {
    return PREFIX;
  }

  @Override
  public Verticle createVerticle(
      @NonNull final String prefixedName,
      @NonNull final ClassLoader classLoader)
      throws Exception
  {
    final String verticleName = VerticleFactory.removePrefix(prefixedName);

    final Class clazz = classLoader.loadClass(verticleName);
    return (Verticle) this.injector.getInstance(clazz);
  }
}

