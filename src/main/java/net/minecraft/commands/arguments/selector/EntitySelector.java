package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntitySelector {
   public static final int INFINITE = Integer.MAX_VALUE;
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_ARBITRARY = ($$0, $$1) -> {};
   private static final EntityTypeTest<Entity, ?> ANY_TYPE = new EntityTypeTest<Entity, Entity>() {
      public Entity tryCast(Entity $$0) {
         return $$0;
      }

      public Class<? extends Entity> getBaseClass() {
         return Entity.class;
      }
   };
   private final int maxResults;
   private final boolean includesEntities;
   private final boolean worldLimited;
   private final List<Predicate<Entity>> contextFreePredicates;
   @Nullable
   private final Doubles range;
   private final Function<Vec3, Vec3> position;
   @Nullable
   private final AABB aabb;
   private final BiConsumer<Vec3, List<? extends Entity>> order;
   private final boolean currentEntity;
   @Nullable
   private final String playerName;
   @Nullable
   private final UUID entityUUID;
   private final EntityTypeTest<Entity, ?> type;
   private final boolean usesSelector;

   public EntitySelector(
      int $$0,
      boolean $$1,
      boolean $$2,
      List<Predicate<Entity>> $$3,
      @Nullable Doubles $$4,
      Function<Vec3, Vec3> $$5,
      @Nullable AABB $$6,
      BiConsumer<Vec3, List<? extends Entity>> $$7,
      boolean $$8,
      @Nullable String $$9,
      @Nullable UUID $$10,
      @Nullable EntityType<?> $$11,
      boolean $$12
   ) {
      this.maxResults = $$0;
      this.includesEntities = $$1;
      this.worldLimited = $$2;
      this.contextFreePredicates = $$3;
      this.range = $$4;
      this.position = $$5;
      this.aabb = $$6;
      this.order = $$7;
      this.currentEntity = $$8;
      this.playerName = $$9;
      this.entityUUID = $$10;
      this.type = (EntityTypeTest<Entity, ?>)($$11 == null ? ANY_TYPE : $$11);
      this.usesSelector = $$12;
   }

   public int getMaxResults() {
      return this.maxResults;
   }

   public boolean includesEntities() {
      return this.includesEntities;
   }

   public boolean isSelfSelector() {
      return this.currentEntity;
   }

   public boolean isWorldLimited() {
      return this.worldLimited;
   }

   public boolean usesSelector() {
      return this.usesSelector;
   }

   private void checkPermissions(net.minecraft.commands.CommandSourceStack $$0) throws CommandSyntaxException {
      if (this.usesSelector && !$$0.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS)) {
         throw EntityArgument.ERROR_SELECTORS_NOT_ALLOWED.create();
      }
   }

   public Entity findSingleEntity(net.minecraft.commands.CommandSourceStack $$0) throws CommandSyntaxException {
      this.checkPermissions($$0);
      List<? extends Entity> $$1 = this.findEntities($$0);
      if ($$1.isEmpty()) {
         throw EntityArgument.NO_ENTITIES_FOUND.create();
      } else if ($$1.size() > 1) {
         throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
      } else {
         return $$1.get(0);
      }
   }

   public List<? extends Entity> findEntities(net.minecraft.commands.CommandSourceStack $$0) throws CommandSyntaxException {
      this.checkPermissions($$0);
      if (!this.includesEntities) {
         return this.findPlayers($$0);
      } else if (this.playerName != null) {
         ServerPlayer $$1 = $$0.getServer().getPlayerList().getPlayerByName(this.playerName);
         return $$1 == null ? List.of() : List.of($$1);
      } else if (this.entityUUID != null) {
         for (ServerLevel $$2 : $$0.getServer().getAllLevels()) {
            Entity $$3 = $$2.getEntity(this.entityUUID);
            if ($$3 != null) {
               if ($$3.getType().isEnabled($$0.enabledFeatures())) {
                  return List.of($$3);
               }
               break;
            }
         }

         return List.of();
      } else {
         Vec3 $$4 = this.position.apply($$0.getPosition());
         AABB $$5 = this.getAbsoluteAabb($$4);
         if (this.currentEntity) {
            Predicate<Entity> $$6 = this.getPredicate($$4, $$5, null);
            return $$0.getEntity() != null && $$6.test($$0.getEntity()) ? List.of($$0.getEntity()) : List.of();
         } else {
            Predicate<Entity> $$7 = this.getPredicate($$4, $$5, $$0.enabledFeatures());
            List<Entity> $$8 = new ObjectArrayList();
            if (this.isWorldLimited()) {
               this.addEntities($$8, $$0.getLevel(), $$5, $$7);
            } else {
               for (ServerLevel $$9 : $$0.getServer().getAllLevels()) {
                  this.addEntities($$8, $$9, $$5, $$7);
               }
            }

            return this.sortAndLimit($$4, $$8);
         }
      }
   }

   private void addEntities(List<Entity> $$0, ServerLevel $$1, @Nullable AABB $$2, Predicate<Entity> $$3) {
      int $$4 = this.getResultLimit();
      if ($$0.size() < $$4) {
         if ($$2 != null) {
            $$1.getEntities(this.type, $$2, $$3, $$0, $$4);
         } else {
            $$1.getEntities(this.type, $$3, $$0, $$4);
         }
      }
   }

   private int getResultLimit() {
      return this.order == ORDER_ARBITRARY ? this.maxResults : Integer.MAX_VALUE;
   }

   public ServerPlayer findSinglePlayer(net.minecraft.commands.CommandSourceStack $$0) throws CommandSyntaxException {
      this.checkPermissions($$0);
      List<ServerPlayer> $$1 = this.findPlayers($$0);
      if ($$1.size() != 1) {
         throw EntityArgument.NO_PLAYERS_FOUND.create();
      } else {
         return $$1.get(0);
      }
   }

   public List<ServerPlayer> findPlayers(net.minecraft.commands.CommandSourceStack $$0) throws CommandSyntaxException {
      this.checkPermissions($$0);
      if (this.playerName != null) {
         ServerPlayer $$1 = $$0.getServer().getPlayerList().getPlayerByName(this.playerName);
         return $$1 == null ? List.of() : List.of($$1);
      } else if (this.entityUUID != null) {
         ServerPlayer $$2 = $$0.getServer().getPlayerList().getPlayer(this.entityUUID);
         return $$2 == null ? List.of() : List.of($$2);
      } else {
         Vec3 $$3 = this.position.apply($$0.getPosition());
         AABB $$4 = this.getAbsoluteAabb($$3);
         Predicate<Entity> $$5 = this.getPredicate($$3, $$4, null);
         if (this.currentEntity) {
            return $$0.getEntity() instanceof ServerPlayer $$6 && $$5.test($$6) ? List.of($$6) : List.of();
         } else {
            int $$7 = this.getResultLimit();
            List<ServerPlayer> $$8;
            if (this.isWorldLimited()) {
               $$8 = $$0.getLevel().getPlayers($$5, $$7);
            } else {
               $$8 = new ObjectArrayList();

               for (ServerPlayer $$10 : $$0.getServer().getPlayerList().getPlayers()) {
                  if ($$5.test($$10)) {
                     $$8.add($$10);
                     if ($$8.size() >= $$7) {
                        return $$8;
                     }
                  }
               }
            }

            return this.sortAndLimit($$3, $$8);
         }
      }
   }

   @Nullable
   private AABB getAbsoluteAabb(Vec3 $$0) {
      return this.aabb != null ? this.aabb.move($$0) : null;
   }

   private Predicate<Entity> getPredicate(Vec3 $$0, @Nullable AABB $$1, @Nullable FeatureFlagSet $$2) {
      boolean $$3 = $$2 != null;
      boolean $$4 = $$1 != null;
      boolean $$5 = this.range != null;
      int $$6 = ($$3 ? 1 : 0) + ($$4 ? 1 : 0) + ($$5 ? 1 : 0);
      List<Predicate<Entity>> $$7;
      if ($$6 == 0) {
         $$7 = this.contextFreePredicates;
      } else {
         List<Predicate<Entity>> $$8 = new ObjectArrayList(this.contextFreePredicates.size() + $$6);
         $$8.addAll(this.contextFreePredicates);
         if ($$3) {
            $$8.add($$1x -> $$1x.getType().isEnabled($$2));
         }

         if ($$4) {
            $$8.add($$1x -> $$1.intersects($$1x.getBoundingBox()));
         }

         if ($$5) {
            $$8.add($$1x -> this.range.matchesSqr($$1x.distanceToSqr($$0)));
         }

         $$7 = $$8;
      }

      return Util.allOf($$7);
   }

   private <T extends Entity> List<T> sortAndLimit(Vec3 $$0, List<T> $$1) {
      if ($$1.size() > 1) {
         this.order.accept($$0, $$1);
      }

      return $$1.subList(0, Math.min(this.maxResults, $$1.size()));
   }

   public static Component joinNames(List<? extends Entity> $$0) {
      return ComponentUtils.formatList($$0, Entity::getDisplayName);
   }
}
