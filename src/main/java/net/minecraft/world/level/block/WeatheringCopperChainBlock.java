package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperChainBlock extends ChainBlock implements WeatheringCopper {
   public static final MapCodec<WeatheringCopperChainBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperChainBlock::getAge), propertiesCodec())
         .apply($$0, WeatheringCopperChainBlock::new)
   );
   private final WeatheringCopper.WeatherState weatherState;

   @Override
   public MapCodec<WeatheringCopperChainBlock> codec() {
      return CODEC;
   }

   protected WeatheringCopperChainBlock(WeatheringCopper.WeatherState $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.weatherState = $$0;
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      this.changeOverTime($$0, $$1, $$2, $$3);
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return WeatheringCopper.getNext($$0.getBlock()).isPresent();
   }

   public WeatheringCopper.WeatherState getAge() {
      return this.weatherState;
   }
}
