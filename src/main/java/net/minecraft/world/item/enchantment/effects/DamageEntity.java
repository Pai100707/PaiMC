package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record DamageEntity(LevelBasedValue minDamage, LevelBasedValue maxDamage, Holder<DamageType> damageType) implements EnchantmentEntityEffect {
   public static final MapCodec<DamageEntity> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            LevelBasedValue.CODEC.fieldOf("min_damage").forGetter(DamageEntity::minDamage),
            LevelBasedValue.CODEC.fieldOf("max_damage").forGetter(DamageEntity::maxDamage),
            DamageType.CODEC.fieldOf("damage_type").forGetter(DamageEntity::damageType)
         )
         .apply($$0, DamageEntity::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      float $$5 = Mth.randomBetween($$3.getRandom(), this.minDamage.calculate($$1), this.maxDamage.calculate($$1));
      $$3.hurtServer($$0, new DamageSource(this.damageType, $$2.owner()), $$5);
   }

   @Override
   public MapCodec<DamageEntity> codec() {
      return CODEC;
   }
}
