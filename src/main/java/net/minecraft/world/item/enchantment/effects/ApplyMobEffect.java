package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ApplyMobEffect(
   HolderSet<MobEffect> toApply, LevelBasedValue minDuration, LevelBasedValue maxDuration, LevelBasedValue minAmplifier, LevelBasedValue maxAmplifier
) implements EnchantmentEntityEffect {
   public static final MapCodec<ApplyMobEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("to_apply").forGetter(ApplyMobEffect::toApply),
            LevelBasedValue.CODEC.fieldOf("min_duration").forGetter(ApplyMobEffect::minDuration),
            LevelBasedValue.CODEC.fieldOf("max_duration").forGetter(ApplyMobEffect::maxDuration),
            LevelBasedValue.CODEC.fieldOf("min_amplifier").forGetter(ApplyMobEffect::minAmplifier),
            LevelBasedValue.CODEC.fieldOf("max_amplifier").forGetter(ApplyMobEffect::maxAmplifier)
         )
         .apply($$0, ApplyMobEffect::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      if ($$3 instanceof LivingEntity $$5) {
         RandomSource $$6 = $$5.getRandom();
         Optional<Holder<MobEffect>> $$7 = this.toApply.getRandomElement($$6);
         if ($$7.isPresent()) {
            int $$8 = Math.round(Mth.randomBetween($$6, this.minDuration.calculate($$1), this.maxDuration.calculate($$1)) * 20.0F);
            int $$9 = Math.max(0, Math.round(Mth.randomBetween($$6, this.minAmplifier.calculate($$1), this.maxAmplifier.calculate($$1))));
            $$5.addEffect(new MobEffectInstance($$7.get(), $$8, $$9));
         }
      }
   }

   @Override
   public MapCodec<ApplyMobEffect> codec() {
      return CODEC;
   }
}
