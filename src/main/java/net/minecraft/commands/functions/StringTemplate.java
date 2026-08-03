package net.minecraft.commands.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;

public record StringTemplate(List<String> segments, List<String> variables) {
   public static StringTemplate fromString(String $$0) {
      Builder<String> $$1 = ImmutableList.builder();
      Builder<String> $$2 = ImmutableList.builder();
      int $$3 = $$0.length();
      int $$4 = 0;
      int $$5 = $$0.indexOf(36);

      while ($$5 != -1) {
         if ($$5 != $$3 - 1 && $$0.charAt($$5 + 1) == '(') {
            $$1.add($$0.substring($$4, $$5));
            int $$6 = $$0.indexOf(41, $$5 + 1);
            if ($$6 == -1) {
               throw new IllegalArgumentException("Unterminated macro variable");
            }

            String $$7 = $$0.substring($$5 + 2, $$6);
            if (!isValidVariableName($$7)) {
               throw new IllegalArgumentException("Invalid macro variable name '" + $$7 + "'");
            }

            $$2.add($$7);
            $$4 = $$6 + 1;
            $$5 = $$0.indexOf(36, $$4);
         } else {
            $$5 = $$0.indexOf(36, $$5 + 1);
         }
      }

      if ($$4 == 0) {
         throw new IllegalArgumentException("No variables in macro");
      } else {
         if ($$4 != $$3) {
            $$1.add($$0.substring($$4));
         }

         return new StringTemplate($$1.build(), $$2.build());
      }
   }

   public static boolean isValidVariableName(String $$0) {
      for (int $$1 = 0; $$1 < $$0.length(); $$1++) {
         char $$2 = $$0.charAt($$1);
         if (!Character.isLetterOrDigit($$2) && $$2 != '_') {
            return false;
         }
      }

      return true;
   }

   public String substitute(List<String> $$0) {
      StringBuilder $$1 = new StringBuilder();

      for (int $$2 = 0; $$2 < this.variables.size(); $$2++) {
         $$1.append(this.segments.get($$2)).append($$0.get($$2));
         CommandFunction.checkCommandLineLength($$1);
      }

      if (this.segments.size() > this.variables.size()) {
         $$1.append(this.segments.getLast());
      }

      CommandFunction.checkCommandLineLength($$1);
      return $$1.toString();
   }
}
