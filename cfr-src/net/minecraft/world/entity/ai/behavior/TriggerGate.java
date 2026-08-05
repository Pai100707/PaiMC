/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public class TriggerGate {
    public static <E extends LivingEntity> OneShot<E> triggerOneShuffled(List<Pair<? extends Trigger<? super E>, Integer>> $$0) {
        return TriggerGate.triggerGate($$0, GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE);
    }

    public static <E extends LivingEntity> OneShot<E> triggerGate(List<Pair<? extends Trigger<? super E>, Integer>> $$0, GateBehavior.OrderPolicy $$12, GateBehavior.RunningPolicy $$2) {
        ShufflingList $$3 = new ShufflingList();
        $$0.forEach($$1 -> $$3.add((Trigger)$$1.getFirst(), (Integer)$$1.getSecond()));
        return BehaviorBuilder.create($$32 -> $$32.point(($$3, $$4, $$5) -> {
            Trigger $$6;
            if ($$12 == GateBehavior.OrderPolicy.SHUFFLED) {
                $$3.shuffle();
            }
            Iterator iterator = $$3.iterator();
            while (iterator.hasNext() && (!($$6 = (Trigger)iterator.next()).trigger($$3, $$4, $$5) || $$2 != GateBehavior.RunningPolicy.RUN_ONE)) {
            }
            return true;
        }));
    }
}

