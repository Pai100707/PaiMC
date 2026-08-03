package net.minecraft.world.level.portal;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

public record TeleportTransition(
   ServerLevel newLevel,
   Vec3 position,
   Vec3 deltaMovement,
   float yRot,
   float xRot,
   boolean missingRespawnBlock,
   boolean asPassenger,
   Set<Relative> relatives,
   TeleportTransition.PostTeleportTransition postTeleportTransition
) {
   public static final TeleportTransition.PostTeleportTransition DO_NOTHING = $$0 -> {};
   public static final TeleportTransition.PostTeleportTransition PLAY_PORTAL_SOUND = TeleportTransition::playPortalSound;
   public static final TeleportTransition.PostTeleportTransition PLACE_PORTAL_TICKET = TeleportTransition::placePortalTicket;

   public TeleportTransition(ServerLevel $$0, Vec3 $$1, Vec3 $$2, float $$3, float $$4, TeleportTransition.PostTeleportTransition $$5) {
      this($$0, $$1, $$2, $$3, $$4, Set.of(), $$5);
   }

   public TeleportTransition(ServerLevel $$0, Vec3 $$1, Vec3 $$2, float $$3, float $$4, Set<Relative> $$5, TeleportTransition.PostTeleportTransition $$6) {
      this($$0, $$1, $$2, $$3, $$4, false, false, $$5, $$6);
   }

   private static void playPortalSound(Entity $$0) {
      if ($$0 instanceof ServerPlayer $$1) {
         $$1.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
      }
   }

   private static void placePortalTicket(Entity $$0) {
      $$0.placePortalTicket(BlockPos.containing($$0.position()));
   }

   public static TeleportTransition createDefault(ServerPlayer $$0, TeleportTransition.PostTeleportTransition $$1) {
      ServerLevel $$2 = $$0.level().getServer().findRespawnDimension();
      LevelData.RespawnData $$3 = $$2.getRespawnData();
      return new TeleportTransition($$2, findAdjustedSharedSpawnPos($$2, $$0), Vec3.ZERO, $$3.yaw(), $$3.pitch(), false, false, Set.of(), $$1);
   }

   public static TeleportTransition missingRespawnBlock(ServerPlayer $$0, TeleportTransition.PostTeleportTransition $$1) {
      ServerLevel $$2 = $$0.level().getServer().findRespawnDimension();
      LevelData.RespawnData $$3 = $$2.getRespawnData();
      return new TeleportTransition($$2, findAdjustedSharedSpawnPos($$2, $$0), Vec3.ZERO, $$3.yaw(), $$3.pitch(), true, false, Set.of(), $$1);
   }

   private static Vec3 findAdjustedSharedSpawnPos(ServerLevel $$0, Entity $$1) {
      return $$1.adjustSpawnLocation($$0, $$0.getRespawnData().pos()).getBottomCenter();
   }

   public TeleportTransition withRotation(float $$0, float $$1) {
      return new TeleportTransition(
         this.newLevel(),
         this.position(),
         this.deltaMovement(),
         $$0,
         $$1,
         this.missingRespawnBlock(),
         this.asPassenger(),
         this.relatives(),
         this.postTeleportTransition()
      );
   }

   public TeleportTransition withPosition(Vec3 $$0) {
      return new TeleportTransition(
         this.newLevel(),
         $$0,
         this.deltaMovement(),
         this.yRot(),
         this.xRot(),
         this.missingRespawnBlock(),
         this.asPassenger(),
         this.relatives(),
         this.postTeleportTransition()
      );
   }

   public TeleportTransition transitionAsPassenger() {
      return new TeleportTransition(
         this.newLevel(),
         this.position(),
         this.deltaMovement(),
         this.yRot(),
         this.xRot(),
         this.missingRespawnBlock(),
         true,
         this.relatives(),
         this.postTeleportTransition()
      );
   }

   @FunctionalInterface
   public interface PostTeleportTransition {
      void onTransition(Entity var1);

      default TeleportTransition.PostTeleportTransition then(TeleportTransition.PostTeleportTransition $$0) {
         return $$1 -> {
            this.onTransition($$1);
            $$0.onTransition($$1);
         };
      }
   }
}
