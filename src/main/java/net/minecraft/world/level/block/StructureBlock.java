package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class StructureBlock extends BaseEntityBlock implements GameMasterBlock {
   public static final MapCodec<StructureBlock> CODEC = simpleCodec(StructureBlock::new);
   public static final EnumProperty<StructureMode> MODE = BlockStateProperties.STRUCTUREBLOCK_MODE;

   @Override
   public MapCodec<StructureBlock> codec() {
      return CODEC;
   }

   protected StructureBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(MODE, StructureMode.LOAD));
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new StructureBlockEntity($$0, $$1);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      BlockEntity $$5 = $$1.getBlockEntity($$2);
      if ($$5 instanceof StructureBlockEntity) {
         return (InteractionResult)(((StructureBlockEntity)$$5).usedBy($$3) ? InteractionResult.SUCCESS : InteractionResult.PASS);
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, LivingEntity $$3, ItemStack $$4) {
      if (!$$0.isClientSide()) {
         if ($$3 != null) {
            BlockEntity $$5 = $$0.getBlockEntity($$1);
            if ($$5 instanceof StructureBlockEntity) {
               ((StructureBlockEntity)$$5).createdBy($$3);
            }
         }
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(MODE);
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if ($$1 instanceof ServerLevel) {
         if ($$1.getBlockEntity($$2) instanceof StructureBlockEntity $$7) {
            boolean $$8 = $$1.hasNeighborSignal($$2);
            boolean $$9 = $$7.isPowered();
            if ($$8 && !$$9) {
               $$7.setPowered(true);
               this.trigger((ServerLevel)$$1, $$7);
            } else if (!$$8 && $$9) {
               $$7.setPowered(false);
            }
         }
      }
   }

   private void trigger(ServerLevel $$0, StructureBlockEntity $$1) {
      switch ($$1.getMode()) {
         case SAVE:
            $$1.saveStructure(false);
            break;
         case LOAD:
            $$1.placeStructure($$0);
            break;
         case CORNER:
            $$1.unloadStructure();
         case DATA:
      }
   }
}
