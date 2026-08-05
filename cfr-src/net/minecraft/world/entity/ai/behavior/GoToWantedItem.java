/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;

public class GoToWantedItem {
    public static BehaviorControl<LivingEntity> create(float $$02, boolean $$1, int $$2) {
        return GoToWantedItem.create($$0 -> true, $$02, $$1, $$2);
    }

    public static <E extends LivingEntity> BehaviorControl<E> create(Predicate<E> $$0, float $$1, boolean $$2, int $$3) {
        return BehaviorBuilder.create($$42 -> {
            BehaviorBuilder $$52 = $$2 ? $$42.registered(MemoryModuleType.WALK_TARGET) : $$42.absent(MemoryModuleType.WALK_TARGET);
            return $$42.group($$42.registered(MemoryModuleType.LOOK_TARGET), $$52, $$42.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), $$42.registered(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)).apply((Applicative)$$42, ($$4, $$5, $$6, $$7) -> ($$8, $$9, $$10) -> {
                ItemEntity $$11 = (ItemEntity)$$42.get($$6);
                if ($$42.tryGet($$7).isEmpty() && $$0.test($$9) && $$11.closerThan($$9, $$3) && $$9.level().getWorldBorder().isWithinBounds($$11.blockPosition()) && $$9.canPickUpLoot()) {
                    WalkTarget $$12 = new WalkTarget(new EntityTracker($$11, false), $$1, 0);
                    $$4.set(new EntityTracker($$11, true));
                    $$5.set($$12);
                    return true;
                }
                return false;
            });
        });
    }
}

