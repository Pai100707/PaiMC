package net.minecraft.world.entity.ambient;

import net.minecraft.world.level.Level;

public abstract class AmbientCreature extends net.minecraft.world.entity.Mob {
   protected AmbientCreature(net.minecraft.world.entity.EntityType<? extends AmbientCreature> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean canBeLeashed() {
      return false;
   }
}
