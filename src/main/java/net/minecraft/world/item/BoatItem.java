package net.minecraft.world.item;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public class BoatItem extends net.minecraft.world.item.Item {
   private final EntityType<? extends AbstractBoat> entityType;

   public BoatItem(EntityType<? extends AbstractBoat> $$0, net.minecraft.world.item.Item.Properties $$1) {
      super($$1);
      this.entityType = $$0;
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      HitResult $$4 = getPlayerPOVHitResult($$0, $$1, Fluid.ANY);
      if ($$4.getType() == Type.MISS) {
         return InteractionResult.PASS;
      } else {
         Vec3 $$5 = $$1.getViewVector(1.0F);
         double $$6 = 5.0;
         List<Entity> $$7 = $$0.getEntities($$1, $$1.getBoundingBox().expandTowards($$5.scale(5.0)).inflate(1.0), EntitySelector.CAN_BE_PICKED);
         if (!$$7.isEmpty()) {
            Vec3 $$8 = $$1.getEyePosition();

            for (Entity $$9 : $$7) {
               AABB $$10 = $$9.getBoundingBox().inflate($$9.getPickRadius());
               if ($$10.contains($$8)) {
                  return InteractionResult.PASS;
               }
            }
         }

         if ($$4.getType() == Type.BLOCK) {
            AbstractBoat $$11 = this.getBoat($$0, $$4, $$3, $$1);
            if ($$11 == null) {
               return InteractionResult.FAIL;
            } else {
               $$11.setYRot($$1.getYRot());
               if (!$$0.noCollision($$11, $$11.getBoundingBox())) {
                  return InteractionResult.FAIL;
               } else {
                  if (!$$0.isClientSide()) {
                     $$0.addFreshEntity($$11);
                     $$0.gameEvent($$1, GameEvent.ENTITY_PLACE, $$4.getLocation());
                     $$3.consume(1, $$1);
                  }

                  $$1.awardStat(Stats.ITEM_USED.get(this));
                  return InteractionResult.SUCCESS;
               }
            }
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   @Nullable
   private AbstractBoat getBoat(Level $$0, HitResult $$1, net.minecraft.world.item.ItemStack $$2, Player $$3) {
      AbstractBoat $$4 = (AbstractBoat)this.entityType.create($$0, EntitySpawnReason.SPAWN_ITEM_USE);
      if ($$4 != null) {
         Vec3 $$5 = $$1.getLocation();
         $$4.setInitialPos($$5.x, $$5.y, $$5.z);
         if ($$0 instanceof ServerLevel $$6) {
            EntityType.createDefaultStackConfig($$6, $$2, $$3).accept($$4);
         }
      }

      return $$4;
   }
}
