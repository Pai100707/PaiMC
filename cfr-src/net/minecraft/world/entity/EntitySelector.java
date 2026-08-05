/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicates
 */
package net.minecraft.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public final class EntitySelector {
    public static final Predicate<Entity> ENTITY_STILL_ALIVE = Entity::isAlive;
    public static final Predicate<Entity> LIVING_ENTITY_STILL_ALIVE = $$0 -> $$0.isAlive() && $$0 instanceof LivingEntity;
    public static final Predicate<Entity> ENTITY_NOT_BEING_RIDDEN = $$0 -> $$0.isAlive() && !$$0.isVehicle() && !$$0.isPassenger();
    public static final Predicate<Entity> CONTAINER_ENTITY_SELECTOR = $$0 -> $$0 instanceof Container && $$0.isAlive();
    public static final Predicate<Entity> NO_CREATIVE_OR_SPECTATOR = $$0 -> {
        if (!($$0 instanceof Player)) return true;
        Player $$1 = (Player)$$0;
        if ($$0.isSpectator()) return false;
        if ($$1.isCreative()) return false;
        return true;
    };
    public static final Predicate<Entity> NO_SPECTATORS = $$0 -> !$$0.isSpectator();
    public static final Predicate<Entity> CAN_BE_COLLIDED_WITH = NO_SPECTATORS.and($$0 -> $$0.canBeCollidedWith(null));
    public static final Predicate<Entity> CAN_BE_PICKED = NO_SPECTATORS.and(Entity::isPickable);

    private EntitySelector() {
    }

    public static Predicate<Entity> withinDistance(double $$0, double $$1, double $$2, double $$3) {
        double $$42 = $$3 * $$3;
        return $$4 -> $$4.distanceToSqr($$0, $$1, $$2) <= $$42;
    }

    public static Predicate<Entity> pushableBy(Entity $$0) {
        Team.CollisionRule $$2;
        PlayerTeam $$1 = $$0.getTeam();
        Team.CollisionRule collisionRule = $$2 = $$1 == null ? Team.CollisionRule.ALWAYS : ((Team)$$1).getCollisionRule();
        if ($$2 == Team.CollisionRule.NEVER) {
            return Predicates.alwaysFalse();
        }
        return NO_SPECTATORS.and($$3 -> {
            boolean $$7;
            Team.CollisionRule $$6;
            Player $$4;
            if (!$$3.isPushable()) {
                return false;
            }
            if (!(!$$0.level().isClientSide() || $$3 instanceof Player && ($$4 = (Player)$$3).isLocalPlayer())) {
                return false;
            }
            PlayerTeam $$5 = $$3.getTeam();
            Team.CollisionRule collisionRule = $$6 = $$5 == null ? Team.CollisionRule.ALWAYS : ((Team)$$5).getCollisionRule();
            if ($$6 == Team.CollisionRule.NEVER) {
                return false;
            }
            boolean bl = $$7 = $$1 != null && $$1.isAlliedTo($$5);
            if (($$2 == Team.CollisionRule.PUSH_OWN_TEAM || $$6 == Team.CollisionRule.PUSH_OWN_TEAM) && $$7) {
                return false;
            }
            return $$2 != Team.CollisionRule.PUSH_OTHER_TEAMS && $$6 != Team.CollisionRule.PUSH_OTHER_TEAMS || $$7;
        });
    }

    public static Predicate<Entity> notRiding(Entity $$0) {
        return $$1 -> {
            while ($$1.isPassenger()) {
                if (($$1 = $$1.getVehicle()) != $$0) continue;
                return false;
            }
            return true;
        };
    }
}

