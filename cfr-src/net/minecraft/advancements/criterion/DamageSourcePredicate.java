/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;

public record DamageSourcePredicate(List<TagPredicate<DamageType>> tags, Optional<EntityPredicate> directEntity, Optional<EntityPredicate> sourceEntity, Optional<Boolean> isDirect) {
    public static final Codec<DamageSourcePredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)TagPredicate.codec(Registries.DAMAGE_TYPE).listOf().optionalFieldOf("tags", List.of()).forGetter(DamageSourcePredicate::tags), (App)EntityPredicate.CODEC.optionalFieldOf("direct_entity").forGetter(DamageSourcePredicate::directEntity), (App)EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamageSourcePredicate::sourceEntity), (App)Codec.BOOL.optionalFieldOf("is_direct").forGetter(DamageSourcePredicate::isDirect)).apply((Applicative)$$0, DamageSourcePredicate::new));

    public boolean matches(ServerPlayer $$0, DamageSource $$1) {
        return this.matches($$0.level(), $$0.position(), $$1);
    }

    public boolean matches(ServerLevel $$0, Vec3 $$1, DamageSource $$2) {
        for (TagPredicate<DamageType> $$3 : this.tags) {
            if ($$3.matches($$2.typeHolder())) continue;
            return false;
        }
        if (this.directEntity.isPresent() && !this.directEntity.get().matches($$0, $$1, $$2.getDirectEntity())) {
            return false;
        }
        if (this.sourceEntity.isPresent() && !this.sourceEntity.get().matches($$0, $$1, $$2.getEntity())) {
            return false;
        }
        return !this.isDirect.isPresent() || this.isDirect.get().booleanValue() == $$2.isDirect();
    }

    public static class Builder {
        private final ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
        private Optional<EntityPredicate> directEntity = Optional.empty();
        private Optional<EntityPredicate> sourceEntity = Optional.empty();
        private Optional<Boolean> isDirect = Optional.empty();

        public static Builder damageType() {
            return new Builder();
        }

        public Builder tag(TagPredicate<DamageType> $$0) {
            this.tags.add($$0);
            return this;
        }

        public Builder direct(EntityPredicate.Builder $$0) {
            this.directEntity = Optional.of($$0.build());
            return this;
        }

        public Builder source(EntityPredicate.Builder $$0) {
            this.sourceEntity = Optional.of($$0.build());
            return this;
        }

        public Builder isDirect(boolean $$0) {
            this.isDirect = Optional.of($$0);
            return this;
        }

        public DamageSourcePredicate build() {
            return new DamageSourcePredicate((List<TagPredicate<DamageType>>)this.tags.build(), this.directEntity, this.sourceEntity, this.isDirect);
        }
    }
}

