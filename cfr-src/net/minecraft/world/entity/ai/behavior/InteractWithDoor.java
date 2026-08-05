/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.kinds.OptionalBox$Mu
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.OptionalBox;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
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

    public static BehaviorControl<LivingEntity> create() {
        MutableObject $$0 = new MutableObject();
        MutableInt $$1 = new MutableInt(0);
        return BehaviorBuilder.create($$2 -> $$2.group($$2.present(MemoryModuleType.PATH), $$2.registered(MemoryModuleType.DOORS_TO_CLOSE), $$2.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)).apply((Applicative)$$2, ($$3, $$4, $$5) -> ($$62, $$7, $$8) -> {
            DoorBlock $$18;
            BlockPos $$16;
            BlockState $$17;
            Path $$9 = (Path)$$2.get($$3);
            Optional<Set<GlobalPos>> $$10 = $$2.tryGet($$4);
            if ($$9.notStarted() || $$9.isDone()) {
                return false;
            }
            if (Objects.equals($$0.get(), $$9.getNextNode())) {
                $$1.setValue(20);
            } else if ($$1.decrementAndGet() > 0) {
                return false;
            }
            $$0.setValue((Object)$$9.getNextNode());
            Node $$11 = $$9.getPreviousNode();
            Node $$12 = $$9.getNextNode();
            BlockPos $$13 = $$11.asBlockPos();
            BlockState $$14 = $$62.getBlockState($$13);
            if ($$14.is(BlockTags.MOB_INTERACTABLE_DOORS, $$0 -> $$0.getBlock() instanceof DoorBlock)) {
                DoorBlock $$15 = (DoorBlock)$$14.getBlock();
                if (!$$15.isOpen($$14)) {
                    $$15.setOpen($$7, $$62, $$14, $$13, true);
                }
                $$10 = InteractWithDoor.rememberDoorToClose($$4, $$10, $$62, $$13);
            }
            if (($$17 = $$62.getBlockState($$16 = $$12.asBlockPos())).is(BlockTags.MOB_INTERACTABLE_DOORS, $$0 -> $$0.getBlock() instanceof DoorBlock) && !($$18 = (DoorBlock)$$17.getBlock()).isOpen($$17)) {
                $$18.setOpen($$7, $$62, $$17, $$16, true);
                $$10 = InteractWithDoor.rememberDoorToClose($$4, $$10, $$62, $$16);
            }
            $$10.ifPresent($$6 -> InteractWithDoor.closeDoorsThatIHaveOpenedOrPassedThrough($$62, $$7, $$11, $$12, $$6, $$2.tryGet($$5)));
            return true;
        }));
    }

    public static void closeDoorsThatIHaveOpenedOrPassedThrough(ServerLevel $$02, LivingEntity $$1, @Nullable Node $$2, @Nullable Node $$3, Set<GlobalPos> $$4, Optional<List<LivingEntity>> $$5) {
        Iterator<GlobalPos> $$6 = $$4.iterator();
        while ($$6.hasNext()) {
            GlobalPos $$7 = $$6.next();
            BlockPos $$8 = $$7.pos();
            if ($$2 != null && $$2.asBlockPos().equals($$8) || $$3 != null && $$3.asBlockPos().equals($$8)) continue;
            if (InteractWithDoor.isDoorTooFarAway($$02, $$1, $$7)) {
                $$6.remove();
                continue;
            }
            BlockState $$9 = $$02.getBlockState($$8);
            if (!$$9.is(BlockTags.MOB_INTERACTABLE_DOORS, $$0 -> $$0.getBlock() instanceof DoorBlock)) {
                $$6.remove();
                continue;
            }
            DoorBlock $$10 = (DoorBlock)$$9.getBlock();
            if (!$$10.isOpen($$9)) {
                $$6.remove();
                continue;
            }
            if (InteractWithDoor.areOtherMobsComingThroughDoor($$1, $$8, $$5)) {
                $$6.remove();
                continue;
            }
            $$10.setOpen($$1, $$02, $$9, $$8, false);
            $$6.remove();
        }
    }

    private static boolean areOtherMobsComingThroughDoor(LivingEntity $$0, BlockPos $$12, Optional<List<LivingEntity>> $$2) {
        if ($$2.isEmpty()) {
            return false;
        }
        return $$2.get().stream().filter($$1 -> $$1.getType() == $$0.getType()).filter($$1 -> $$12.closerToCenterThan($$1.position(), 2.0)).anyMatch($$1 -> InteractWithDoor.isMobComingThroughDoor($$1.getBrain(), $$12));
    }

    private static boolean isMobComingThroughDoor(Brain<?> $$0, BlockPos $$1) {
        if (!$$0.hasMemoryValue(MemoryModuleType.PATH)) {
            return false;
        }
        Path $$2 = $$0.getMemory(MemoryModuleType.PATH).get();
        if ($$2.isDone()) {
            return false;
        }
        Node $$3 = $$2.getPreviousNode();
        if ($$3 == null) {
            return false;
        }
        Node $$4 = $$2.getNextNode();
        return $$1.equals($$3.asBlockPos()) || $$1.equals($$4.asBlockPos());
    }

    private static boolean isDoorTooFarAway(ServerLevel $$0, LivingEntity $$1, GlobalPos $$2) {
        return $$2.dimension() != $$0.dimension() || !$$2.pos().closerToCenterThan($$1.position(), 3.0);
    }

    private static Optional<Set<GlobalPos>> rememberDoorToClose(MemoryAccessor<OptionalBox.Mu, Set<GlobalPos>> $$0, Optional<Set<GlobalPos>> $$12, ServerLevel $$2, BlockPos $$3) {
        GlobalPos $$4 = GlobalPos.of($$2.dimension(), $$3);
        return Optional.of($$12.map($$1 -> {
            $$1.add($$4);
            return $$1;
        }).orElseGet(() -> {
            HashSet $$2 = Sets.newHashSet((Object[])new GlobalPos[]{$$4});
            $$0.set($$2);
            return $$2;
        }));
    }
}

