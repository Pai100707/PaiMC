package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record Ignite(LevelBasedValue duration) implements EnchantmentEntityEffect {
   public static final MapCodec<Ignite> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LevelBasedValue.CODEC.fieldOf("duration").forGetter($$0x -> $$0x.duration)).apply($$0, Ignite::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      $$3.igniteForSeconds(this.duration.calculate($$1));
   }

   @Override
   public MapCodec<Ignite> codec() {
      return CODEC;
   }
}
