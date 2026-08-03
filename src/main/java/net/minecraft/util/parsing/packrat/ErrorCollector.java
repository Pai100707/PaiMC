package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;

public interface ErrorCollector<S> {
   void store(int var1, SuggestionSupplier<S> var2, Object var3);

   default void store(int $$0, Object $$1) {
      this.store($$0, SuggestionSupplier.empty(), $$1);
   }

   void finish(int var1);

   public static class LongestOnly<S> implements ErrorCollector<S> {
      private ErrorCollector.LongestOnly.MutableErrorEntry<S>[] entries = new ErrorCollector.LongestOnly.MutableErrorEntry[16];
      private int nextErrorEntry;
      private int lastCursor = -1;

      private void discardErrorsFromShorterParse(int $$0) {
         if ($$0 > this.lastCursor) {
            this.lastCursor = $$0;
            this.nextErrorEntry = 0;
         }
      }

      @Override
      public void finish(int $$0) {
         this.discardErrorsFromShorterParse($$0);
      }

      @Override
      public void store(int $$0, SuggestionSupplier<S> $$1, Object $$2) {
         this.discardErrorsFromShorterParse($$0);
         if ($$0 == this.lastCursor) {
            this.addErrorEntry($$1, $$2);
         }
      }

      private void addErrorEntry(SuggestionSupplier<S> $$0, Object $$1) {
         int $$2 = this.entries.length;
         if (this.nextErrorEntry >= $$2) {
            int $$3 = net.minecraft.util.Util.growByHalf($$2, this.nextErrorEntry + 1);
            ErrorCollector.LongestOnly.MutableErrorEntry<S>[] $$4 = new ErrorCollector.LongestOnly.MutableErrorEntry[$$3];
            System.arraycopy(this.entries, 0, $$4, 0, $$2);
            this.entries = $$4;
         }

         int $$5 = this.nextErrorEntry++;
         ErrorCollector.LongestOnly.MutableErrorEntry<S> $$6 = this.entries[$$5];
         if ($$6 == null) {
            $$6 = new ErrorCollector.LongestOnly.MutableErrorEntry<>();
            this.entries[$$5] = $$6;
         }

         $$6.suggestions = $$0;
         $$6.reason = $$1;
      }

      public List<ErrorEntry<S>> entries() {
         int $$0 = this.nextErrorEntry;
         if ($$0 == 0) {
            return List.of();
         } else {
            List<ErrorEntry<S>> $$1 = new ArrayList<>($$0);

            for (int $$2 = 0; $$2 < $$0; $$2++) {
               ErrorCollector.LongestOnly.MutableErrorEntry<S> $$3 = this.entries[$$2];
               $$1.add(new ErrorEntry<>(this.lastCursor, $$3.suggestions, $$3.reason));
            }

            return $$1;
         }
      }

      public int cursor() {
         return this.lastCursor;
      }

      static class MutableErrorEntry<S> {
         SuggestionSupplier<S> suggestions = SuggestionSupplier.empty();
         Object reason = "empty";
      }
   }

   public static class Nop<S> implements ErrorCollector<S> {
      @Override
      public void store(int $$0, SuggestionSupplier<S> $$1, Object $$2) {
      }

      @Override
      public void finish(int $$0) {
      }
   }
}
