package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class WeatheringCopperChestBlock extends CopperChestBlock implements WeatheringCopper {
   public static final MapCodec<WeatheringCopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(CopperChestBlock::getState),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(ChestBlock::getOpenChestSound),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(ChestBlock::getCloseChestSound),
            propertiesCodec()
         )
         .apply($$0, WeatheringCopperChestBlock::new)
   );

   @Override
   public MapCodec<WeatheringCopperChestBlock> codec() {
      return CODEC;
   }

   public WeatheringCopperChestBlock(WeatheringCopper.WeatherState $$0, SoundEvent $$1, SoundEvent $$2, BlockBehaviour.Properties $$3) {
      super($$0, $$1, $$2, $$3);
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return WeatheringCopper.getNext($$0.getBlock()).isPresent();
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!$$0.getValue(ChestBlock.TYPE).equals(ChestType.RIGHT)
         && $$1.getBlockEntity($$2) instanceof ChestBlockEntity $$4
         && $$4.getEntitiesWithContainerOpen().isEmpty()) {
         this.changeOverTime($$0, $$1, $$2, $$3);
      }
   }

   public WeatheringCopper.WeatherState getAge() {
      return this.getState();
   }

   @Override
   public boolean isWaxed() {
      return false;
   }
}
