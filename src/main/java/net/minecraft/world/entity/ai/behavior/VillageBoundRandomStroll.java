package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class VillageBoundRandomStroll {
   private static final int MAX_XZ_DIST = 10;
   private static final int MAX_Y_DIST = 7;

   public static OneShot<net.minecraft.world.entity.PathfinderMob> create(float $$0) {
      return create($$0, 10, 7);
   }

   public static OneShot<net.minecraft.world.entity.PathfinderMob> create(float $$0, int $$1, int $$2) {
      return BehaviorBuilder.create($$3 -> $$3.group($$3.absent(MemoryModuleType.WALK_TARGET)).apply($$3, $$3x -> ($$4, $$5, $$6) -> {
         BlockPos $$7 = $$5.blockPosition();
         Vec3 $$8;
         if ($$4.isVillage($$7)) {
            $$8 = LandRandomPos.getPos($$5, $$1, $$2);
         } else {
            SectionPos $$9 = SectionPos.of($$7);
            SectionPos $$10 = BehaviorUtils.findSectionClosestToVillage($$4, $$9, 2);
            if ($$10 != $$9) {
               $$8 = DefaultRandomPos.getPosTowards($$5, $$1, $$2, Vec3.atBottomCenterOf($$10.center()), (float) (Math.PI / 2));
            } else {
               $$8 = LandRandomPos.getPos($$5, $$1, $$2);
            }
         }

         $$3x.setOrErase(Optional.ofNullable($$8).map($$1xxxx -> new WalkTarget($$1xxxx, $$0, 0)));
         return true;
      }));
   }
}
