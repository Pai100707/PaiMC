package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class WeatheringCopperDoorBlock extends DoorBlock implements WeatheringCopper {
   public static final MapCodec<WeatheringCopperDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            BlockSetType.CODEC.fieldOf("block_set_type").forGetter(DoorBlock::type),
            WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperDoorBlock::getAge),
            propertiesCodec()
         )
         .apply($$0, WeatheringCopperDoorBlock::new)
   );
   private final WeatheringCopper.WeatherState weatherState;

   @Override
   public MapCodec<WeatheringCopperDoorBlock> codec() {
      return CODEC;
   }

   protected WeatheringCopperDoorBlock(BlockSetType $$0, WeatheringCopper.WeatherState $$1, BlockBehaviour.Properties $$2) {
      super($$0, $$2);
      this.weatherState = $$1;
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
         this.changeOverTime($$0, $$1, $$2, $$3);
      }
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return WeatheringCopper.getNext($$0.getBlock()).isPresent();
   }

   public WeatheringCopper.WeatherState getAge() {
      return this.weatherState;
   }
}
