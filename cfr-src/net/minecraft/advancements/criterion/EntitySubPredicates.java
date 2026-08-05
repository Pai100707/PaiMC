/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.FishingHookPredicate;
import net.minecraft.advancements.criterion.LightningBoltPredicate;
import net.minecraft.advancements.criterion.PlayerPredicate;
import net.minecraft.advancements.criterion.RaiderPredicate;
import net.minecraft.advancements.criterion.SheepPredicate;
import net.minecraft.advancements.criterion.SlimePredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class EntitySubPredicates {
    public static final MapCodec<LightningBoltPredicate> LIGHTNING = EntitySubPredicates.register("lightning", LightningBoltPredicate.CODEC);
    public static final MapCodec<FishingHookPredicate> FISHING_HOOK = EntitySubPredicates.register("fishing_hook", FishingHookPredicate.CODEC);
    public static final MapCodec<PlayerPredicate> PLAYER = EntitySubPredicates.register("player", PlayerPredicate.CODEC);
    public static final MapCodec<SlimePredicate> SLIME = EntitySubPredicates.register("slime", SlimePredicate.CODEC);
    public static final MapCodec<RaiderPredicate> RAIDER = EntitySubPredicates.register("raider", RaiderPredicate.CODEC);
    public static final MapCodec<SheepPredicate> SHEEP = EntitySubPredicates.register("sheep", SheepPredicate.CODEC);

    private static <T extends EntitySubPredicate> MapCodec<T> register(String $$0, MapCodec<T> $$1) {
        return Registry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, $$0, $$1);
    }

    public static MapCodec<? extends EntitySubPredicate> bootstrap(Registry<MapCodec<? extends EntitySubPredicate>> $$0) {
        return LIGHTNING;
    }
}

