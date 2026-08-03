package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public record DebugStickState(Map<Holder<Block>, Property<?>> properties) {
   public static final DebugStickState EMPTY = new DebugStickState(Map.of());
   public static final Codec<DebugStickState> CODEC = Codec.dispatchedMap(
         BuiltInRegistries.BLOCK.holderByNameCodec(), $$0 -> Codec.STRING.comapFlatMap($$1 -> {
            Property<?> $$2 = ((Block)$$0.value()).getStateDefinition().getProperty($$1);
            return $$2 != null ? DataResult.success($$2) : DataResult.error(() -> "No property on " + $$0.getRegisteredName() + " with name: " + $$1);
         }, Property::getName)
      )
      .xmap(DebugStickState::new, DebugStickState::properties);

   public DebugStickState withProperty(Holder<Block> $$0, Property<?> $$1) {
      return new DebugStickState(Util.copyAndPut(this.properties, $$0, $$1));
   }
}
