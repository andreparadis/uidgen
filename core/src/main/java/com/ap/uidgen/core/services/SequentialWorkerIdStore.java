package com.ap.uidgen.core.services;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic implementation of worker id store. It generates sequential number from an initial seed
 *
 * @author aparadis
 * @since x.x.x
 */
public class SequentialWorkerIdStore implements WorkerIdStore
{
  private final AtomicInteger sequence;

  public SequentialWorkerIdStore(int baseIndex) {
    this.sequence = new AtomicInteger(baseIndex);
  }

  @Override
  public Optional<Integer> getAvailableWorkerId() {
    return Optional.of(sequence.getAndAdd(1));
  }
}
