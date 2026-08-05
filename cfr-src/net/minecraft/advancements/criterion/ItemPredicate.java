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
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemPredicate(Optional<HolderSet<Item>> items, MinMaxBounds.Ints count, DataComponentMatchers components) implements Predicate<ItemStack>
{
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(ItemPredicate::items), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("count", (Object)MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count), (App)DataComponentMatchers.CODEC.forGetter(ItemPredicate::components)).apply((Applicative)$$0, ItemPredicate::new));

    @Override
    public boolean test(ItemStack $$0) {
        if (this.items.isPresent() && !$$0.is(this.items.get())) {
            return false;
        }
        if (!this.count.matches($$0.getCount())) {
            return false;
        }
        return this.components.test($$0);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((ItemStack)object);
    }

    public static class Builder {
        private Optional<HolderSet<Item>> items = Optional.empty();
        private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
        private DataComponentMatchers components = DataComponentMatchers.ANY;

        public static Builder item() {
            return new Builder();
        }

        public Builder of(HolderGetter<Item> $$02, ItemLike ... $$1) {
            this.items = Optional.of(HolderSet.direct($$0 -> $$0.asItem().builtInRegistryHolder(), $$1));
            return this;
        }

        public Builder of(HolderGetter<Item> $$0, TagKey<Item> $$1) {
            this.items = Optional.of($$0.getOrThrow($$1));
            return this;
        }

        public Builder withCount(MinMaxBounds.Ints $$0) {
            this.count = $$0;
            return this;
        }

        public Builder withComponents(DataComponentMatchers $$0) {
            this.components = $$0;
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.items, this.count, this.components);
        }
    }
}

