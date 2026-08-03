package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Unit;

public interface FormattedText {
   Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
   FormattedText EMPTY = new FormattedText() {
      @Override
      public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
         return Optional.empty();
      }

      @Override
      public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
         return Optional.empty();
      }
   };

   <T> Optional<T> visit(FormattedText.ContentConsumer<T> var1);

   <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> var1, Style var2);

   static FormattedText of(final String $$0) {
      return new FormattedText() {
         @Override
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0x) {
            return $$0.accept($$0);
         }

         @Override
         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0x, Style $$1) {
            return $$0.accept($$1, $$0);
         }
      };
   }

   static FormattedText of(final String $$0, final Style $$1) {
      return new FormattedText() {
         @Override
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0x) {
            return $$0.accept($$0);
         }

         @Override
         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0x, Style $$1x) {
            return $$0.accept($$1.applyTo($$1), $$0);
         }
      };
   }

   static FormattedText composite(FormattedText... $$0) {
      return composite(ImmutableList.copyOf($$0));
   }

   static FormattedText composite(final List<? extends FormattedText> $$0) {
      return new FormattedText() {
         @Override
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0x) {
            for (FormattedText $$1 : $$0) {
               Optional<T> $$2 = $$1.visit($$0);
               if ($$2.isPresent()) {
                  return $$2;
               }
            }

            return Optional.empty();
         }

         @Override
         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0x, Style $$1) {
            for (FormattedText $$2 : $$0) {
               Optional<T> $$3 = $$2.visit($$0, $$1);
               if ($$3.isPresent()) {
                  return $$3;
               }
            }

            return Optional.empty();
         }
      };
   }

   default String getString() {
      StringBuilder $$0 = new StringBuilder();
      this.visit($$1 -> {
         $$0.append($$1);
         return Optional.empty();
      });
      return $$0.toString();
   }

   public interface ContentConsumer<T> {
      Optional<T> accept(String var1);
   }

   public interface StyledContentConsumer<T> {
      Optional<T> accept(Style var1, String var2);
   }
}
