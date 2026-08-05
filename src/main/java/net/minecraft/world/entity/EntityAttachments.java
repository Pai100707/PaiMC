package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;

public class EntityAttachments {
   private final Map<net.minecraft.world.entity.EntityAttachment, List<Vec3>> attachments;

   EntityAttachments(Map<net.minecraft.world.entity.EntityAttachment, List<Vec3>> $$0) {
      this.attachments = $$0;
   }

   public static net.minecraft.world.entity.EntityAttachments createDefault(float $$0, float $$1) {
      return builder().build($$0, $$1);
   }

   public static net.minecraft.world.entity.EntityAttachments.Builder builder() {
      return new net.minecraft.world.entity.EntityAttachments.Builder();
   }

   public net.minecraft.world.entity.EntityAttachments scale(float $$0, float $$1, float $$2) {
      return new net.minecraft.world.entity.EntityAttachments(Util.makeEnumMap(net.minecraft.world.entity.EntityAttachment.class, $$3 -> {
         List<Vec3> $$4 = new ArrayList<>();

         for (Vec3 $$5 : this.attachments.get($$3)) {
            $$4.add($$5.multiply($$0, $$1, $$2));
         }

         return $$4;
      }));
   }

   
   public Vec3 getNullable(net.minecraft.world.entity.EntityAttachment $$0, int $$1, float $$2) {
      List<Vec3> $$3 = this.attachments.get($$0);
      return $$1 >= 0 && $$1 < $$3.size() ? transformPoint($$3.get($$1), $$2) : null;
   }

   public Vec3 get(net.minecraft.world.entity.EntityAttachment $$0, int $$1, float $$2) {
      Vec3 $$3 = this.getNullable($$0, $$1, $$2);
      if ($$3 == null) {
         throw new IllegalStateException("Had no attachment point of type: " + $$0 + " for index: " + $$1);
      } else {
         return $$3;
      }
   }

   public Vec3 getAverage(net.minecraft.world.entity.EntityAttachment $$0) {
      List<Vec3> $$1 = this.attachments.get($$0);
      if ($$1 != null && !$$1.isEmpty()) {
         Vec3 $$2 = Vec3.ZERO;

         for (Vec3 $$3 : $$1) {
            $$2 = $$2.add($$3);
         }

         return $$2.scale(1.0F / $$1.size());
      } else {
         throw new IllegalStateException("No attachment points of type: PASSENGER");
      }
   }

   public Vec3 getClamped(net.minecraft.world.entity.EntityAttachment $$0, int $$1, float $$2) {
      List<Vec3> $$3 = this.attachments.get($$0);
      if ($$3.isEmpty()) {
         throw new IllegalStateException("Had no attachment points of type: " + $$0);
      } else {
         Vec3 $$4 = $$3.get(Mth.clamp($$1, 0, $$3.size() - 1));
         return transformPoint($$4, $$2);
      }
   }

   private static Vec3 transformPoint(Vec3 $$0, float $$1) {
      return $$0.yRot(-$$1 * (float) (Math.PI / 180.0));
   }

   public static class Builder {
      private final Map<net.minecraft.world.entity.EntityAttachment, List<Vec3>> attachments = new EnumMap<>(net.minecraft.world.entity.EntityAttachment.class);

      Builder() {
      }

      public net.minecraft.world.entity.EntityAttachments.Builder attach(net.minecraft.world.entity.EntityAttachment $$0, float $$1, float $$2, float $$3) {
         return this.attach($$0, new Vec3($$1, $$2, $$3));
      }

      public net.minecraft.world.entity.EntityAttachments.Builder attach(net.minecraft.world.entity.EntityAttachment $$0, Vec3 $$1) {
         this.attachments.computeIfAbsent($$0, $$0x -> new ArrayList<>(1)).add($$1);
         return this;
      }

      public net.minecraft.world.entity.EntityAttachments build(float $$0, float $$1) {
         Map<net.minecraft.world.entity.EntityAttachment, List<Vec3>> $$2 = Util.makeEnumMap(net.minecraft.world.entity.EntityAttachment.class, $$2x -> {
            List<Vec3> $$3 = this.attachments.get($$2x);
            return $$3 == null ? $$2x.createFallbackPoints($$0, $$1) : List.copyOf($$3);
         });
         return new net.minecraft.world.entity.EntityAttachments($$2);
      }
   }
}
