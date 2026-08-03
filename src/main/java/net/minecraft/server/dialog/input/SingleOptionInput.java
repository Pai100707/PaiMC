package net.minecraft.server.dialog.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.ExtraCodecs;

public record SingleOptionInput(int width, List<SingleOptionInput.Entry> entries, Component label, boolean labelVisible) implements InputControl {
   public static final MapCodec<SingleOptionInput> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(SingleOptionInput::width),
               ExtraCodecs.nonEmptyList(SingleOptionInput.Entry.CODEC.listOf()).fieldOf("options").forGetter(SingleOptionInput::entries),
               ComponentSerialization.CODEC.fieldOf("label").forGetter(SingleOptionInput::label),
               Codec.BOOL.optionalFieldOf("label_visible", true).forGetter(SingleOptionInput::labelVisible)
            )
            .apply($$0, SingleOptionInput::new)
      )
      .validate($$0 -> {
         long $$1 = $$0.entries.stream().filter(SingleOptionInput.Entry::initial).count();
         return $$1 > 1L ? DataResult.error(() -> "Multiple initial values") : DataResult.success($$0);
      });

   @Override
   public MapCodec<SingleOptionInput> mapCodec() {
      return MAP_CODEC;
   }

   public Optional<SingleOptionInput.Entry> initial() {
      return this.entries.stream().filter(SingleOptionInput.Entry::initial).findFirst();
   }

   public record Entry(String id, Optional<Component> display, boolean initial) {
      public static final Codec<SingleOptionInput.Entry> FULL_CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.STRING.fieldOf("id").forGetter(SingleOptionInput.Entry::id),
               ComponentSerialization.CODEC.optionalFieldOf("display").forGetter(SingleOptionInput.Entry::display),
               Codec.BOOL.optionalFieldOf("initial", false).forGetter(SingleOptionInput.Entry::initial)
            )
            .apply($$0, SingleOptionInput.Entry::new)
      );
      public static final Codec<SingleOptionInput.Entry> CODEC = Codec.withAlternative(
         FULL_CODEC, Codec.STRING, $$0 -> new SingleOptionInput.Entry($$0, Optional.empty(), false)
      );

      public Component displayOrDefault() {
         return this.display.orElseGet(() -> Component.literal(this.id));
      }
   }
}
