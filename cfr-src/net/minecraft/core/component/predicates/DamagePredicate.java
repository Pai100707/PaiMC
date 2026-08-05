/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.core.component.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public record DamagePredicate(MinMaxBounds.Ints durability, MinMaxBounds.Ints damage) implements DataComponentPredicate
{
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("durability", (Object)MinMaxBounds.Ints.ANY).forGetter(DamagePredicate::durability), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("damage", (Object)MinMaxBounds.Ints.ANY).forGetter(DamagePredicate::damage)).apply((Applicative)$$0, DamagePredicate::new));

    @Override
    public boolean matches(DataComponentGetter $$0) {
        Integer $$1 = $$0.get(DataComponents.DAMAGE);
        if ($$1 == null) {
            return false;
        }
        int $$2 = $$0.getOrDefault(DataComponents.MAX_DAMAGE, 0);
        if (!this.durability.matches($$2 - $$1)) {
            return false;
        }
        return this.damage.matches($$1);
    }

    public static DamagePredicate durability(MinMaxBounds.Ints $$0) {
        return new DamagePredicate($$0, MinMaxBounds.Ints.ANY);
    }
}

