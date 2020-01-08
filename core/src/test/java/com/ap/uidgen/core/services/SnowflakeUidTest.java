package com.ap.uidgen.core.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.ap.uidgen.core.services.snowflake.SnowflakeUid;

/**
 * @author aparadis
 * @since 1.0.0
 */
public class SnowflakeUidTest
{
  @Test
  public void testSnowflakeId() {
    final var snowflake = new SnowflakeUid(100);
    final String id1 = snowflake.generateId();
    final String id2 = snowflake.generateId();
    assertNotSame(id1, id2);
  }
}
