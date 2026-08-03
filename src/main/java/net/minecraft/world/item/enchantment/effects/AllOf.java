package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public interface AllOf {
   static <T, A extends T> MapCodec<A> codec(Codec<T> $$0, Function<List<T>, A> $$1, Function<A, List<T>> $$2) {
      return RecordCodecBuilder.mapCodec($$3 -> $$3.group($$0.listOf().fieldOf("effects").forGetter($$2)).apply($$3, $$1));
   }

   static AllOf.EntityEffects entityEffects(EnchantmentEntityEffect... $$0) {
      return new AllOf.EntityEffects(List.of($$0));
   }

   static AllOf.LocationBasedEffects locationBasedEffects(EnchantmentLocationBasedEffect... $$0) {
      return new AllOf.LocationBasedEffects(List.of($$0));
   }

   static AllOf.ValueEffects valueEffects(EnchantmentValueEffect... $$0) {
      return new AllOf.ValueEffects(List.of($$0));
   }

   public record EntityEffects(List<EnchantmentEntityEffect> effects) implements EnchantmentEntityEffect {
      public static final MapCodec<AllOf.EntityEffects> CODEC = AllOf.codec(
         EnchantmentEntityEffect.CODEC, AllOf.EntityEffects::new, AllOf.EntityEffects::effects
      );

      @Override
      public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
         for (EnchantmentEntityEffect $$5 : this.effects) {
            $$5.apply($$0, $$1, $$2, $$3, $$4);
         }
      }

      @Override
      public MapCodec<AllOf.EntityEffects> codec() {
         return CODEC;
      }
   }

   public record LocationBasedEffects(List<EnchantmentLocationBasedEffect> effects) implements EnchantmentLocationBasedEffect {
      public static final MapCodec<AllOf.LocationBasedEffects> CODEC = AllOf.codec(
         EnchantmentLocationBasedEffect.CODEC, AllOf.LocationBasedEffects::new, AllOf.LocationBasedEffects::effects
      );

      @Override
      public void onChangedBlock(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4, boolean $$5) {
         for (EnchantmentLocationBasedEffect $$6 : this.effects) {
            $$6.onChangedBlock($$0, $$1, $$2, $$3, $$4, $$5);
         }
      }

      @Override
      public void onDeactivated(EnchantedItemInUse $$0, Entity $$1, Vec3 $$2, int $$3) {
         for (EnchantmentLocationBasedEffect $$4 : this.effects) {
            $$4.onDeactivated($$0, $$1, $$2, $$3);
         }
      }

      @Override
      public MapCodec<AllOf.LocationBasedEffects> codec() {
         return CODEC;
      }
   }

   public record ValueEffects(List<EnchantmentValueEffect> effects) implements EnchantmentValueEffect {
      public static final MapCodec<AllOf.ValueEffects> CODEC = AllOf.codec(EnchantmentValueEffect.CODEC, AllOf.ValueEffects::new, AllOf.ValueEffects::effects);

      @Override
      public float process(int $$0, RandomSource $$1, float $$2) {
         for (EnchantmentValueEffect $$3 : this.effects) {
            $$2 = $$3.process($$0, $$1, $$2);
         }

         return $$2;
      }

      @Override
      public MapCodec<AllOf.ValueEffects> codec() {
         return CODEC;
      }
   }
}
