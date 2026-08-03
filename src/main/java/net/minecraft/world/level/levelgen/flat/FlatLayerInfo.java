package net.minecraft.world.level.levelgen.flat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public class FlatLayerInfo {
   public static final Codec<FlatLayerInfo> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(FlatLayerInfo::getHeight),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").orElse(Blocks.AIR).forGetter($$0x -> $$0x.getBlockState().getBlock())
         )
         .apply($$0, FlatLayerInfo::new)
   );
   private final Block block;
   private final int height;

   public FlatLayerInfo(int $$0, Block $$1) {
      this.height = $$0;
      this.block = $$1;
   }

   public int getHeight() {
      return this.height;
   }

   public BlockState getBlockState() {
      return this.block.defaultBlockState();
   }

   public FlatLayerInfo heightLimited(int $$0) {
      return this.height > $$0 ? new FlatLayerInfo($$0, this.block) : this;
   }

   @Override
   public String toString() {
      return (this.height != 1 ? this.height + "*" : "") + BuiltInRegistries.BLOCK.getKey(this.block);
   }
}
