package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.world.level.block.BaseFireBlock;

public enum InsideBlockEffectType {
   FREEZE($$0 -> {
      $$0.setIsInPowderSnow(true);
      if ($$0.canFreeze()) {
         $$0.setTicksFrozen(Math.min($$0.getTicksRequiredToFreeze(), $$0.getTicksFrozen() + 1));
      }
   }),
   CLEAR_FREEZE(net.minecraft.world.entity.Entity::clearFreeze),
   FIRE_IGNITE(BaseFireBlock::fireIgnite),
   LAVA_IGNITE(net.minecraft.world.entity.Entity::lavaIgnite),
   EXTINGUISH(net.minecraft.world.entity.Entity::clearFire);

   private final Consumer<net.minecraft.world.entity.Entity> effect;

   private InsideBlockEffectType(final Consumer<net.minecraft.world.entity.Entity> $$0) {
      this.effect = $$0;
   }

   public Consumer<net.minecraft.world.entity.Entity> effect() {
      return this.effect;
   }
}
