/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCraftedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, ResourceKey<Recipe<?>> $$1, List<ItemStack> $$22) {
        this.trigger($$0, $$2 -> $$2.matches($$1, $$22));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipeId, List<ItemPredicate> ingredients) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)Recipe.KEY_CODEC.fieldOf("recipe_id").forGetter(TriggerInstance::recipeId), (App)ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(TriggerInstance::ingredients)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> craftedItem(ResourceKey<Recipe<?>> $$0, List<ItemPredicate.Builder> $$1) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), $$0, $$1.stream().map(ItemPredicate.Builder::build).toList()));
        }

        public static Criterion<TriggerInstance> craftedItem(ResourceKey<Recipe<?>> $$0) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), $$0, List.of()));
        }

        public static Criterion<TriggerInstance> crafterCraftedItem(ResourceKey<Recipe<?>> $$0) {
            return CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), $$0, List.of()));
        }

        boolean matches(ResourceKey<Recipe<?>> $$0, List<ItemStack> $$1) {
            if ($$0 != this.recipeId) {
                return false;
            }
            ArrayList<ItemStack> $$2 = new ArrayList<ItemStack>($$1);
            for (ItemPredicate $$3 : this.ingredients) {
                boolean $$4 = false;
                Iterator $$5 = $$2.iterator();
                while ($$5.hasNext()) {
                    if (!$$3.test((ItemStack)$$5.next())) continue;
                    $$5.remove();
                    $$4 = true;
                    break;
                }
                if ($$4) continue;
                return false;
            }
            return true;
        }
    }
}

