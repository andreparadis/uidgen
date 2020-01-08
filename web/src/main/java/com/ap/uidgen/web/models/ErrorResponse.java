package com.ap.uidgen.web.models;

import lombok.Builder;
import lombok.Getter;

/**
 * @author aparadis
 * @since x.x.x
 */
@Builder
@Getter
public class ErrorResponse
{
  private String message;
  private int code;
}
