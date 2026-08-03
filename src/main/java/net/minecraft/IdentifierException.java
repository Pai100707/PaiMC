package net.minecraft;

import org.apache.commons.lang3.StringEscapeUtils;

public class IdentifierException extends RuntimeException {
   public IdentifierException(String $$0) {
      super(StringEscapeUtils.escapeJava($$0));
   }

   public IdentifierException(String $$0, Throwable $$1) {
      super(StringEscapeUtils.escapeJava($$0), $$1);
   }
}
