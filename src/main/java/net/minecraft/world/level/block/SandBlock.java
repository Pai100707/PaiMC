package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.sounds.AmbientDesertBlockSoundsPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SandBlock extends ColoredFallingBlock {
   public static final MapCodec<SandBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ColorRGBA.CODEC.fieldOf("falling_dust_color").forGetter($$0x -> $$0x.dustColor), propertiesCodec()).apply($$0, SandBlock::new)
   );

   @Override
   public MapCodec<SandBlock> codec() {
      return CODEC;
   }

   public SandBlock(ColorRGBA $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      super.animateTick($$0, $$1, $$2, $$3);
      AmbientDesertBlockSoundsPlayer.playAmbientSandSounds($$1, $$2, $$3);
   }
}
