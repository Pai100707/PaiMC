package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GoAndGiveItemsToTarget<E extends net.minecraft.world.entity.LivingEntity & InventoryCarrier> extends Behavior<E> {
   private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
   private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 60;
   private final Function<net.minecraft.world.entity.LivingEntity, Optional<PositionTracker>> targetPositionGetter;
   private final float speedModifier;

   public GoAndGiveItemsToTarget(Function<net.minecraft.world.entity.LivingEntity, Optional<PositionTracker>> $$0, float $$1, int $$2) {
      super(
         Map.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
            MemoryStatus.REGISTERED
         ),
         $$2
      );
      this.targetPositionGetter = $$0;
      this.speedModifier = $$1;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel $$0, E $$1) {
      return this.canThrowItemToTarget($$1);
   }

   @Override
   protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
      return this.canThrowItemToTarget($$1);
   }

   @Override
   protected void start(ServerLevel $$0, E $$1, long $$2) {
      this.targetPositionGetter.apply($$1).ifPresent($$1x -> BehaviorUtils.setWalkAndLookTargetMemories($$1, $$1x, this.speedModifier, 3));
   }

   @Override
   protected void tick(ServerLevel $$0, E $$1, long $$2) {
      Optional<PositionTracker> $$3 = this.targetPositionGetter.apply($$1);
      if (!$$3.isEmpty()) {
         PositionTracker $$4 = $$3.get();
         double $$5 = $$4.currentPosition().distanceTo($$1.getEyePosition());
         if ($$5 < 3.0) {
            ItemStack $$6 = $$1.getInventory().removeItem(0, 1);
            if (!$$6.isEmpty()) {
               throwItem($$1, $$6, getThrowPosition($$4));
               if ($$1 instanceof Allay $$7) {
                  AllayAi.getLikedPlayer($$7).ifPresent($$2x -> this.triggerDropItemOnBlock($$4, $$6, $$2x));
               }

               $$1.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 60);
            }
         }
      }
   }

   private void triggerDropItemOnBlock(PositionTracker $$0, ItemStack $$1, ServerPlayer $$2) {
      BlockPos $$3 = $$0.currentBlockPosition().below();
      CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.trigger($$2, $$3, $$1);
   }

   private boolean canThrowItemToTarget(E $$0) {
      if ($$0.getInventory().isEmpty()) {
         return false;
      } else {
         Optional<PositionTracker> $$1 = this.targetPositionGetter.apply($$0);
         return $$1.isPresent();
      }
   }

   private static Vec3 getThrowPosition(PositionTracker $$0) {
      return $$0.currentPosition().add(0.0, 1.0, 0.0);
   }

   public static void throwItem(net.minecraft.world.entity.LivingEntity $$0, ItemStack $$1, Vec3 $$2) {
      Vec3 $$3 = new Vec3(0.2F, 0.3F, 0.2F);
      BehaviorUtils.throwItem($$0, $$1, $$2, $$3, 0.2F);
      Level $$4 = $$0.level();
      if ($$4.getGameTime() % 7L == 0L && $$4.random.nextDouble() < 0.9) {
         float $$5 = (Float)Util.getRandom(Allay.THROW_SOUND_PITCHES, $$4.getRandom());
         $$4.playSound(null, $$0, SoundEvents.ALLAY_THROW, SoundSource.NEUTRAL, 1.0F, $$5);
      }
   }
}
