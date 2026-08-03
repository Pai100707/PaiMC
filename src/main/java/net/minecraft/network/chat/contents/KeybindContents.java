package net.minecraft.network.chat.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

public class KeybindContents implements ComponentContents {
   public static final MapCodec<KeybindContents> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.STRING.fieldOf("keybind").forGetter($$0x -> $$0x.name)).apply($$0, KeybindContents::new)
   );
   private final String name;
   @Nullable
   private Supplier<Component> nameResolver;

   public KeybindContents(String $$0) {
      this.name = $$0;
   }

   private Component getNestedComponent() {
      if (this.nameResolver == null) {
         this.nameResolver = KeybindResolver.keyResolver.apply(this.name);
      }

      return this.nameResolver.get();
   }

   @Override
   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
      return this.getNestedComponent().visit($$0);
   }

   @Override
   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
      return this.getNestedComponent().visit($$0, $$1);
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof KeybindContents $$1 && this.name.equals($$1.name);
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   @Override
   public String toString() {
      return "keybind{" + this.name + "}";
   }

   public String getName() {
      return this.name;
   }

   @Override
   public MapCodec<KeybindContents> codec() {
      return MAP_CODEC;
   }
}
