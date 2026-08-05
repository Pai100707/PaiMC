/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.Nullable;

public class KilledByArrowTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, Collection<Entity> $$1, @Nullable ItemStack $$2) {
        ArrayList $$32 = Lists.newArrayList();
        HashSet $$4 = Sets.newHashSet();
        for (Entity $$5 : $$1) {
            $$4.add($$5.getType());
            $$32.add(EntityPredicate.createContext($$0, $$5));
        }
        this.trigger($$0, $$3 -> $$3.matches($$32, $$4.size(), $$2));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, MinMaxBounds.Ints uniqueEntityTypes, Optional<ItemPredicate> firedFromWeapon) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(TriggerInstance::victims), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("unique_entity_types", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::uniqueEntityTypes), (App)ItemPredicate.CODEC.optionalFieldOf("fired_from_weapon").forGetter(TriggerInstance::firedFromWeapon)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> crossbowKilled(HolderGetter<Item> $$0, EntityPredicate.Builder ... $$1) {
            return CriteriaTriggers.KILLED_BY_ARROW.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$1), MinMaxBounds.Ints.ANY, Optional.of(ItemPredicate.Builder.item().of($$0, Items.CROSSBOW).build())));
        }

        public static Criterion<TriggerInstance> crossbowKilled(HolderGetter<Item> $$0, MinMaxBounds.Ints $$1) {
            return CriteriaTriggers.KILLED_BY_ARROW.createCriterion(new TriggerInstance(Optional.empty(), List.of(), $$1, Optional.of(ItemPredicate.Builder.item().of($$0, Items.CROSSBOW).build())));
        }

        public boolean matches(Collection<LootContext> $$0, int $$1, @Nullable ItemStack $$2) {
            if (this.firedFromWeapon.isPresent() && ($$2 == null || !this.firedFromWeapon.get().test($$2))) {
                return false;
            }
            if (!this.victims.isEmpty()) {
                ArrayList $$3 = Lists.newArrayList($$0);
                for (ContextAwarePredicate $$4 : this.victims) {
                    boolean $$5 = false;
                    Iterator $$6 = $$3.iterator();
                    while ($$6.hasNext()) {
                        LootContext $$7 = (LootContext)$$6.next();
                        if (!$$4.matches($$7)) continue;
                        $$6.remove();
                        $$5 = true;
                        break;
                    }
                    if ($$5) continue;
                    return false;
                }
            }
            return this.uniqueEntityTypes.matches($$1);
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntities(this.victims, "victims");
        }
    }
}

