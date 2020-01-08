package com.ap.uidgen.core.services.snowflake;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import com.ap.uidgen.core.services.UniqueIdGenerator;

/**
 *  Uid generator implementation using twitter snowflake UIDs. See
 *  {@link SnowflakeUid} for details
 *
 *  The Snowflake implementation is deliberately built without synchronization to avoid performance
 *  hit. Each verticle instance deployed is guaranteed to be executed by a single thread at a time
 *  by vert.x, thus providing safe usage without synchronization.
 *
 *  Since ApiVerticle instances are running with their own thread, they each use a dedicated
 *  instance of the generator configured with a different worker id.
 *
 *  This allows id generation without contention in the generator, while ensuring no colliding IDs
 *  are emitted by concurrently running verticles since they all use a different worker id.
 *
 * @author aparadis
 * @since 1.0.0
 */
@Slf4j
public class SnowflakeUniqueIdGenerator implements UniqueIdGenerator
{
  // dedicated instance of a snowflake
  private final SnowflakeUid snowflake;

  public SnowflakeUniqueIdGenerator(int nodeId) {
    this.snowflake = new SnowflakeUid(nodeId);
  }

  /**
   * generateId is not thread safe, but this id generator instance is used by a single verticle
   * instance. Verticle handlers are guaranteed to be executed by a single thread at a time.
   * @param namespace
   * @return a unique id
   */
  @Override
  public String generateUid(@NonNull final String namespace)
  {
    log.info("generating new uid");
    return namespace + "-" + snowflake.generateId();
  }
}
