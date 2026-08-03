package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ValidateNearbyPoi {
   private static final int MAX_DISTANCE = 16;

   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(Predicate<Holder<PoiType>> $$0, MemoryModuleType<GlobalPos> $$1) {
      return BehaviorBuilder.create($$2 -> $$2.group($$2.present($$1)).apply($$2, $$2x -> ($$3, $$4, $$5) -> {
         GlobalPos $$6 = $$2.get($$2x);
         BlockPos $$7 = $$6.pos();
         if ($$3.dimension() == $$6.dimension() && $$7.closerToCenterThan($$4.position(), 16.0)) {
            ServerLevel $$8 = $$3.getServer().getLevel($$6.dimension());
            if ($$8 == null || !$$8.getPoiManager().exists($$7, $$0)) {
               $$2x.erase();
            } else if (bedIsOccupied($$8, $$7, $$4)) {
               $$2x.erase();
               if (!bedIsOccupiedByVillager($$8, $$7)) {
                  $$3.getPoiManager().release($$7);
                  $$3.debugSynchronizers().updatePoi($$7);
               }
            }

            return true;
         } else {
            return false;
         }
      }));
   }

   private static boolean bedIsOccupied(ServerLevel $$0, BlockPos $$1, net.minecraft.world.entity.LivingEntity $$2) {
      BlockState $$3 = $$0.getBlockState($$1);
      return $$3.is(BlockTags.BEDS) && (Boolean)$$3.getValue(BedBlock.OCCUPIED) && !$$2.isSleeping();
   }

   private static boolean bedIsOccupiedByVillager(ServerLevel $$0, BlockPos $$1) {
      List<Villager> $$2 = $$0.getEntitiesOfClass(Villager.class, new AABB($$1), net.minecraft.world.entity.LivingEntity::isSleeping);
      return !$$2.isEmpty();
   }
}
