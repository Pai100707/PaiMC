package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
   private BehaviorUtils() {
   }

   public static void lockGazeAndWalkToEachOther(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1, float $$2, int $$3) {
      lookAtEachOther($$0, $$1);
      setWalkAndLookTargetMemoriesToEachOther($$0, $$1, $$2, $$3);
   }

   public static boolean entityIsVisible(Brain<?> $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Optional<NearestVisibleLivingEntities> $$2 = $$0.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
      return $$2.isPresent() && $$2.get().contains($$1);
   }

   public static boolean targetIsValid(
      Brain<?> $$0, MemoryModuleType<? extends net.minecraft.world.entity.LivingEntity> $$1, net.minecraft.world.entity.EntityType<?> $$2
   ) {
      return targetIsValid($$0, $$1, $$1x -> $$1x.getType() == $$2);
   }

   private static boolean targetIsValid(
      Brain<?> $$0, MemoryModuleType<? extends net.minecraft.world.entity.LivingEntity> $$1, Predicate<net.minecraft.world.entity.LivingEntity> $$2
   ) {
      return $$0.getMemory($$1).filter($$2).filter(net.minecraft.world.entity.LivingEntity::isAlive).filter($$1x -> entityIsVisible($$0, $$1x)).isPresent();
   }

   private static void lookAtEachOther(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      lookAtEntity($$0, $$1);
      lookAtEntity($$1, $$0);
   }

   public static void lookAtEntity(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$1, true));
   }

   private static void setWalkAndLookTargetMemoriesToEachOther(
      net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1, float $$2, int $$3
   ) {
      setWalkAndLookTargetMemories($$0, $$1, $$2, $$3);
      setWalkAndLookTargetMemories($$1, $$0, $$2, $$3);
   }

   public static void setWalkAndLookTargetMemories(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.Entity $$1, float $$2, int $$3) {
      setWalkAndLookTargetMemories($$0, new EntityTracker($$1, true), $$2, $$3);
   }

   public static void setWalkAndLookTargetMemories(net.minecraft.world.entity.LivingEntity $$0, BlockPos $$1, float $$2, int $$3) {
      setWalkAndLookTargetMemories($$0, new BlockPosTracker($$1), $$2, $$3);
   }

   public static void setWalkAndLookTargetMemories(net.minecraft.world.entity.LivingEntity $$0, PositionTracker $$1, float $$2, int $$3) {
      WalkTarget $$4 = new WalkTarget($$1, $$2, $$3);
      $$0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, $$1);
      $$0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, $$4);
   }

   public static void throwItem(net.minecraft.world.entity.LivingEntity $$0, ItemStack $$1, Vec3 $$2) {
      Vec3 $$3 = new Vec3(0.3F, 0.3F, 0.3F);
      throwItem($$0, $$1, $$2, $$3, 0.3F);
   }

   public static void throwItem(net.minecraft.world.entity.LivingEntity $$0, ItemStack $$1, Vec3 $$2, Vec3 $$3, float $$4) {
      double $$5 = $$0.getEyeY() - $$4;
      ItemEntity $$6 = new ItemEntity($$0.level(), $$0.getX(), $$5, $$0.getZ(), $$1);
      $$6.setThrower($$0);
      Vec3 $$7 = $$2.subtract($$0.position());
      $$7 = $$7.normalize().multiply($$3.x, $$3.y, $$3.z);
      $$6.setDeltaMovement($$7);
      $$6.setDefaultPickUpDelay();
      $$0.level().addFreshEntity($$6);
   }

   public static SectionPos findSectionClosestToVillage(ServerLevel $$0, SectionPos $$1, int $$2) {
      int $$3 = $$0.sectionsToVillage($$1);
      return SectionPos.cube($$1, $$2).filter($$2x -> $$0.sectionsToVillage($$2x) < $$3).min(Comparator.comparingInt($$0::sectionsToVillage)).orElse($$1);
   }

   public static boolean isWithinAttackRange(net.minecraft.world.entity.Mob $$0, net.minecraft.world.entity.LivingEntity $$1, int $$2) {
      if ($$0.getMainHandItem().getItem() instanceof ProjectileWeaponItem $$3 && $$0.canUseNonMeleeWeapon($$0.getMainHandItem())) {
         int $$4 = $$3.getDefaultProjectileRange() - $$2;
         return $$0.closerThan($$1, $$4);
      } else {
         return $$0.isWithinMeleeAttackRange($$1);
      }
   }

   public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(
      net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1, double $$2
   ) {
      Optional<net.minecraft.world.entity.LivingEntity> $$3 = $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
      if ($$3.isEmpty()) {
         return false;
      } else {
         double $$4 = $$0.distanceToSqr($$3.get().position());
         double $$5 = $$0.distanceToSqr($$1.position());
         return $$5 > $$4 + $$2 * $$2;
      }
   }

   public static boolean canSee(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Brain<?> $$2 = $$0.getBrain();
      return !$$2.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
         ? false
         : $$2.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains($$1);
   }

   public static net.minecraft.world.entity.LivingEntity getNearestTarget(
      net.minecraft.world.entity.LivingEntity $$0, Optional<net.minecraft.world.entity.LivingEntity> $$1, net.minecraft.world.entity.LivingEntity $$2
   ) {
      return $$1.isEmpty() ? $$2 : getTargetNearestMe($$0, $$1.get(), $$2);
   }

   public static net.minecraft.world.entity.LivingEntity getTargetNearestMe(
      net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1, net.minecraft.world.entity.LivingEntity $$2
   ) {
      Vec3 $$3 = $$1.position();
      Vec3 $$4 = $$2.position();
      return $$0.distanceToSqr($$3) < $$0.distanceToSqr($$4) ? $$1 : $$2;
   }

   public static Optional<net.minecraft.world.entity.LivingEntity> getLivingEntityFromUUIDMemory(
      net.minecraft.world.entity.LivingEntity $$0, MemoryModuleType<UUID> $$1
   ) {
      Optional<UUID> $$2 = $$0.getBrain().getMemory($$1);
      return $$2.<net.minecraft.world.entity.Entity>map($$1x -> $$0.level().getEntity($$1x))
         .map($$0x -> $$0x instanceof net.minecraft.world.entity.LivingEntity $$1x ? $$1x : null);
   }

   
   public static Vec3 getRandomSwimmablePos(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2) {
      Vec3 $$3 = DefaultRandomPos.getPos($$0, $$1, $$2);
      int $$4 = 0;

      while ($$3 != null && !$$0.level().getBlockState(BlockPos.containing($$3)).isPathfindable(PathComputationType.WATER) && $$4++ < 10) {
         $$3 = DefaultRandomPos.getPos($$0, $$1, $$2);
      }

      return $$3;
   }

   public static boolean isBreeding(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
   }
}
