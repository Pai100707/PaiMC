package net.minecraft.world.entity;

import java.util.List;
import net.minecraft.world.phys.Vec3;

public enum EntityAttachment {
   PASSENGER(net.minecraft.world.entity.EntityAttachment.Fallback.AT_HEIGHT),
   VEHICLE(net.minecraft.world.entity.EntityAttachment.Fallback.AT_FEET),
   NAME_TAG(net.minecraft.world.entity.EntityAttachment.Fallback.AT_HEIGHT),
   WARDEN_CHEST(net.minecraft.world.entity.EntityAttachment.Fallback.AT_CENTER);

   private final net.minecraft.world.entity.EntityAttachment.Fallback fallback;

   private EntityAttachment(final net.minecraft.world.entity.EntityAttachment.Fallback $$0) {
      this.fallback = $$0;
   }

   public List<Vec3> createFallbackPoints(float $$0, float $$1) {
      return this.fallback.create($$0, $$1);
   }

   public interface Fallback {
      List<Vec3> ZERO = List.of(Vec3.ZERO);
      net.minecraft.world.entity.EntityAttachment.Fallback AT_FEET = ($$0, $$1) -> ZERO;
      net.minecraft.world.entity.EntityAttachment.Fallback AT_HEIGHT = ($$0, $$1) -> List.of(new Vec3(0.0, $$1, 0.0));
      net.minecraft.world.entity.EntityAttachment.Fallback AT_CENTER = ($$0, $$1) -> List.of(new Vec3(0.0, $$1 / 2.0, 0.0));

      List<Vec3> create(float var1, float var2);
   }
}
