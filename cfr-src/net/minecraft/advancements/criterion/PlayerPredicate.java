/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMaps
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.advancements.criterion.GameTypePredicate;
import net.minecraft.advancements.criterion.InputPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record PlayerPredicate(MinMaxBounds.Ints level, GameTypePredicate gameType, List<StatMatcher<?>> stats, Object2BooleanMap<ResourceKey<Recipe<?>>> recipes, Map<Identifier, AdvancementPredicate> advancements, Optional<EntityPredicate> lookingAt, Optional<InputPredicate> input) implements EntitySubPredicate
{
    public static final int LOOKING_AT_RANGE = 100;
    public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("level", (Object)MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate::level), (App)GameTypePredicate.CODEC.optionalFieldOf("gamemode", (Object)GameTypePredicate.ANY).forGetter(PlayerPredicate::gameType), (App)StatMatcher.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(PlayerPredicate::stats), (App)ExtraCodecs.object2BooleanMap(Recipe.KEY_CODEC).optionalFieldOf("recipes", (Object)Object2BooleanMaps.emptyMap()).forGetter(PlayerPredicate::recipes), (App)Codec.unboundedMap(Identifier.CODEC, AdvancementPredicate.CODEC).optionalFieldOf("advancements", Map.of()).forGetter(PlayerPredicate::advancements), (App)EntityPredicate.CODEC.optionalFieldOf("looking_at").forGetter(PlayerPredicate::lookingAt), (App)InputPredicate.CODEC.optionalFieldOf("input").forGetter(PlayerPredicate::input)).apply((Applicative)$$0, PlayerPredicate::new));

    /*
     * WARNING - void declaration
     */
    @Override
    public boolean matches(Entity $$02, ServerLevel $$1, @Nullable Vec3 $$2) {
        void $$4;
        if (!($$02 instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer $$3 = (ServerPlayer)$$02;
        if (!this.level.matches($$4.experienceLevel)) {
            return false;
        }
        if (!this.gameType.matches($$4.gameMode())) {
            return false;
        }
        ServerStatsCounter $$5 = $$4.getStats();
        for (StatMatcher<?> statMatcher : this.stats) {
            if (statMatcher.matches($$5)) continue;
            return false;
        }
        ServerRecipeBook $$7 = $$4.getRecipeBook();
        for (Object2BooleanMap.Entry $$8 : this.recipes.object2BooleanEntrySet()) {
            if ($$7.contains((ResourceKey)$$8.getKey()) == $$8.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancements playerAdvancements = $$4.getAdvancements();
            ServerAdvancementManager $$10 = $$4.level().getServer().getAdvancements();
            for (Map.Entry<Identifier, AdvancementPredicate> $$11 : this.advancements.entrySet()) {
                AdvancementHolder $$12 = $$10.get($$11.getKey());
                if ($$12 != null && $$11.getValue().test(playerAdvancements.getOrStartProgress($$12))) continue;
                return false;
            }
        }
        if (this.lookingAt.isPresent()) {
            Vec3 vec3 = $$4.getEyePosition();
            Vec3 $$14 = $$4.getViewVector(1.0f);
            Vec3 $$15 = vec3.add($$14.x * 100.0, $$14.y * 100.0, $$14.z * 100.0);
            EntityHitResult $$16 = ProjectileUtil.getEntityHitResult($$4.level(), (Entity)$$4, vec3, $$15, new AABB(vec3, $$15).inflate(1.0), $$0 -> !$$0.isSpectator(), 0.0f);
            if ($$16 == null || $$16.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            Entity $$17 = $$16.getEntity();
            if (!this.lookingAt.get().matches((ServerPlayer)$$4, $$17) || !$$4.hasLineOfSight($$17)) {
                return false;
            }
        }
        return !this.input.isPresent() || this.input.get().matches($$4.getLastClientInput());
    }

    public MapCodec<PlayerPredicate> codec() {
        return EntitySubPredicates.PLAYER;
    }

    record StatMatcher<T>(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range, Supplier<Stat<T>> stat) {
        public static final Codec<StatMatcher<?>> CODEC = BuiltInRegistries.STAT_TYPE.byNameCodec().dispatch(StatMatcher::type, StatMatcher::createTypedCodec);

        public StatMatcher(StatType<T> $$0, Holder<T> $$1, MinMaxBounds.Ints $$2) {
            this($$0, $$1, $$2, (Supplier<Stat<T>>)Suppliers.memoize(() -> $$0.get($$1.value())));
        }

        private static <T> MapCodec<StatMatcher<T>> createTypedCodec(StatType<T> $$0) {
            return RecordCodecBuilder.mapCodec($$12 -> $$12.group((App)$$0.getRegistry().holderByNameCodec().fieldOf("stat").forGetter(StatMatcher::value), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("value", (Object)MinMaxBounds.Ints.ANY).forGetter(StatMatcher::range)).apply((Applicative)$$12, ($$1, $$2) -> new StatMatcher($$0, $$1, (MinMaxBounds.Ints)$$2)));
        }

        public boolean matches(StatsCounter $$0) {
            return this.range.matches($$0.getValue(this.stat.get()));
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public static final Codec<AdvancementPredicate> CODEC = Codec.either(AdvancementDonePredicate.CODEC, AdvancementCriterionsPredicate.CODEC).xmap(Either::unwrap, $$0 -> {
            if ($$0 instanceof AdvancementDonePredicate) {
                AdvancementDonePredicate $$1 = (AdvancementDonePredicate)$$0;
                return Either.left((Object)$$1);
            }
            if ($$0 instanceof AdvancementCriterionsPredicate) {
                AdvancementCriterionsPredicate $$2 = (AdvancementCriterionsPredicate)$$0;
                return Either.right((Object)$$2);
            }
            throw new UnsupportedOperationException();
        });
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        private GameTypePredicate gameType = GameTypePredicate.ANY;
        private final ImmutableList.Builder<StatMatcher<?>> stats = ImmutableList.builder();
        private final Object2BooleanMap<ResourceKey<Recipe<?>>> recipes = new Object2BooleanOpenHashMap();
        private final Map<Identifier, AdvancementPredicate> advancements = Maps.newHashMap();
        private Optional<EntityPredicate> lookingAt = Optional.empty();
        private Optional<InputPredicate> input = Optional.empty();

        public static Builder player() {
            return new Builder();
        }

        public Builder setLevel(MinMaxBounds.Ints $$0) {
            this.level = $$0;
            return this;
        }

        public <T> Builder addStat(StatType<T> $$0, Holder.Reference<T> $$1, MinMaxBounds.Ints $$2) {
            this.stats.add(new StatMatcher<T>($$0, $$1, $$2));
            return this;
        }

        public Builder addRecipe(ResourceKey<Recipe<?>> $$0, boolean $$1) {
            this.recipes.put($$0, $$1);
            return this;
        }

        public Builder setGameType(GameTypePredicate $$0) {
            this.gameType = $$0;
            return this;
        }

        public Builder setLookingAt(EntityPredicate.Builder $$0) {
            this.lookingAt = Optional.of($$0.build());
            return this;
        }

        public Builder checkAdvancementDone(Identifier $$0, boolean $$1) {
            this.advancements.put($$0, new AdvancementDonePredicate($$1));
            return this;
        }

        public Builder checkAdvancementCriterions(Identifier $$0, Map<String, Boolean> $$1) {
            this.advancements.put($$0, new AdvancementCriterionsPredicate((Object2BooleanMap<String>)new Object2BooleanOpenHashMap($$1)));
            return this;
        }

        public Builder hasInput(InputPredicate $$0) {
            this.input = Optional.of($$0);
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.gameType, (List<StatMatcher<?>>)this.stats.build(), this.recipes, this.advancements, this.lookingAt, this.input);
        }
    }

    record AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions) implements AdvancementPredicate
    {
        public static final Codec<AdvancementCriterionsPredicate> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING).xmap(AdvancementCriterionsPredicate::new, AdvancementCriterionsPredicate::criterions);

        @Override
        public boolean test(AdvancementProgress $$0) {
            for (Object2BooleanMap.Entry $$1 : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress $$2 = $$0.getCriterion((String)$$1.getKey());
                if ($$2 != null && $$2.isDone() == $$1.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }

    record AdvancementDonePredicate(boolean state) implements AdvancementPredicate
    {
        public static final Codec<AdvancementDonePredicate> CODEC = Codec.BOOL.xmap(AdvancementDonePredicate::new, AdvancementDonePredicate::state);

        @Override
        public boolean test(AdvancementProgress $$0) {
            return $$0.isDone() == this.state;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }
}

