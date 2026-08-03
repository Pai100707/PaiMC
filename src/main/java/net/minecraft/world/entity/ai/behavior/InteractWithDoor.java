package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.OptionalBox.Mu;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class InteractWithDoor {
   private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
   private static final double SKIP_CLOSING_DOOR_IF_FURTHER_AWAY_THAN = 3.0;
   private static final double MAX_DISTANCE_TO_HOLD_DOOR_OPEN_FOR_OTHER_MOBS = 2.0;

   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      MutableObject<Node> $$0 = new MutableObject();
      MutableInt $$1 = new MutableInt(0);
      return BehaviorBuilder.create(
         $$2 -> $$2.group(
               $$2.present(MemoryModuleType.PATH), $$2.registered(MemoryModuleType.DOORS_TO_CLOSE), $$2.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)
            )
            .apply($$2, ($$3, $$4, $$5) -> ($$6, $$7, $$8) -> {
               Path $$9 = $$2.get($$3);
               Optional<Set<GlobalPos>> $$10 = $$2.tryGet($$4);
               if (!$$9.notStarted() && !$$9.isDone()) {
                  if (Objects.equals($$0.get(), $$9.getNextNode())) {
                     $$1.setValue(20);
                  } else if ($$1.decrementAndGet() > 0) {
                     return false;
                  }

                  $$0.setValue($$9.getNextNode());
                  Node $$11 = $$9.getPreviousNode();
                  Node $$12 = $$9.getNextNode();
                  BlockPos $$13 = $$11.asBlockPos();
                  BlockState $$14 = $$6.getBlockState($$13);
                  if ($$14.is(BlockTags.MOB_INTERACTABLE_DOORS, $$0xxxx -> $$0xxxx.getBlock() instanceof DoorBlock)) {
                     DoorBlock $$15 = (DoorBlock)$$14.getBlock();
                     if (!$$15.isOpen($$14)) {
                        $$15.setOpen($$7, $$6, $$14, $$13, true);
                     }

                     $$10 = rememberDoorToClose($$4, $$10, $$6, $$13);
                  }

                  BlockPos $$16 = $$12.asBlockPos();
                  BlockState $$17 = $$6.getBlockState($$16);
                  if ($$17.is(BlockTags.MOB_INTERACTABLE_DOORS, $$0xxxx -> $$0xxxx.getBlock() instanceof DoorBlock)) {
                     DoorBlock $$18 = (DoorBlock)$$17.getBlock();
                     if (!$$18.isOpen($$17)) {
                        $$18.setOpen($$7, $$6, $$17, $$16, true);
                        $$10 = rememberDoorToClose($$4, $$10, $$6, $$16);
                     }
                  }

                  $$10.ifPresent($$6x -> closeDoorsThatIHaveOpenedOrPassedThrough($$6, $$7, $$11, $$12, $$6x, $$2.tryGet($$5)));
                  return true;
               } else {
                  return false;
               }
            })
      );
   }

   public static void closeDoorsThatIHaveOpenedOrPassedThrough(
      ServerLevel $$0,
      net.minecraft.world.entity.LivingEntity $$1,
      @Nullable Node $$2,
      @Nullable Node $$3,
      Set<GlobalPos> $$4,
      Optional<List<net.minecraft.world.entity.LivingEntity>> $$5
   ) {
      Iterator<GlobalPos> $$6 = $$4.iterator();

      while ($$6.hasNext()) {
         GlobalPos $$7 = $$6.next();
         BlockPos $$8 = $$7.pos();
         if (($$2 == null || !$$2.asBlockPos().equals($$8)) && ($$3 == null || !$$3.asBlockPos().equals($$8))) {
            if (isDoorTooFarAway($$0, $$1, $$7)) {
               $$6.remove();
            } else {
               BlockState $$9 = $$0.getBlockState($$8);
               if (!$$9.is(BlockTags.MOB_INTERACTABLE_DOORS, $$0x -> $$0x.getBlock() instanceof DoorBlock)) {
                  $$6.remove();
               } else {
                  DoorBlock $$10 = (DoorBlock)$$9.getBlock();
                  if (!$$10.isOpen($$9)) {
                     $$6.remove();
                  } else if (areOtherMobsComingThroughDoor($$1, $$8, $$5)) {
                     $$6.remove();
                  } else {
                     $$10.setOpen($$1, $$0, $$9, $$8, false);
                     $$6.remove();
                  }
               }
            }
         }
      }
   }

   private static boolean areOtherMobsComingThroughDoor(
      net.minecraft.world.entity.LivingEntity $$0, BlockPos $$1, Optional<List<net.minecraft.world.entity.LivingEntity>> $$2
   ) {
      return $$2.isEmpty()
         ? false
         : $$2.get()
            .stream()
            .filter($$1x -> $$1x.getType() == $$0.getType())
            .filter($$1x -> $$1.closerToCenterThan($$1x.position(), 2.0))
            .anyMatch($$1x -> isMobComingThroughDoor($$1x.getBrain(), $$1));
   }

   private static boolean isMobComingThroughDoor(Brain<?> $$0, BlockPos $$1) {
      if (!$$0.hasMemoryValue(MemoryModuleType.PATH)) {
         return false;
      } else {
         Path $$2 = $$0.getMemory(MemoryModuleType.PATH).get();
         if ($$2.isDone()) {
            return false;
         } else {
            Node $$3 = $$2.getPreviousNode();
            if ($$3 == null) {
               return false;
            } else {
               Node $$4 = $$2.getNextNode();
               return $$1.equals($$3.asBlockPos()) || $$1.equals($$4.asBlockPos());
            }
         }
      }
   }

   private static boolean isDoorTooFarAway(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, GlobalPos $$2) {
      return $$2.dimension() != $$0.dimension() || !$$2.pos().closerToCenterThan($$1.position(), 3.0);
   }

   private static Optional<Set<GlobalPos>> rememberDoorToClose(
      MemoryAccessor<Mu, Set<GlobalPos>> $$0, Optional<Set<GlobalPos>> $$1, ServerLevel $$2, BlockPos $$3
   ) {
      GlobalPos $$4 = GlobalPos.of($$2.dimension(), $$3);
      return Optional.of($$1.<Set<GlobalPos>>map($$1x -> {
         $$1x.add($$4);
         return $$1x;
      }).orElseGet(() -> {
         Set<GlobalPos> $$2x = Sets.newHashSet(new GlobalPos[]{$$4});
         $$0.set($$2x);
         return $$2x;
      }));
   }
}
