package com.ap.uidgen.core.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.ap.uidgen.core.services.snowflake.SnowflakeUniqueIdGenerator;
import com.ap.uidgen.core.services.uuid.UUIDUniqueIdGenerator;

/**
 * @author aparadis
 * @since x.x.x
 */
public class UniqueIdGeneratorTest
{
  @Test
  public void testSnowflakeGenerator() {
    var generator = new SnowflakeUniqueIdGenerator(100);
    final var id1 = generator.generateUid("test");
    final var segments = id1.split("-");
    assertTrue(segments.length >= 2);
    assertEquals(segments[0], "test");
  }

  @Test
  public void snowflakeShouldRejectInvalidWorkerNode() {
    assertThrows(IllegalArgumentException.class, () -> {
      var generator = new SnowflakeUniqueIdGenerator(9999);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      var generator = new SnowflakeUniqueIdGenerator(-1);
    });
  }

  @Test
  public void snowflakeShouldRejectNullNamespace() {
    var generator = new SnowflakeUniqueIdGenerator(100);
    assertThrows(NullPointerException.class, () -> {
      var id = generator.generateUid(null);
    });
  }

  @Test
  public void testGUIDUidGenerator() {
    var generator = new UUIDUniqueIdGenerator();
    final var id1 = generator.generateUid("test");
    final var segments = id1.split("-");
    assertTrue(segments.length >= 2);
    assertEquals(segments[0], "test");
  }
}
