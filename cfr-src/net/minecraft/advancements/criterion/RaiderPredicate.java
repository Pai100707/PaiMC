/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record RaiderPredicate(boolean hasRaid, boolean isCaptain) implements EntitySubPredicate
{
    public static final MapCodec<RaiderPredicate> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.BOOL.optionalFieldOf("has_raid", (Object)false).forGetter(RaiderPredicate::hasRaid), (App)Codec.BOOL.optionalFieldOf("is_captain", (Object)false).forGetter(RaiderPredicate::isCaptain)).apply((Applicative)$$0, RaiderPredicate::new));
    public static final RaiderPredicate CAPTAIN_WITHOUT_RAID = new RaiderPredicate(false, true);

    public MapCodec<RaiderPredicate> codec() {
        return EntitySubPredicates.RAIDER;
    }

    @Override
    public boolean matches(Entity $$0, ServerLevel $$1, @Nullable Vec3 $$2) {
        if ($$0 instanceof Raider) {
            Raider $$3 = (Raider)$$0;
            return $$3.hasRaid() == this.hasRaid && $$3.isCaptain() == this.isCaptain;
        }
        return false;
    }
}

