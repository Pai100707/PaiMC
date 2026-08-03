package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class PlayTagWithOtherKids {
   private static final int MAX_FLEE_XZ_DIST = 20;
   private static final int MAX_FLEE_Y_DIST = 8;
   private static final float FLEE_SPEED_MODIFIER = 0.6F;
   private static final float CHASE_SPEED_MODIFIER = 0.6F;
   private static final int MAX_CHASERS_PER_TARGET = 5;
   private static final int AVERAGE_WAIT_TIME_BETWEEN_RUNS = 10;

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group(
               $$0.present(MemoryModuleType.VISIBLE_VILLAGER_BABIES),
               $$0.absent(MemoryModuleType.WALK_TARGET),
               $$0.registered(MemoryModuleType.LOOK_TARGET),
               $$0.registered(MemoryModuleType.INTERACTION_TARGET)
            )
            .apply($$0, ($$1, $$2, $$3, $$4) -> ($$5, $$6, $$7) -> {
               if ($$5.getRandom().nextInt(10) != 0) {
                  return false;
               } else {
                  List<net.minecraft.world.entity.LivingEntity> $$8 = $$0.get($$1);
                  Optional<net.minecraft.world.entity.LivingEntity> $$9 = $$8.stream().filter($$1xx -> isFriendChasingMe($$6, $$1xx)).findAny();
                  if (!$$9.isPresent()) {
                     Optional<net.minecraft.world.entity.LivingEntity> $$12 = findSomeoneBeingChased($$8);
                     if ($$12.isPresent()) {
                        chaseKid($$4, $$3, $$2, $$12.get());
                        return true;
                     } else {
                        $$8.stream().findAny().ifPresent($$3xx -> chaseKid($$4, $$3, $$2, $$3xx));
                        return true;
                     }
                  } else {
                     for (int $$10 = 0; $$10 < 10; $$10++) {
                        Vec3 $$11 = LandRandomPos.getPos($$6, 20, 8);
                        if ($$11 != null && $$5.isVillage(BlockPos.containing($$11))) {
                           $$2.set(new WalkTarget($$11, 0.6F, 0));
                           break;
                        }
                     }

                     return true;
                  }
               }
            })
      );
   }

   private static void chaseKid(
      MemoryAccessor<?, net.minecraft.world.entity.LivingEntity> $$0,
      MemoryAccessor<?, PositionTracker> $$1,
      MemoryAccessor<?, WalkTarget> $$2,
      net.minecraft.world.entity.LivingEntity $$3
   ) {
      $$0.set($$3);
      $$1.set(new EntityTracker($$3, true));
      $$2.set(new WalkTarget(new EntityTracker($$3, false), 0.6F, 1));
   }

   private static Optional<net.minecraft.world.entity.LivingEntity> findSomeoneBeingChased(List<net.minecraft.world.entity.LivingEntity> $$0) {
      Map<net.minecraft.world.entity.LivingEntity, Integer> $$1 = checkHowManyChasersEachFriendHas($$0);
      return $$1.entrySet()
         .stream()
         .sorted(Comparator.comparingInt(Entry::getValue))
         .filter($$0x -> (Integer)$$0x.getValue() > 0 && (Integer)$$0x.getValue() <= 5)
         .map(Entry::getKey)
         .findFirst();
   }

   private static Map<net.minecraft.world.entity.LivingEntity, Integer> checkHowManyChasersEachFriendHas(List<net.minecraft.world.entity.LivingEntity> $$0) {
      Map<net.minecraft.world.entity.LivingEntity, Integer> $$1 = Maps.newHashMap();
      $$0.stream()
         .filter(PlayTagWithOtherKids::isChasingSomeone)
         .forEach($$1x -> $$1.compute(whoAreYouChasing($$1x), ($$0xx, $$1xx) -> $$1xx == null ? 1 : $$1xx + 1));
      return $$1;
   }

   private static net.minecraft.world.entity.LivingEntity whoAreYouChasing(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
   }

   private static boolean isChasingSomeone(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
   }

   private static boolean isFriendChasingMe(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return $$1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).filter($$1x -> $$1x == $$0).isPresent();
   }
}
