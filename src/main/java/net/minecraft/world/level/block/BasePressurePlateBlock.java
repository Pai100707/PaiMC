package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class BasePressurePlateBlock extends Block {
   private static final VoxelShape SHAPE_PRESSED = Block.column(14.0, 0.0, 0.5);
   private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 1.0);
   protected static final AABB TOUCH_AABB = (AABB)Block.column(14.0, 0.0, 4.0).toAabbs().getFirst();
   protected final BlockSetType type;

   protected BasePressurePlateBlock(BlockBehaviour.Properties $$0, BlockSetType $$1) {
      super($$0.sound($$1.soundType()));
      this.type = $$1;
   }

   @Override
   protected abstract MapCodec<? extends BasePressurePlateBlock> codec();

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.getSignalForState($$0) > 0 ? SHAPE_PRESSED : SHAPE;
   }

   protected int getPressedTime() {
      return 20;
   }

   @Override
   public boolean isPossibleToRespawnInThis(BlockState $$0) {
      return true;
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      return $$4 == Direction.DOWN && !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      return canSupportRigidBlock($$1, $$3) || canSupportCenter($$1, $$3, Direction.UP);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      int $$4 = this.getSignalForState($$0);
      if ($$4 > 0) {
         this.checkPressed(null, $$1, $$2, $$0, $$4);
      }
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         int $$6 = this.getSignalForState($$0);
         if ($$6 == 0) {
            this.checkPressed($$3, $$1, $$2, $$0, $$6);
         }
      }
   }

   private void checkPressed(@Nullable Entity $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, int $$4) {
      int $$5 = this.getSignalStrength($$1, $$2);
      boolean $$6 = $$4 > 0;
      boolean $$7 = $$5 > 0;
      if ($$4 != $$5) {
         BlockState $$8 = this.setSignalForState($$3, $$5);
         $$1.setBlock($$2, $$8, 2);
         this.updateNeighbours($$1, $$2);
         $$1.setBlocksDirty($$2, $$3, $$8);
      }

      if (!$$7 && $$6) {
         $$1.playSound(null, $$2, this.type.pressurePlateClickOff(), SoundSource.BLOCKS);
         $$1.gameEvent($$0, GameEvent.BLOCK_DEACTIVATE, $$2);
      } else if ($$7 && !$$6) {
         $$1.playSound(null, $$2, this.type.pressurePlateClickOn(), SoundSource.BLOCKS);
         $$1.gameEvent($$0, GameEvent.BLOCK_ACTIVATE, $$2);
      }

      if ($$7) {
         $$1.scheduleTick(new BlockPos($$2), this, this.getPressedTime());
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if (!$$3 && this.getSignalForState($$0) > 0) {
         this.updateNeighbours($$1, $$2);
      }
   }

   protected void updateNeighbours(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      $$0.updateNeighborsAt($$1, this);
      $$0.updateNeighborsAt($$1.below(), this);
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return this.getSignalForState($$0);
   }

   @Override
   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$3 == Direction.UP ? this.getSignalForState($$0) : 0;
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   protected static int getEntityCount(net.minecraft.world.level.Level $$0, AABB $$1, Class<? extends Entity> $$2) {
      return $$0.getEntitiesOfClass($$2, $$1, EntitySelector.NO_SPECTATORS.and($$0x -> !$$0x.isIgnoringBlockTriggers())).size();
   }

   protected abstract int getSignalStrength(net.minecraft.world.level.Level var1, BlockPos var2);

   protected abstract int getSignalForState(BlockState var1);

   protected abstract BlockState setSignalForState(BlockState var1, int var2);
}
