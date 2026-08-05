/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BehaviorUtils {
    private BehaviorUtils() {
    }

    public static void lockGazeAndWalkToEachOther(LivingEntity $$0, LivingEntity $$1, float $$2, int $$3) {
        BehaviorUtils.lookAtEachOther($$0, $$1);
        BehaviorUtils.setWalkAndLookTargetMemoriesToEachOther($$0, $$1, $$2, $$3);
    }

    public static boolean entityIsVisible(Brain<?> $$0, LivingEntity $$1) {
        Optional<NearestVisibleLivingEntities> $$2 = $$0.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        return $$2.isPresent() && $$2.get().contains($$1);
    }

    public static boolean targetIsValid(Brain<?> $$0, MemoryModuleType<? extends LivingEntity> $$12, EntityType<?> $$2) {
        return BehaviorUtils.targetIsValid($$0, $$12, (LivingEntity $$1) -> $$1.getType() == $$2);
    }

    private static boolean targetIsValid(Brain<?> $$0, MemoryModuleType<? extends LivingEntity> $$12, Predicate<LivingEntity> $$2) {
        return $$0.getMemory($$12).filter($$2).filter(LivingEntity::isAlive).filter($$1 -> BehaviorUtils.entityIsVisible($$0, $$1)).isPresent();
    }

    private static void lookAtEachOther(LivingEntity $$0, LivingEntity $$1) {
        BehaviorUtils.lookAtEntity($$0, $$1);
        BehaviorUtils.lookAtEntity($$1, $$0);
    }

    public static void lookAtEntity(LivingEntity $$0, LivingEntity $$1) {
        $$0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$1, true));
    }

    private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity $$0, LivingEntity $$1, float $$2, int $$3) {
        BehaviorUtils.setWalkAndLookTargetMemories($$0, $$1, $$2, $$3);
        BehaviorUtils.setWalkAndLookTargetMemories($$1, $$0, $$2, $$3);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity $$0, Entity $$1, float $$2, int $$3) {
        BehaviorUtils.setWalkAndLookTargetMemories($$0, new EntityTracker($$1, true), $$2, $$3);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity $$0, BlockPos $$1, float $$2, int $$3) {
        BehaviorUtils.setWalkAndLookTargetMemories($$0, new BlockPosTracker($$1), $$2, $$3);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity $$0, PositionTracker $$1, float $$2, int $$3) {
        WalkTarget $$4 = new WalkTarget($$1, $$2, $$3);
        $$0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, $$1);
        $$0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, $$4);
    }

    public static void throwItem(LivingEntity $$0, ItemStack $$1, Vec3 $$2) {
        Vec3 $$3 = new Vec3(0.3f, 0.3f, 0.3f);
        BehaviorUtils.throwItem($$0, $$1, $$2, $$3, 0.3f);
    }

    public static void throwItem(LivingEntity $$0, ItemStack $$1, Vec3 $$2, Vec3 $$3, float $$4) {
        double $$5 = $$0.getEyeY() - (double)$$4;
        ItemEntity $$6 = new ItemEntity($$0.level(), $$0.getX(), $$5, $$0.getZ(), $$1);
        $$6.setThrower($$0);
        Vec3 $$7 = $$2.subtract($$0.position());
        $$7 = $$7.normalize().multiply($$3.x, $$3.y, $$3.z);
        $$6.setDeltaMovement($$7);
        $$6.setDefaultPickUpDelay();
        $$0.level().addFreshEntity($$6);
    }

    public static SectionPos findSectionClosestToVillage(ServerLevel $$0, SectionPos $$1, int $$22) {
        int $$3 = $$0.sectionsToVillage($$1);
        return SectionPos.cube($$1, $$22).filter($$2 -> $$0.sectionsToVillage((SectionPos)$$2) < $$3).min(Comparator.comparingInt($$0::sectionsToVillage)).orElse($$1);
    }

    public static boolean isWithinAttackRange(Mob $$0, LivingEntity $$1, int $$2) {
        Item item = $$0.getMainHandItem().getItem();
        if (item instanceof ProjectileWeaponItem) {
            ProjectileWeaponItem $$3 = (ProjectileWeaponItem)item;
            if ($$0.canUseNonMeleeWeapon($$0.getMainHandItem())) {
                int $$4 = $$3.getDefaultProjectileRange() - $$2;
                return $$0.closerThan($$1, $$4);
            }
        }
        return $$0.isWithinMeleeAttackRange($$1);
    }

    public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity $$0, LivingEntity $$1, double $$2) {
        Optional<LivingEntity> $$3 = $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if ($$3.isEmpty()) {
            return false;
        }
        double $$4 = $$0.distanceToSqr($$3.get().position());
        double $$5 = $$0.distanceToSqr($$1.position());
        return $$5 > $$4 + $$2 * $$2;
    }

    public static boolean canSee(LivingEntity $$0, LivingEntity $$1) {
        Brain<NearestVisibleLivingEntities> $$2 = $$0.getBrain();
        if (!$$2.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) {
            return false;
        }
        return $$2.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains($$1);
    }

    public static LivingEntity getNearestTarget(LivingEntity $$0, Optional<LivingEntity> $$1, LivingEntity $$2) {
        if ($$1.isEmpty()) {
            return $$2;
        }
        return BehaviorUtils.getTargetNearestMe($$0, $$1.get(), $$2);
    }

    public static LivingEntity getTargetNearestMe(LivingEntity $$0, LivingEntity $$1, LivingEntity $$2) {
        Vec3 $$3 = $$1.position();
        Vec3 $$4 = $$2.position();
        return $$0.distanceToSqr($$3) < $$0.distanceToSqr($$4) ? $$1 : $$2;
    }

    public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity $$02, MemoryModuleType<UUID> $$12) {
        Optional<UUID> $$2 = $$02.getBrain().getMemory($$12);
        return $$2.map($$1 -> $$02.level().getEntity((UUID)$$1)).map($$0 -> {
            LivingEntity $$1;
            return $$0 instanceof LivingEntity ? ($$1 = (LivingEntity)$$0) : null;
        });
    }

    public static @Nullable Vec3 getRandomSwimmablePos(PathfinderMob $$0, int $$1, int $$2) {
        Vec3 $$3 = DefaultRandomPos.getPos($$0, $$1, $$2);
        int $$4 = 0;
        while ($$3 != null && !$$0.level().getBlockState(BlockPos.containing($$3)).isPathfindable(PathComputationType.WATER) && $$4++ < 10) {
            $$3 = DefaultRandomPos.getPos($$0, $$1, $$2);
        }
        return $$3;
    }

    public static boolean isBreeding(LivingEntity $$0) {
        return $$0.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }
}

