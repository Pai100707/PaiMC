package net.minecraft.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.Team.CollisionRule;

public final class EntitySelector {
   public static final Predicate<net.minecraft.world.entity.Entity> ENTITY_STILL_ALIVE = net.minecraft.world.entity.Entity::isAlive;
   public static final Predicate<net.minecraft.world.entity.Entity> LIVING_ENTITY_STILL_ALIVE = $$0 -> $$0.isAlive()
      && $$0 instanceof net.minecraft.world.entity.LivingEntity;
   public static final Predicate<net.minecraft.world.entity.Entity> ENTITY_NOT_BEING_RIDDEN = $$0 -> $$0.isAlive() && !$$0.isVehicle() && !$$0.isPassenger();
   public static final Predicate<net.minecraft.world.entity.Entity> CONTAINER_ENTITY_SELECTOR = $$0 -> $$0 instanceof Container && $$0.isAlive();
   public static final Predicate<net.minecraft.world.entity.Entity> NO_CREATIVE_OR_SPECTATOR = $$0 -> !(
      $$0 instanceof Player $$1 && ($$0.isSpectator() || $$1.isCreative())
   );
   public static final Predicate<net.minecraft.world.entity.Entity> NO_SPECTATORS = $$0 -> !$$0.isSpectator();
   public static final Predicate<net.minecraft.world.entity.Entity> CAN_BE_COLLIDED_WITH = NO_SPECTATORS.and($$0 -> $$0.canBeCollidedWith(null));
   public static final Predicate<net.minecraft.world.entity.Entity> CAN_BE_PICKED = NO_SPECTATORS.and(net.minecraft.world.entity.Entity::isPickable);

   private EntitySelector() {
   }

   public static Predicate<net.minecraft.world.entity.Entity> withinDistance(double $$0, double $$1, double $$2, double $$3) {
      double $$4 = $$3 * $$3;
      return $$4x -> $$4x.distanceToSqr($$0, $$1, $$2) <= $$4;
   }

   public static Predicate<net.minecraft.world.entity.Entity> pushableBy(net.minecraft.world.entity.Entity $$0) {
      Team $$1 = $$0.getTeam();
      CollisionRule $$2 = $$1 == null ? CollisionRule.ALWAYS : $$1.getCollisionRule();
      return (Predicate<net.minecraft.world.entity.Entity>)($$2 == CollisionRule.NEVER
         ? Predicates.alwaysFalse()
         : NO_SPECTATORS.and(
            $$3 -> {
               if (!$$3.isPushable()) {
                  return false;
               } else if (!$$0.level().isClientSide() || $$3 instanceof Player $$4 && $$4.isLocalPlayer()) {
                  Team $$5 = $$3.getTeam();
                  CollisionRule $$6 = $$5 == null ? CollisionRule.ALWAYS : $$5.getCollisionRule();
                  if ($$6 == CollisionRule.NEVER) {
                     return false;
                  } else {
                     boolean $$7 = $$1 != null && $$1.isAlliedTo($$5);
                     return ($$2 == CollisionRule.PUSH_OWN_TEAM || $$6 == CollisionRule.PUSH_OWN_TEAM) && $$7
                        ? false
                        : $$2 != CollisionRule.PUSH_OTHER_TEAMS && $$6 != CollisionRule.PUSH_OTHER_TEAMS || $$7;
                  }
               } else {
                  return false;
               }
            }
         ));
   }

   public static Predicate<net.minecraft.world.entity.Entity> notRiding(net.minecraft.world.entity.Entity $$0) {
      return $$1 -> {
         while ($$1.isPassenger()) {
            $$1 = $$1.getVehicle();
            if ($$1 == $$0) {
               return false;
            }
         }

         return true;
      };
   }
}
