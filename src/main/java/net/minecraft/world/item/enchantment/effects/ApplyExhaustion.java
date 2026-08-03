package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ApplyExhaustion(LevelBasedValue amount) implements EnchantmentEntityEffect {
   public static final MapCodec<ApplyExhaustion> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter(ApplyExhaustion::amount)).apply($$0, ApplyExhaustion::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      if ($$3 instanceof Player $$5) {
         $$5.causeFoodExhaustion(this.amount.calculate($$1));
      }
   }

   @Override
   public MapCodec<ApplyExhaustion> codec() {
      return CODEC;
   }
}
