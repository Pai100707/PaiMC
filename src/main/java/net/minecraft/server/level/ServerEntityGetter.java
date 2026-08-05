package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;

public interface ServerEntityGetter extends EntityGetter {
   ServerLevel getLevel();

   
   default Player getNearestPlayer(TargetingConditions $$0, LivingEntity $$1) {
      return this.getNearestEntity(this.players(), $$0, $$1, $$1.getX(), $$1.getY(), $$1.getZ());
   }

   
   default Player getNearestPlayer(TargetingConditions $$0, LivingEntity $$1, double $$2, double $$3, double $$4) {
      return this.getNearestEntity(this.players(), $$0, $$1, $$2, $$3, $$4);
   }

   
   default Player getNearestPlayer(TargetingConditions $$0, double $$1, double $$2, double $$3) {
      return this.getNearestEntity(this.players(), $$0, null, $$1, $$2, $$3);
   }

   
   default <T extends LivingEntity> T getNearestEntity(
      Class<? extends T> $$0, TargetingConditions $$1, LivingEntity $$2, double $$3, double $$4, double $$5, AABB $$6
   ) {
      return this.getNearestEntity(this.getEntitiesOfClass($$0, $$6, $$0x -> true), $$1, $$2, $$3, $$4, $$5);
   }

   
   default LivingEntity getNearestEntity(
      TagKey<EntityType<?>> $$0, TargetingConditions $$1, LivingEntity $$2, double $$3, double $$4, double $$5, AABB $$6
   ) {
      double $$7 = Double.MAX_VALUE;
      LivingEntity $$8 = null;

      for (LivingEntity $$9 : this.getEntitiesOfClass(LivingEntity.class, $$6, $$1x -> $$1x.getType().is($$0))) {
         if ($$1.test(this.getLevel(), $$2, $$9)) {
            double $$10 = $$9.distanceToSqr($$3, $$4, $$5);
            if ($$10 < $$7) {
               $$7 = $$10;
               $$8 = $$9;
            }
         }
      }

      return $$8;
   }

   
   default <T extends LivingEntity> T getNearestEntity(
      List<? extends T> $$0, TargetingConditions $$1, LivingEntity $$2, double $$3, double $$4, double $$5
   ) {
      double $$6 = -1.0;
      T $$7 = null;

      for (T $$8 : $$0) {
         if ($$1.test(this.getLevel(), $$2, $$8)) {
            double $$9 = $$8.distanceToSqr($$3, $$4, $$5);
            if ($$6 == -1.0 || $$9 < $$6) {
               $$6 = $$9;
               $$7 = $$8;
            }
         }
      }

      return $$7;
   }

   default List<Player> getNearbyPlayers(TargetingConditions $$0, LivingEntity $$1, AABB $$2) {
      List<Player> $$3 = new ArrayList<>();

      for (Player $$4 : this.players()) {
         if ($$2.contains($$4.getX(), $$4.getY(), $$4.getZ()) && $$0.test(this.getLevel(), $$1, $$4)) {
            $$3.add($$4);
         }
      }

      return $$3;
   }

   default <T extends LivingEntity> List<T> getNearbyEntities(Class<T> $$0, TargetingConditions $$1, LivingEntity $$2, AABB $$3) {
      List<T> $$4 = this.getEntitiesOfClass($$0, $$3, $$0x -> true);
      List<T> $$5 = new ArrayList<>();

      for (T $$6 : $$4) {
         if ($$1.test(this.getLevel(), $$2, $$6)) {
            $$5.add($$6);
         }
      }

      return $$5;
   }
}
