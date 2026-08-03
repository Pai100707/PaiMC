package net.minecraft.world.entity;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface ItemOwner {
   Level level();

   Vec3 position();

   float getVisualRotationYInDegrees();

   @Nullable
   default net.minecraft.world.entity.LivingEntity asLivingEntity() {
      return null;
   }

   static net.minecraft.world.entity.ItemOwner offsetFromOwner(net.minecraft.world.entity.ItemOwner $$0, Vec3 $$1) {
      return new net.minecraft.world.entity.ItemOwner.OffsetFromOwner($$0, $$1);
   }

   public record OffsetFromOwner(net.minecraft.world.entity.ItemOwner owner, Vec3 offset) implements net.minecraft.world.entity.ItemOwner {
      @Override
      public Level level() {
         return this.owner.level();
      }

      @Override
      public Vec3 position() {
         return this.owner.position().add(this.offset);
      }

      @Override
      public float getVisualRotationYInDegrees() {
         return this.owner.getVisualRotationYInDegrees();
      }

      @Nullable
      @Override
      public net.minecraft.world.entity.LivingEntity asLivingEntity() {
         return this.owner.asLivingEntity();
      }
   }
}
