package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public class SubStringSource {
   private final String plainText;
   private final List<Style> charStyles;
   private final Int2IntFunction reverseCharModifier;

   private SubStringSource(String $$0, List<Style> $$1, Int2IntFunction $$2) {
      this.plainText = $$0;
      this.charStyles = ImmutableList.copyOf($$1);
      this.reverseCharModifier = $$2;
   }

   public String getPlainText() {
      return this.plainText;
   }

   public List<FormattedCharSequence> substring(int $$0, int $$1, boolean $$2) {
      if ($$1 == 0) {
         return ImmutableList.of();
      } else {
         List<FormattedCharSequence> $$3 = Lists.newArrayList();
         Style $$4 = this.charStyles.get($$0);
         int $$5 = $$0;

         for (int $$6 = 1; $$6 < $$1; $$6++) {
            int $$7 = $$0 + $$6;
            Style $$8 = this.charStyles.get($$7);
            if (!$$8.equals($$4)) {
               String $$9 = this.plainText.substring($$5, $$7);
               $$3.add($$2 ? FormattedCharSequence.backward($$9, $$4, this.reverseCharModifier) : FormattedCharSequence.forward($$9, $$4));
               $$4 = $$8;
               $$5 = $$7;
            }
         }

         if ($$5 < $$0 + $$1) {
            String $$10 = this.plainText.substring($$5, $$0 + $$1);
            $$3.add($$2 ? FormattedCharSequence.backward($$10, $$4, this.reverseCharModifier) : FormattedCharSequence.forward($$10, $$4));
         }

         return $$2 ? Lists.reverse($$3) : $$3;
      }
   }

   public static SubStringSource create(FormattedText $$0) {
      return create($$0, $$0x -> $$0x, $$0x -> $$0x);
   }

   public static SubStringSource create(FormattedText $$0, Int2IntFunction $$1, UnaryOperator<String> $$2) {
      StringBuilder $$3 = new StringBuilder();
      List<Style> $$4 = Lists.newArrayList();
      $$0.visit(($$2x, $$3x) -> {
         StringDecomposer.iterateFormatted($$3x, $$2x, ($$2xx, $$3xx, $$4x) -> {
            $$3.appendCodePoint($$4x);
            int $$5 = Character.charCount($$4x);

            for (int $$6 = 0; $$6 < $$5; $$6++) {
               $$4.add($$3xx);
            }

            return true;
         });
         return Optional.empty();
      }, Style.EMPTY);
      return new SubStringSource($$2.apply($$3.toString()), $$4, $$1);
   }
}
