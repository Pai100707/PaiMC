package net.minecraft.advancements.criterion;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public record PlayerPredicate(
   MinMaxBounds.Ints level,
   GameTypePredicate gameType,
   List<PlayerPredicate.StatMatcher<?>> stats,
   Object2BooleanMap<ResourceKey<Recipe<?>>> recipes,
   Map<Identifier, PlayerPredicate.AdvancementPredicate> advancements,
   Optional<EntityPredicate> lookingAt,
   Optional<InputPredicate> input
) implements EntitySubPredicate {
   public static final int LOOKING_AT_RANGE = 100;
   public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            MinMaxBounds.Ints.CODEC.optionalFieldOf("level", MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate::level),
            GameTypePredicate.CODEC.optionalFieldOf("gamemode", GameTypePredicate.ANY).forGetter(PlayerPredicate::gameType),
            PlayerPredicate.StatMatcher.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(PlayerPredicate::stats),
            ExtraCodecs.object2BooleanMap(Recipe.KEY_CODEC).optionalFieldOf("recipes", Object2BooleanMaps.emptyMap()).forGetter(PlayerPredicate::recipes),
            Codec.unboundedMap(Identifier.CODEC, PlayerPredicate.AdvancementPredicate.CODEC)
               .optionalFieldOf("advancements", Map.of())
               .forGetter(PlayerPredicate::advancements),
            EntityPredicate.CODEC.optionalFieldOf("looking_at").forGetter(PlayerPredicate::lookingAt),
            InputPredicate.CODEC.optionalFieldOf("input").forGetter(PlayerPredicate::input)
         )
         .apply($$0, PlayerPredicate::new)
   );

   @Override
   public boolean matches(Entity $$0, ServerLevel $$1, @Nullable Vec3 $$2) {
      if (!($$0 instanceof ServerPlayer $$3)) {
         return false;
      } else if (!this.level.matches($$3.experienceLevel)) {
         return false;
      } else if (!this.gameType.matches($$3.gameMode())) {
         return false;
      } else {
         StatsCounter $$5 = $$3.getStats();

         for (PlayerPredicate.StatMatcher<?> $$6 : this.stats) {
            if (!$$6.matches($$5)) {
               return false;
            }
         }

         ServerRecipeBook $$7 = $$3.getRecipeBook();
         ObjectIterator var13 = this.recipes.object2BooleanEntrySet().iterator();

         while (var13.hasNext()) {
            Entry<ResourceKey<Recipe<?>>> $$8 = (Entry<ResourceKey<Recipe<?>>>)var13.next();
            if ($$7.contains((ResourceKey)$$8.getKey()) != $$8.getBooleanValue()) {
               return false;
            }
         }

         if (!this.advancements.isEmpty()) {
            PlayerAdvancements $$9 = $$3.getAdvancements();
            ServerAdvancementManager $$10 = $$3.level().getServer().getAdvancements();

            for (java.util.Map.Entry<Identifier, PlayerPredicate.AdvancementPredicate> $$11 : this.advancements.entrySet()) {
               net.minecraft.advancements.AdvancementHolder $$12 = $$10.get($$11.getKey());
               if ($$12 == null || !$$11.getValue().test($$9.getOrStartProgress($$12))) {
                  return false;
               }
            }
         }

         if (this.lookingAt.isPresent()) {
            Vec3 $$13 = $$3.getEyePosition();
            Vec3 $$14 = $$3.getViewVector(1.0F);
            Vec3 $$15 = $$13.add($$14.x * 100.0, $$14.y * 100.0, $$14.z * 100.0);
            EntityHitResult $$16 = ProjectileUtil.getEntityHitResult(
               $$3.level(), $$3, $$13, $$15, new AABB($$13, $$15).inflate(1.0), $$0x -> !$$0x.isSpectator(), 0.0F
            );
            if ($$16 == null || $$16.getType() != Type.ENTITY) {
               return false;
            }

            Entity $$17 = $$16.getEntity();
            if (!this.lookingAt.get().matches($$3, $$17) || !$$3.hasLineOfSight($$17)) {
               return false;
            }
         }

         return !this.input.isPresent() || this.input.get().matches($$3.getLastClientInput());
      }
   }

   @Override
   public MapCodec<PlayerPredicate> codec() {
      return EntitySubPredicates.PLAYER;
   }

   record AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions) implements PlayerPredicate.AdvancementPredicate {
      public static final Codec<PlayerPredicate.AdvancementCriterionsPredicate> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING)
         .xmap(PlayerPredicate.AdvancementCriterionsPredicate::new, PlayerPredicate.AdvancementCriterionsPredicate::criterions);

      public boolean test(net.minecraft.advancements.AdvancementProgress $$0) {
         ObjectIterator var2 = this.criterions.object2BooleanEntrySet().iterator();

         while (var2.hasNext()) {
            Entry<String> $$1 = (Entry<String>)var2.next();
            net.minecraft.advancements.CriterionProgress $$2 = $$0.getCriterion((String)$$1.getKey());
            if ($$2 == null || $$2.isDone() != $$1.getBooleanValue()) {
               return false;
            }
         }

         return true;
      }
   }

   record AdvancementDonePredicate(boolean state) implements PlayerPredicate.AdvancementPredicate {
      public static final Codec<PlayerPredicate.AdvancementDonePredicate> CODEC = Codec.BOOL
         .xmap(PlayerPredicate.AdvancementDonePredicate::new, PlayerPredicate.AdvancementDonePredicate::state);

      public boolean test(net.minecraft.advancements.AdvancementProgress $$0) {
         return $$0.isDone() == this.state;
      }
   }

   interface AdvancementPredicate extends Predicate<net.minecraft.advancements.AdvancementProgress> {
      Codec<PlayerPredicate.AdvancementPredicate> CODEC = Codec.either(
            PlayerPredicate.AdvancementDonePredicate.CODEC, PlayerPredicate.AdvancementCriterionsPredicate.CODEC
         )
         .xmap(Either::unwrap, $$0 -> {
            if ($$0 instanceof PlayerPredicate.AdvancementDonePredicate $$1) {
               return Either.left($$1);
            } else if ($$0 instanceof PlayerPredicate.AdvancementCriterionsPredicate $$2) {
               return Either.right($$2);
            } else {
               throw new UnsupportedOperationException();
            }
         });
   }

   public static class Builder {
      private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
      private GameTypePredicate gameType = GameTypePredicate.ANY;
      private final com.google.common.collect.ImmutableList.Builder<PlayerPredicate.StatMatcher<?>> stats = ImmutableList.builder();
      private final Object2BooleanMap<ResourceKey<Recipe<?>>> recipes = new Object2BooleanOpenHashMap();
      private final Map<Identifier, PlayerPredicate.AdvancementPredicate> advancements = Maps.newHashMap();
      private Optional<EntityPredicate> lookingAt = Optional.empty();
      private Optional<InputPredicate> input = Optional.empty();

      public static PlayerPredicate.Builder player() {
         return new PlayerPredicate.Builder();
      }

      public PlayerPredicate.Builder setLevel(MinMaxBounds.Ints $$0) {
         this.level = $$0;
         return this;
      }

      public <T> PlayerPredicate.Builder addStat(StatType<T> $$0, Reference<T> $$1, MinMaxBounds.Ints $$2) {
         this.stats.add(new PlayerPredicate.StatMatcher($$0, $$1, $$2));
         return this;
      }

      public PlayerPredicate.Builder addRecipe(ResourceKey<Recipe<?>> $$0, boolean $$1) {
         this.recipes.put($$0, $$1);
         return this;
      }

      public PlayerPredicate.Builder setGameType(GameTypePredicate $$0) {
         this.gameType = $$0;
         return this;
      }

      public PlayerPredicate.Builder setLookingAt(EntityPredicate.Builder $$0) {
         this.lookingAt = Optional.of($$0.build());
         return this;
      }

      public PlayerPredicate.Builder checkAdvancementDone(Identifier $$0, boolean $$1) {
         this.advancements.put($$0, new PlayerPredicate.AdvancementDonePredicate($$1));
         return this;
      }

      public PlayerPredicate.Builder checkAdvancementCriterions(Identifier $$0, Map<String, Boolean> $$1) {
         this.advancements.put($$0, new PlayerPredicate.AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap($$1)));
         return this;
      }

      public PlayerPredicate.Builder hasInput(InputPredicate $$0) {
         this.input = Optional.of($$0);
         return this;
      }

      public PlayerPredicate build() {
         return new PlayerPredicate(this.level, this.gameType, this.stats.build(), this.recipes, this.advancements, this.lookingAt, this.input);
      }
   }

   record StatMatcher<T>(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range, Supplier<Stat<T>> stat) {
      public static final Codec<PlayerPredicate.StatMatcher<?>> CODEC = BuiltInRegistries.STAT_TYPE
         .byNameCodec()
         .dispatch(PlayerPredicate.StatMatcher::type, PlayerPredicate.StatMatcher::createTypedCodec);

      public StatMatcher(StatType<T> $$0, Holder<T> $$1, MinMaxBounds.Ints $$2) {
         this($$0, $$1, $$2, Suppliers.memoize(() -> $$0.get($$1.value())));
      }

      private static <T> MapCodec<PlayerPredicate.StatMatcher<T>> createTypedCodec(StatType<T> $$0) {
         return RecordCodecBuilder.mapCodec(
            $$1 -> $$1.group(
                  $$0.getRegistry().holderByNameCodec().fieldOf("stat").forGetter(PlayerPredicate.StatMatcher::value),
                  MinMaxBounds.Ints.CODEC.optionalFieldOf("value", MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate.StatMatcher::range)
               )
               .apply($$1, ($$1x, $$2) -> new PlayerPredicate.StatMatcher($$0, $$1x, $$2))
         );
      }

      public boolean matches(StatsCounter $$0) {
         return this.range.matches($$0.getValue(this.stat.get()));
      }
   }
}
