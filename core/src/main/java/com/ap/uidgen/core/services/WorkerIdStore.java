package com.ap.uidgen.core.services;

import java.util.Optional;

/**
 * Contract that all worker id provider must implement. A worker id store must return an unused id
 * if available.
 *
 * @author aparadis
 * @since 1.0.0
 */
public interface WorkerIdStore
{
  Optional<Integer> getAvailableWorkerId();
}
