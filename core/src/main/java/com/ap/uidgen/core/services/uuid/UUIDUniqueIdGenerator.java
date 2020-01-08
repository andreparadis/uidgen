package com.ap.uidgen.core.services.uuid;

import java.util.UUID;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import com.ap.uidgen.core.services.UniqueIdGenerator;

/**
 * Alternate implementation using regular uuid v4. This will be slower under high load because UUID
 * uses a synchronized access to a random number generator.
 *
 * @author aparadis
 * @since x.x.x
 */
@Slf4j
public class UUIDUniqueIdGenerator implements UniqueIdGenerator
{
  @Override
  public String generateUid(@NonNull final String namespace)
  {
    log.info("generating new uid");

    return namespace + "-" + UUID.randomUUID().toString();
  }
}
