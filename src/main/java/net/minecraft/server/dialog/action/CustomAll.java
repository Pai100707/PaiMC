package net.minecraft.server.dialog.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Custom;
import net.minecraft.resources.Identifier;

public record CustomAll(Identifier id, Optional<CompoundTag> additions) implements Action {
   public static final MapCodec<CustomAll> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Identifier.CODEC.fieldOf("id").forGetter(CustomAll::id), CompoundTag.CODEC.optionalFieldOf("additions").forGetter(CustomAll::additions))
         .apply($$0, CustomAll::new)
   );

   @Override
   public MapCodec<CustomAll> codec() {
      return MAP_CODEC;
   }

   @Override
   public Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> $$0) {
      CompoundTag $$1 = this.additions.<CompoundTag>map(CompoundTag::copy).orElseGet(CompoundTag::new);
      $$0.forEach(($$1x, $$2) -> $$1.put($$1x, $$2.asTag()));
      return Optional.of(new Custom(this.id, Optional.of($$1)));
   }
}
