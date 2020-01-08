package com.ap.uidgen.web.handlers;

/**
 * Basic validator for namespace query parameter
 * @author aparadis
 * @since 1.0.0
 */
public class NamespaceValidator
{
  private enum ValidationCode {
    OK,
    BLANK,
    INVALID_LENGTH,
    INVALID_CHARACTERS
  };

  private static final int MAX_LENGTH = 50;
  private static final String VALID_NAMESPACE_REGEX = "^\\w+$";

  private final String namespace;
  private ValidationCode code = ValidationCode.BLANK;

  public NamespaceValidator(String namespace) {
    this.namespace = namespace;
    validate();
  }

  public boolean isValid() {
    return code == ValidationCode.OK;
  }

  public String getValidationMessage() {
    String msg = "";
    switch(code) {
      case INVALID_CHARACTERS:
        msg = "Namespace must include only characters in [a-zA-Z_0-9]";
        break;
      case INVALID_LENGTH:
        msg = "Namespace length is invalid, must be between 1 and 50 alphanumerical characters";
        break;
      case BLANK:
        msg = "Namespace must not be blank. I must be between 1 and 50 alphanumerical characters";
        break;
      default:
        msg = "";
    }
    return msg;
  }

  private void validate() {
    if(namespace == null || namespace.length() == 0) {
      code = ValidationCode.BLANK;
    }
    else if(namespace.length() > MAX_LENGTH) {
      code = ValidationCode.INVALID_LENGTH;
    }
    else if(!namespace.matches(VALID_NAMESPACE_REGEX)) {
      code = ValidationCode.INVALID_CHARACTERS;
    }
    else {
      code = ValidationCode.OK;
    }
  }
}
