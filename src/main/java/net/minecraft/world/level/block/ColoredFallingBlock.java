package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ColoredFallingBlock extends FallingBlock {
   public static final MapCodec<ColoredFallingBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ColorRGBA.CODEC.fieldOf("falling_dust_color").forGetter($$0x -> $$0x.dustColor), propertiesCodec()).apply($$0, ColoredFallingBlock::new)
   );
   protected final ColorRGBA dustColor;

   @Override
   public MapCodec<? extends ColoredFallingBlock> codec() {
      return CODEC;
   }

   public ColoredFallingBlock(ColorRGBA $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.dustColor = $$0;
   }

   @Override
   public int getDustColor(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return this.dustColor.rgba();
   }
}
