package com.ap.uidgen.core.services.snowflake;

import java.time.Instant;

/**
 * Twitter snowflake uid generator. Generates a unique id based on a time component, a unique worker node id
 * and a sequence number. These IDs a roughly emitted in order across nodes, leading to good
 * indexing performance.
 *
 * 42 bits of time components gives us 2^42 - 1 milliseconds worth of IDs (almost 140 years) of
 * operation.
 *
 * @author aparadis
 * @since 1.0.0
 */
public class SnowflakeUid
{
  // number of bits reserved for the worker id
  private static final long WORKER_ID_BITS = 10L;

  // number of bits reserved for the sequence number
  private static final long SEQUENCE_BITS = 12L;

  // max sequence mask based on number of bits
  private static final int MAX_SEQUENCE_VALUE = (int)(Math.pow(2, SEQUENCE_BITS) - 1);

  // max node id based on number of bits
  private static final int MAX_WORKER_ID_VALUE = (int)(Math.pow(2, WORKER_ID_BITS) - 1);

  // custom epoch to get number of milliseconds from a recent reference to maximize usage of the
  // 42 bits.
  // IMPORTANT: This must be not be changed in later versions of this generators. Set to
  // Wednesday, January 1, 2020 0:00:00 GMT
  private static final long EPOCH_REFERENCE = 1577836800000L;

  // unique id identifying this process in a cluster
  private final long workerId;

  // avoid cpu cache for these values.
  private volatile long lastTimestamp;
  private volatile long sequence;

  public SnowflakeUid(final long workerId) {
    if(workerId < 0 || workerId > MAX_WORKER_ID_VALUE) {
      throw new IllegalArgumentException(String.format("worker id must be between %d and %d", 0,
          MAX_WORKER_ID_VALUE));
    }

    this.workerId = workerId;
    this.lastTimestamp = 0L;
    this.sequence = 0L;
  }

  /**
   * Calculate number of milliseconds relative to a custom epoch
   * @return number of milliseconds
   */
  private long getTimestamp() {
    return Instant.now().toEpochMilli() - EPOCH_REFERENCE;
  }

  /**
   * Generate a unique 64 bits id using 42 bits of timestamp, 10 bits worker id and 12 bits
   * sequence.
   *
   * IMPORTANT: this method is not thread safe to avoid synchronization hit. The caller should
   * provide make sure this method is called by a single thread at at a time
   *
   * @return a string representation of the unsigned long id.
   */
  public String generateId() {
    long timestamp = getTimestamp();
    if(timestamp < lastTimestamp) {
      throw new IllegalStateException("Invalid timestamp acquired");
    }

    // critical section:
    // deliberately avoiding synchronization lock to allow fastest

    if (timestamp == lastTimestamp) {
      // id generated in the same millisecond, need to increment sequence to have unique id.
      // On a speedy host, we could max out sequence so detect this condition.
      sequence = (sequence + 1) & MAX_SEQUENCE_VALUE;
      if(sequence == 0) {
        // Sequence overflow, wait till next millisecond.
        timestamp = waitForNextMillis(timestamp);
      }
    } else {
      // next millisecond, we can reset the sequence
      sequence = 0;
    }

    // keep track of time
    lastTimestamp = timestamp;

    // end of critical section


    long id = timestamp << (WORKER_ID_BITS + SEQUENCE_BITS);
    id |= workerId << SEQUENCE_BITS;
    id |= sequence;

    // generate string representation of this id using unsigned long representation
    return Long.toUnsignedString(id);
  }

  /**
   * busy wait till next timestamp. This will happen only when MAX_SEQUENCE_VALUE IDs have been
   * generated under 1ms.
   *
   * @param currentTimestamp
   * @return the next timestamp different than the supplied one
   */
  private long waitForNextMillis(long currentTimestamp) {
    while (currentTimestamp == lastTimestamp) {
      currentTimestamp = getTimestamp();
    }
    return currentTimestamp;
  }
}
