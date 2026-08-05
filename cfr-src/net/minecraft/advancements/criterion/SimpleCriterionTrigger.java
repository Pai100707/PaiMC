/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;

public abstract class SimpleCriterionTrigger<T extends SimpleInstance>
implements CriterionTrigger<T> {
    private final Map<PlayerAdvancements, Set<CriterionTrigger.Listener<T>>> players = Maps.newIdentityHashMap();

    @Override
    public final void addPlayerListener(PlayerAdvancements $$02, CriterionTrigger.Listener<T> $$1) {
        this.players.computeIfAbsent($$02, $$0 -> Sets.newHashSet()).add($$1);
    }

    @Override
    public final void removePlayerListener(PlayerAdvancements $$0, CriterionTrigger.Listener<T> $$1) {
        Set<CriterionTrigger.Listener<T>> $$2 = this.players.get($$0);
        if ($$2 != null) {
            $$2.remove($$1);
            if ($$2.isEmpty()) {
                this.players.remove($$0);
            }
        }
    }

    @Override
    public final void removePlayerListeners(PlayerAdvancements $$0) {
        this.players.remove($$0);
    }

    protected void trigger(ServerPlayer $$0, Predicate<T> $$1) {
        PlayerAdvancements $$2 = $$0.getAdvancements();
        Set<CriterionTrigger.Listener<T>> $$3 = this.players.get($$2);
        if ($$3 == null || $$3.isEmpty()) {
            return;
        }
        LootContext $$4 = EntityPredicate.createContext($$0, $$0);
        List $$5 = null;
        for (CriterionTrigger.Listener<T> $$6 : $$3) {
            Optional<ContextAwarePredicate> $$8;
            SimpleInstance $$7 = (SimpleInstance)$$6.trigger();
            if (!$$1.test($$7) || !($$8 = $$7.player()).isEmpty() && !$$8.get().matches($$4)) continue;
            if ($$5 == null) {
                $$5 = Lists.newArrayList();
            }
            $$5.add($$6);
        }
        if ($$5 != null) {
            for (CriterionTrigger.Listener<Object> $$9 : $$5) {
                $$9.run($$2);
            }
        }
    }

    public static interface SimpleInstance
    extends CriterionTriggerInstance {
        @Override
        default public void validate(CriterionValidator $$0) {
            $$0.validateEntity(this.player(), "player");
        }

        public Optional<ContextAwarePredicate> player();
    }
}

