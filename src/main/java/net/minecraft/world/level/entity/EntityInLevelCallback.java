package net.minecraft.world.level.entity;

import net.minecraft.world.entity.Entity.RemovalReason;

public interface EntityInLevelCallback {
   EntityInLevelCallback NULL = new EntityInLevelCallback() {
      @Override
      public void onMove() {
      }

      @Override
      public void onRemove(RemovalReason $$0) {
      }
   };

   void onMove();

   void onRemove(RemovalReason var1);
}
