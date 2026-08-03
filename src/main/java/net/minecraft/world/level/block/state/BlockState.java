package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockState extends BlockBehaviour.BlockStateBase {
   public static final Codec<BlockState> CODEC = codec(BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState).stable();

   public BlockState(Block $$0, Reference2ObjectArrayMap<Property<?>, Comparable<?>> $$1, MapCodec<BlockState> $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected BlockState asState() {
      return this;
   }
}
