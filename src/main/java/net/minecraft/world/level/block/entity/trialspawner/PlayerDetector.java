package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;

public interface PlayerDetector {
   PlayerDetector NO_CREATIVE_PLAYERS = ($$0, $$1, $$2, $$3, $$4) -> $$1.getPlayers(
         $$0, $$2x -> $$2x.blockPosition().closerThan($$2, $$3) && !$$2x.isCreative() && !$$2x.isSpectator()
      )
      .stream()
      .filter($$3x -> !$$4 || inLineOfSight($$0, $$2.getCenter(), $$3x.getEyePosition()))
      .<UUID>map(Entity::getUUID)
      .toList();
   PlayerDetector INCLUDING_CREATIVE_PLAYERS = ($$0, $$1, $$2, $$3, $$4) -> $$1.getPlayers(
         $$0, $$2x -> $$2x.blockPosition().closerThan($$2, $$3) && !$$2x.isSpectator()
      )
      .stream()
      .filter($$3x -> !$$4 || inLineOfSight($$0, $$2.getCenter(), $$3x.getEyePosition()))
      .<UUID>map(Entity::getUUID)
      .toList();
   PlayerDetector SHEEP = ($$0, $$1, $$2, $$3, $$4) -> {
      AABB $$5 = new AABB($$2).inflate($$3);
      return $$1.getEntities($$0, EntityType.SHEEP, $$5, LivingEntity::isAlive)
         .stream()
         .filter($$3x -> !$$4 || inLineOfSight($$0, $$2.getCenter(), $$3x.getEyePosition()))
         .<UUID>map(Entity::getUUID)
         .toList();
   };

   List<UUID> detect(ServerLevel var1, PlayerDetector.EntitySelector var2, BlockPos var3, double var4, boolean var6);

   private static boolean inLineOfSight(net.minecraft.world.level.Level $$0, Vec3 $$1, Vec3 $$2) {
      BlockHitResult $$3 = $$0.clip(
         new net.minecraft.world.level.ClipContext(
            $$2, $$1, net.minecraft.world.level.ClipContext.Block.VISUAL, net.minecraft.world.level.ClipContext.Fluid.NONE, CollisionContext.empty()
         )
      );
      return $$3.getBlockPos().equals(BlockPos.containing($$1)) || $$3.getType() == Type.MISS;
   }

   public interface EntitySelector {
      PlayerDetector.EntitySelector SELECT_FROM_LEVEL = new PlayerDetector.EntitySelector() {
         @Override
         public List<ServerPlayer> getPlayers(ServerLevel $$0, Predicate<? super Player> $$1) {
            return $$0.getPlayers($$1);
         }

         @Override
         public <T extends Entity> List<T> getEntities(ServerLevel $$0, EntityTypeTest<Entity, T> $$1, AABB $$2, Predicate<? super T> $$3) {
            return $$0.getEntities($$1, $$2, $$3);
         }
      };

      List<? extends Player> getPlayers(ServerLevel var1, Predicate<? super Player> var2);

      <T extends Entity> List<T> getEntities(ServerLevel var1, EntityTypeTest<Entity, T> var2, AABB var3, Predicate<? super T> var4);

      static PlayerDetector.EntitySelector onlySelectPlayer(Player $$0) {
         return onlySelectPlayers(List.of($$0));
      }

      static PlayerDetector.EntitySelector onlySelectPlayers(final List<Player> $$0) {
         return new PlayerDetector.EntitySelector() {
            @Override
            public List<Player> getPlayers(ServerLevel $$0x, Predicate<? super Player> $$1) {
               return $$0.stream().filter($$1).toList();
            }

            @Override
            public <T extends Entity> List<T> getEntities(ServerLevel $$0x, EntityTypeTest<Entity, T> $$1, AABB $$2, Predicate<? super T> $$3) {
               return $$0.stream().map($$1::tryCast).filter(Objects::nonNull).filter($$3).toList();
            }
         };
      }
   }
}
