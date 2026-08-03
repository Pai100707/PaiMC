package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PressurePlateBlock extends BasePressurePlateBlock {
   public static final MapCodec<PressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter($$0x -> $$0x.type), propertiesCodec()).apply($$0, PressurePlateBlock::new)
   );
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   @Override
   public MapCodec<PressurePlateBlock> codec() {
      return CODEC;
   }

   protected PressurePlateBlock(BlockSetType $$0, BlockBehaviour.Properties $$1) {
      super($$1, $$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
   }

   @Override
   protected int getSignalForState(BlockState $$0) {
      return $$0.getValue(POWERED) ? 15 : 0;
   }

   @Override
   protected BlockState setSignalForState(BlockState $$0, int $$1) {
      return $$0.setValue(POWERED, $$1 > 0);
   }

   @Override
   protected int getSignalStrength(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      Class<? extends Entity> $$2 = switch (this.type.pressurePlateSensitivity()) {
         case EVERYTHING -> Entity.class;
         case MOBS -> LivingEntity.class;
      };
      return getEntityCount($$0, TOUCH_AABB.move($$1), $$2) > 0 ? 15 : 0;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(POWERED);
   }
}
