package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WeightedPressurePlateBlock extends BasePressurePlateBlock {
   public static final MapCodec<WeightedPressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.intRange(1, 1024).fieldOf("max_weight").forGetter($$0x -> $$0x.maxWeight),
            BlockSetType.CODEC.fieldOf("block_set_type").forGetter($$0x -> $$0x.type),
            propertiesCodec()
         )
         .apply($$0, WeightedPressurePlateBlock::new)
   );
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   private final int maxWeight;

   @Override
   public MapCodec<WeightedPressurePlateBlock> codec() {
      return CODEC;
   }

   protected WeightedPressurePlateBlock(int $$0, BlockSetType $$1, BlockBehaviour.Properties $$2) {
      super($$2, $$1);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
      this.maxWeight = $$0;
   }

   @Override
   protected int getSignalStrength(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      int $$2 = Math.min(getEntityCount($$0, TOUCH_AABB.move($$1), Entity.class), this.maxWeight);
      if ($$2 > 0) {
         float $$3 = (float)Math.min(this.maxWeight, $$2) / this.maxWeight;
         return Mth.ceil($$3 * 15.0F);
      } else {
         return 0;
      }
   }

   @Override
   protected int getSignalForState(BlockState $$0) {
      return $$0.getValue(POWER);
   }

   @Override
   protected BlockState setSignalForState(BlockState $$0, int $$1) {
      return $$0.setValue(POWER, $$1);
   }

   @Override
   protected int getPressedTime() {
      return 10;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(POWER);
   }
}
