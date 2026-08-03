package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import net.minecraft.network.chat.Style;

@FunctionalInterface
public interface FormattedCharSequence {
   net.minecraft.util.FormattedCharSequence EMPTY = $$0 -> true;

   boolean accept(net.minecraft.util.FormattedCharSink var1);

   static net.minecraft.util.FormattedCharSequence codepoint(int $$0, Style $$1) {
      return $$2 -> $$2.accept(0, $$1, $$0);
   }

   static net.minecraft.util.FormattedCharSequence forward(String $$0, Style $$1) {
      return $$0.isEmpty() ? EMPTY : $$2 -> net.minecraft.util.StringDecomposer.iterate($$0, $$1, $$2);
   }

   static net.minecraft.util.FormattedCharSequence forward(String $$0, Style $$1, Int2IntFunction $$2) {
      return $$0.isEmpty() ? EMPTY : $$3 -> net.minecraft.util.StringDecomposer.iterate($$0, $$1, decorateOutput($$3, $$2));
   }

   static net.minecraft.util.FormattedCharSequence backward(String $$0, Style $$1) {
      return $$0.isEmpty() ? EMPTY : $$2 -> net.minecraft.util.StringDecomposer.iterateBackwards($$0, $$1, $$2);
   }

   static net.minecraft.util.FormattedCharSequence backward(String $$0, Style $$1, Int2IntFunction $$2) {
      return $$0.isEmpty() ? EMPTY : $$3 -> net.minecraft.util.StringDecomposer.iterateBackwards($$0, $$1, decorateOutput($$3, $$2));
   }

   static net.minecraft.util.FormattedCharSink decorateOutput(net.minecraft.util.FormattedCharSink $$0, Int2IntFunction $$1) {
      return ($$2, $$3, $$4) -> $$0.accept($$2, $$3, (Integer)$$1.apply($$4));
   }

   static net.minecraft.util.FormattedCharSequence composite() {
      return EMPTY;
   }

   static net.minecraft.util.FormattedCharSequence composite(net.minecraft.util.FormattedCharSequence $$0) {
      return $$0;
   }

   static net.minecraft.util.FormattedCharSequence composite(net.minecraft.util.FormattedCharSequence $$0, net.minecraft.util.FormattedCharSequence $$1) {
      return fromPair($$0, $$1);
   }

   static net.minecraft.util.FormattedCharSequence composite(net.minecraft.util.FormattedCharSequence... $$0) {
      return fromList(ImmutableList.copyOf($$0));
   }

   static net.minecraft.util.FormattedCharSequence composite(List<net.minecraft.util.FormattedCharSequence> $$0) {
      int $$1 = $$0.size();
      switch ($$1) {
         case 0:
            return EMPTY;
         case 1:
            return $$0.get(0);
         case 2:
            return fromPair($$0.get(0), $$0.get(1));
         default:
            return fromList(ImmutableList.copyOf($$0));
      }
   }

   static net.minecraft.util.FormattedCharSequence fromPair(net.minecraft.util.FormattedCharSequence $$0, net.minecraft.util.FormattedCharSequence $$1) {
      return $$2 -> $$0.accept($$2) && $$1.accept($$2);
   }

   static net.minecraft.util.FormattedCharSequence fromList(List<net.minecraft.util.FormattedCharSequence> $$0) {
      return $$1 -> {
         for (net.minecraft.util.FormattedCharSequence $$2 : $$0) {
            if (!$$2.accept($$1)) {
               return false;
            }
         }

         return true;
      };
   }
}
