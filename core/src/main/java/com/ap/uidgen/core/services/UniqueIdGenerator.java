package com.ap.uidgen.core.services;

/**
 * Contract all uid generators must implement. Given a namespace, it returns a unique id consisting
 * of a namespace prefixed value.
 *
 * @author aparadis
 * @since 1.0.0
 */
public interface UniqueIdGenerator
{
  String generateUid(String namespace);
}
