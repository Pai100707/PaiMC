/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record EntityTypePredicate(HolderSet<EntityType<?>> types) {
    public static final Codec<EntityTypePredicate> CODEC = RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).xmap(EntityTypePredicate::new, EntityTypePredicate::types);

    public static EntityTypePredicate of(HolderGetter<EntityType<?>> $$0, EntityType<?> $$1) {
        return new EntityTypePredicate(HolderSet.direct($$1.builtInRegistryHolder()));
    }

    public static EntityTypePredicate of(HolderGetter<EntityType<?>> $$0, TagKey<EntityType<?>> $$1) {
        return new EntityTypePredicate($$0.getOrThrow($$1));
    }

    public boolean matches(EntityType<?> $$0) {
        return $$0.is(this.types);
    }
}

