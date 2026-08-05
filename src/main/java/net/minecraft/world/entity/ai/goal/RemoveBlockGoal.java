package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public class RemoveBlockGoal extends MoveToBlockGoal {
   private final Block blockToRemove;
   private final net.minecraft.world.entity.Mob removerMob;
   private int ticksSinceReachedGoal;
   private static final int WAIT_AFTER_BLOCK_FOUND = 20;

   public RemoveBlockGoal(Block $$0, net.minecraft.world.entity.PathfinderMob $$1, double $$2, int $$3) {
      super($$1, $$2, 24, $$3);
      this.blockToRemove = $$0;
      this.removerMob = $$1;
   }

   @Override
   public boolean canUse() {
      if (!(Boolean)getServerLevel(this.removerMob).getGameRules().get(GameRules.MOB_GRIEFING)) {
         return false;
      } else if (this.nextStartTick > 0) {
         this.nextStartTick--;
         return false;
      } else if (this.findNearestBlock()) {
         this.nextStartTick = reducedTickDelay(20);
         return true;
      } else {
         this.nextStartTick = this.nextStartTick(this.mob);
         return false;
      }
   }

   @Override
   public void stop() {
      super.stop();
      this.removerMob.fallDistance = 1.0;
   }

   @Override
   public void start() {
      super.start();
      this.ticksSinceReachedGoal = 0;
   }

   public void playDestroyProgressSound(LevelAccessor $$0, BlockPos $$1) {
   }

   public void playBreakSound(Level $$0, BlockPos $$1) {
   }

   @Override
   public void tick() {
      super.tick();
      Level $$0 = this.removerMob.level();
      BlockPos $$1 = this.removerMob.blockPosition();
      BlockPos $$2 = this.getPosWithBlock($$1, $$0);
      RandomSource $$3 = this.removerMob.getRandom();
      if (this.isReachedTarget() && $$2 != null) {
         if (this.ticksSinceReachedGoal > 0) {
            Vec3 $$4 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement($$4.x, 0.3, $$4.z);
            if (!$$0.isClientSide()) {
               double $$5 = 0.08;
               ((ServerLevel)$$0)
                  .sendParticles(
                     new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG)),
                     $$2.getX() + 0.5,
                     $$2.getY() + 0.7,
                     $$2.getZ() + 0.5,
                     3,
                     ($$3.nextFloat() - 0.5) * 0.08,
                     ($$3.nextFloat() - 0.5) * 0.08,
                     ($$3.nextFloat() - 0.5) * 0.08,
                     0.15F
                  );
            }
         }

         if (this.ticksSinceReachedGoal % 2 == 0) {
            Vec3 $$6 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement($$6.x, -0.3, $$6.z);
            if (this.ticksSinceReachedGoal % 6 == 0) {
               this.playDestroyProgressSound($$0, this.blockPos);
            }
         }

         if (this.ticksSinceReachedGoal > 60) {
            $$0.removeBlock($$2, false);
            if (!$$0.isClientSide()) {
               for (int $$7 = 0; $$7 < 20; $$7++) {
                  double $$8 = $$3.nextGaussian() * 0.02;
                  double $$9 = $$3.nextGaussian() * 0.02;
                  double $$10 = $$3.nextGaussian() * 0.02;
                  ((ServerLevel)$$0).sendParticles(ParticleTypes.POOF, $$2.getX() + 0.5, $$2.getY(), $$2.getZ() + 0.5, 1, $$8, $$9, $$10, 0.15F);
               }

               this.playBreakSound($$0, $$2);
            }
         }

         this.ticksSinceReachedGoal++;
      }
   }

   
   private BlockPos getPosWithBlock(BlockPos $$0, BlockGetter $$1) {
      if ($$1.getBlockState($$0).is(this.blockToRemove)) {
         return $$0;
      } else {
         BlockPos[] $$2 = new BlockPos[]{$$0.below(), $$0.west(), $$0.east(), $$0.north(), $$0.south(), $$0.below().below()};

         for (BlockPos $$3 : $$2) {
            if ($$1.getBlockState($$3).is(this.blockToRemove)) {
               return $$3;
            }
         }

         return null;
      }
   }

   @Override
   protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
      ChunkAccess $$2 = $$0.getChunk(SectionPos.blockToSectionCoord($$1.getX()), SectionPos.blockToSectionCoord($$1.getZ()), ChunkStatus.FULL, false);
      return $$2 == null
         ? false
         : $$2.getBlockState($$1).is(this.blockToRemove) && $$2.getBlockState($$1.above()).isAir() && $$2.getBlockState($$1.above(2)).isAir();
   }
}
