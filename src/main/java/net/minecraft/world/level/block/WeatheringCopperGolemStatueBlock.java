package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WeatheringCopperGolemStatueBlock extends CopperGolemStatueBlock implements WeatheringCopper {
   public static final MapCodec<WeatheringCopperGolemStatueBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(ChangeOverTimeBlock::getAge), propertiesCodec())
         .apply($$0, WeatheringCopperGolemStatueBlock::new)
   );

   @Override
   public MapCodec<WeatheringCopperGolemStatueBlock> codec() {
      return CODEC;
   }

   public WeatheringCopperGolemStatueBlock(WeatheringCopper.WeatherState $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return WeatheringCopper.getNext($$0.getBlock()).isPresent();
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      this.changeOverTime($$0, $$1, $$2, $$3);
   }

   public WeatheringCopper.WeatherState getAge() {
      return this.getWeatheringState();
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$2.getBlockEntity($$3) instanceof CopperGolemStatueBlockEntity $$7) {
         if (!$$0.is(ItemTags.AXES)) {
            if ($$0.is(Items.HONEYCOMB)) {
               return InteractionResult.PASS;
            }

            this.updatePose($$2, $$1, $$3, $$4);
            return InteractionResult.SUCCESS;
         }

         if (this.getAge().equals(WeatheringCopper.WeatherState.UNAFFECTED)) {
            CopperGolem $$8 = $$7.removeStatue($$1);
            $$0.hurtAndBreak(1, $$4, $$5.asEquipmentSlot());
            if ($$8 != null) {
               $$2.addFreshEntity($$8);
               $$2.removeBlock($$3, false);
               return InteractionResult.SUCCESS;
            }
         }
      }

      return InteractionResult.PASS;
   }
}
