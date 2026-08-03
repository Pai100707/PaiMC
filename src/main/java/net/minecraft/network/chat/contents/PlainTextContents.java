package net.minecraft.network.chat.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public interface PlainTextContents extends ComponentContents {
   MapCodec<PlainTextContents> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.STRING.fieldOf("text").forGetter(PlainTextContents::text)).apply($$0, PlainTextContents::create)
   );
   PlainTextContents EMPTY = new PlainTextContents() {
      @Override
      public String toString() {
         return "empty";
      }

      @Override
      public String text() {
         return "";
      }
   };

   static PlainTextContents create(String $$0) {
      return (PlainTextContents)($$0.isEmpty() ? EMPTY : new PlainTextContents.LiteralContents($$0));
   }

   String text();

   @Override
   default MapCodec<PlainTextContents> codec() {
      return MAP_CODEC;
   }

   public record LiteralContents(String text) implements PlainTextContents {
      @Override
      public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
         return $$0.accept(this.text);
      }

      @Override
      public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
         return $$0.accept($$1, this.text);
      }

      @Override
      public String toString() {
         return "literal{" + this.text + "}";
      }
   }
}
