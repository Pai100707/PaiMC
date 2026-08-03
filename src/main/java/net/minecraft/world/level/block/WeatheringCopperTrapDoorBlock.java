package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class WeatheringCopperTrapDoorBlock extends TrapDoorBlock implements WeatheringCopper {
   public static final MapCodec<WeatheringCopperTrapDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            BlockSetType.CODEC.fieldOf("block_set_type").forGetter(TrapDoorBlock::getType),
            WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperTrapDoorBlock::getAge),
            propertiesCodec()
         )
         .apply($$0, WeatheringCopperTrapDoorBlock::new)
   );
   private final WeatheringCopper.WeatherState weatherState;

   @Override
   public MapCodec<WeatheringCopperTrapDoorBlock> codec() {
      return CODEC;
   }

   protected WeatheringCopperTrapDoorBlock(BlockSetType $$0, WeatheringCopper.WeatherState $$1, BlockBehaviour.Properties $$2) {
      super($$0, $$2);
      this.weatherState = $$1;
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
