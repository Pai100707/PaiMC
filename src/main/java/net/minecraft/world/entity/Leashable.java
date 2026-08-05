package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface Leashable {
   String LEASH_TAG = "leash";
   double LEASH_TOO_FAR_DIST = 12.0;
   double LEASH_ELASTIC_DIST = 6.0;
   double MAXIMUM_ALLOWED_LEASHED_DIST = 16.0;
   Vec3 AXIS_SPECIFIC_ELASTICITY = new Vec3(0.8, 0.2, 0.8);
   float SPRING_DAMPENING = 0.7F;
   double TORSIONAL_ELASTICITY = 10.0;
   double STIFFNESS = 0.11;
   List<Vec3> ENTITY_ATTACHMENT_POINT = ImmutableList.of(new Vec3(0.0, 0.5, 0.5));
   List<Vec3> LEASHER_ATTACHMENT_POINT = ImmutableList.of(new Vec3(0.0, 0.5, 0.0));
   List<Vec3> SHARED_QUAD_ATTACHMENT_POINTS = ImmutableList.of(
      new Vec3(-0.5, 0.5, 0.5), new Vec3(-0.5, 0.5, -0.5), new Vec3(0.5, 0.5, -0.5), new Vec3(0.5, 0.5, 0.5)
   );

   
   net.minecraft.world.entity.Leashable.LeashData getLeashData();

   void setLeashData(net.minecraft.world.entity.Leashable.LeashData var1);

   default boolean isLeashed() {
      return this.getLeashData() != null && this.getLeashData().leashHolder != null;
   }

   default boolean mayBeLeashed() {
      return this.getLeashData() != null;
   }

   default boolean canHaveALeashAttachedTo(net.minecraft.world.entity.Entity $$0) {
      if (this == $$0) {
         return false;
      } else {
         return this.leashDistanceTo($$0) > this.leashSnapDistance() ? false : this.canBeLeashed();
      }
   }

   default double leashDistanceTo(net.minecraft.world.entity.Entity $$0) {
      return $$0.getBoundingBox().getCenter().distanceTo(((net.minecraft.world.entity.Entity)this).getBoundingBox().getCenter());
   }

   default boolean canBeLeashed() {
      return true;
   }

   default void setDelayedLeashHolderId(int $$0) {
      this.setLeashData(new net.minecraft.world.entity.Leashable.LeashData($$0));
      dropLeash((net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable)this, false, false);
   }

   default void readLeashData(ValueInput $$0) {
      net.minecraft.world.entity.Leashable.LeashData $$1 = (net.minecraft.world.entity.Leashable.LeashData)$$0.read(
            "leash", net.minecraft.world.entity.Leashable.LeashData.CODEC
         )
         .orElse(null);
      if (this.getLeashData() != null && $$1 == null) {
         this.removeLeash();
      }

      this.setLeashData($$1);
   }

   default void writeLeashData(ValueOutput $$0, net.minecraft.world.entity.Leashable.LeashData $$1) {
      $$0.storeNullable("leash", net.minecraft.world.entity.Leashable.LeashData.CODEC, $$1);
   }

   private static <E extends net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable> void restoreLeashFromSave(
      E $$0, net.minecraft.world.entity.Leashable.LeashData $$1
   ) {
      if ($$1.delayedLeashInfo != null && $$0.level() instanceof ServerLevel $$2) {
         Optional<UUID> $$3 = $$1.delayedLeashInfo.left();
         Optional<BlockPos> $$4 = $$1.delayedLeashInfo.right();
         if ($$3.isPresent()) {
            net.minecraft.world.entity.Entity $$5 = $$2.getEntity($$3.get());
            if ($$5 != null) {
               setLeashedTo($$0, $$5, true);
               return;
            }
         } else if ($$4.isPresent()) {
            setLeashedTo($$0, LeashFenceKnotEntity.getOrCreateKnot($$2, $$4.get()), true);
            return;
         }

         if ($$0.tickCount > 100) {
            $$0.spawnAtLocation($$2, Items.LEAD);
            $$0.setLeashData(null);
         }
      }
   }

   default void dropLeash() {
      dropLeash((net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable)this, true, true);
   }

   default void removeLeash() {
      dropLeash((net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable)this, true, false);
   }

   default void onLeashRemoved() {
   }

   private static <E extends net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable> void dropLeash(E $$0, boolean $$1, boolean $$2) {
      net.minecraft.world.entity.Leashable.LeashData $$3 = $$0.getLeashData();
      if ($$3 != null && $$3.leashHolder != null) {
         $$0.setLeashData(null);
         $$0.onLeashRemoved();
         if ($$0.level() instanceof ServerLevel $$4) {
            if ($$2) {
               $$0.spawnAtLocation($$4, Items.LEAD);
            }

            if ($$1) {
               $$4.getChunkSource().sendToTrackingPlayers($$0, new ClientboundSetEntityLinkPacket($$0, null));
            }

            $$3.leashHolder.notifyLeasheeRemoved($$0);
         }
      }
   }

   static <E extends net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable> void tickLeash(ServerLevel $$0, E $$1) {
      net.minecraft.world.entity.Leashable.LeashData $$2 = $$1.getLeashData();
      if ($$2 != null && $$2.delayedLeashInfo != null) {
         restoreLeashFromSave($$1, $$2);
      }

      if ($$2 != null && $$2.leashHolder != null) {
         if (!$$1.canInteractWithLevel() || !$$2.leashHolder.canInteractWithLevel()) {
            if ((Boolean)$$0.getGameRules().get(GameRules.ENTITY_DROPS)) {
               $$1.dropLeash();
            } else {
               $$1.removeLeash();
            }
         }

         net.minecraft.world.entity.Entity $$3 = $$1.getLeashHolder();
         if ($$3 != null && $$3.level() == $$1.level()) {
            double $$4 = $$1.leashDistanceTo($$3);
            $$1.whenLeashedTo($$3);
            if ($$4 > $$1.leashSnapDistance()) {
               $$0.playSound(null, $$3.getX(), $$3.getY(), $$3.getZ(), SoundEvents.LEAD_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
               $$1.leashTooFarBehaviour();
            } else if ($$4 > $$1.leashElasticDistance() - $$3.getBbWidth() - $$1.getBbWidth() && $$1.checkElasticInteractions($$3, $$2)) {
               $$1.onElasticLeashPull();
            } else {
               $$1.closeRangeLeashBehaviour($$3);
            }

            $$1.setYRot((float)($$1.getYRot() - $$2.angularMomentum));
            $$2.angularMomentum = $$2.angularMomentum * angularFriction($$1);
         }
      }
   }

   default void onElasticLeashPull() {
      net.minecraft.world.entity.Entity $$0 = (net.minecraft.world.entity.Entity)this;
      $$0.checkFallDistanceAccumulation();
   }

   default double leashSnapDistance() {
      return 12.0;
   }

   default double leashElasticDistance() {
      return 6.0;
   }

   static <E extends net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable> float angularFriction(E $$0) {
      if ($$0.onGround()) {
         return $$0.level().getBlockState($$0.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.91F;
      } else {
         return $$0.isInLiquid() ? 0.8F : 0.91F;
      }
   }

   default void whenLeashedTo(net.minecraft.world.entity.Entity $$0) {
      $$0.notifyLeashHolder(this);
   }

   default void leashTooFarBehaviour() {
      this.dropLeash();
   }

   default void closeRangeLeashBehaviour(net.minecraft.world.entity.Entity $$0) {
   }

   default boolean checkElasticInteractions(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.Leashable.LeashData $$1) {
      boolean $$2 = $$0.supportQuadLeashAsHolder() && this.supportQuadLeash();
      List<net.minecraft.world.entity.Leashable.Wrench> $$3 = computeElasticInteraction(
         (net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable)this,
         $$0,
         $$2 ? SHARED_QUAD_ATTACHMENT_POINTS : ENTITY_ATTACHMENT_POINT,
         $$2 ? SHARED_QUAD_ATTACHMENT_POINTS : LEASHER_ATTACHMENT_POINT
      );
      if ($$3.isEmpty()) {
         return false;
      } else {
         net.minecraft.world.entity.Leashable.Wrench $$4 = net.minecraft.world.entity.Leashable.Wrench.accumulate($$3).scale($$2 ? 0.25 : 1.0);
         $$1.angularMomentum = $$1.angularMomentum + 10.0 * $$4.torque();
         Vec3 $$5 = getHolderMovement($$0).subtract(((net.minecraft.world.entity.Entity)this).getKnownMovement());
         ((net.minecraft.world.entity.Entity)this).addDeltaMovement($$4.force().multiply(AXIS_SPECIFIC_ELASTICITY).add($$5.scale(0.11)));
         return true;
      }
   }

   private static Vec3 getHolderMovement(net.minecraft.world.entity.Entity $$0) {
      return $$0 instanceof net.minecraft.world.entity.Mob $$1 && $$1.isNoAi() ? Vec3.ZERO : $$0.getKnownMovement();
   }

   private static <E extends net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable> List<net.minecraft.world.entity.Leashable.Wrench> computeElasticInteraction(
      E $$0, net.minecraft.world.entity.Entity $$1, List<Vec3> $$2, List<Vec3> $$3
   ) {
      double $$4 = $$0.leashElasticDistance();
      Vec3 $$5 = getHolderMovement($$0);
      float $$6 = $$0.getYRot() * (float) (Math.PI / 180.0);
      Vec3 $$7 = new Vec3($$0.getBbWidth(), $$0.getBbHeight(), $$0.getBbWidth());
      float $$8 = $$1.getYRot() * (float) (Math.PI / 180.0);
      Vec3 $$9 = new Vec3($$1.getBbWidth(), $$1.getBbHeight(), $$1.getBbWidth());
      List<net.minecraft.world.entity.Leashable.Wrench> $$10 = new ArrayList<>();

      for (int $$11 = 0; $$11 < $$2.size(); $$11++) {
         Vec3 $$12 = $$2.get($$11).multiply($$7).yRot(-$$6);
         Vec3 $$13 = $$0.position().add($$12);
         Vec3 $$14 = $$3.get($$11).multiply($$9).yRot(-$$8);
         Vec3 $$15 = $$1.position().add($$14);
         computeDampenedSpringInteraction($$15, $$13, $$4, $$5, $$12).ifPresent($$10::add);
      }

      return $$10;
   }

   private static Optional<net.minecraft.world.entity.Leashable.Wrench> computeDampenedSpringInteraction(Vec3 $$0, Vec3 $$1, double $$2, Vec3 $$3, Vec3 $$4) {
      double $$5 = $$1.distanceTo($$0);
      if ($$5 < $$2) {
         return Optional.empty();
      } else {
         Vec3 $$6 = $$0.subtract($$1).normalize().scale($$5 - $$2);
         double $$7 = net.minecraft.world.entity.Leashable.Wrench.torqueFromForce($$4, $$6);
         boolean $$8 = $$3.dot($$6) >= 0.0;
         if ($$8) {
            $$6 = $$6.scale(0.3F);
         }

         return Optional.of(new net.minecraft.world.entity.Leashable.Wrench($$6, $$7));
      }
   }

   default boolean supportQuadLeash() {
      return false;
   }

   default Vec3[] getQuadLeashOffsets() {
      return createQuadLeashOffsets((net.minecraft.world.entity.Entity)this, 0.0, 0.5, 0.5, 0.5);
   }

   static Vec3[] createQuadLeashOffsets(net.minecraft.world.entity.Entity $$0, double $$1, double $$2, double $$3, double $$4) {
      float $$5 = $$0.getBbWidth();
      double $$6 = $$1 * $$5;
      double $$7 = $$2 * $$5;
      double $$8 = $$3 * $$5;
      double $$9 = $$4 * $$0.getBbHeight();
      return new Vec3[]{new Vec3(-$$8, $$9, $$7 + $$6), new Vec3(-$$8, $$9, -$$7 + $$6), new Vec3($$8, $$9, -$$7 + $$6), new Vec3($$8, $$9, $$7 + $$6)};
   }

   default Vec3 getLeashOffset(float $$0) {
      return this.getLeashOffset();
   }

   default Vec3 getLeashOffset() {
      net.minecraft.world.entity.Entity $$0 = (net.minecraft.world.entity.Entity)this;
      return new Vec3(0.0, $$0.getEyeHeight(), $$0.getBbWidth() * 0.4F);
   }

   default void setLeashedTo(net.minecraft.world.entity.Entity $$0, boolean $$1) {
      if (this != $$0) {
         setLeashedTo((net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable)this, $$0, $$1);
      }
   }

   private static <E extends net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable> void setLeashedTo(
      E $$0, net.minecraft.world.entity.Entity $$1, boolean $$2
   ) {
      net.minecraft.world.entity.Leashable.LeashData $$3 = $$0.getLeashData();
      if ($$3 == null) {
         $$3 = new net.minecraft.world.entity.Leashable.LeashData($$1);
         $$0.setLeashData($$3);
      } else {
         net.minecraft.world.entity.Entity $$4 = $$3.leashHolder;
         $$3.setLeashHolder($$1);
         if ($$4 != null && $$4 != $$1) {
            $$4.notifyLeasheeRemoved($$0);
         }
      }

      if ($$2 && $$0.level() instanceof ServerLevel $$5) {
         $$5.getChunkSource().sendToTrackingPlayers($$0, new ClientboundSetEntityLinkPacket($$0, $$1));
      }

      if ($$0.isPassenger()) {
         $$0.stopRiding();
      }
   }

   
   default net.minecraft.world.entity.Entity getLeashHolder() {
      return getLeashHolder((net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable)this);
   }

   
   private static <E extends net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable> net.minecraft.world.entity.Entity getLeashHolder(E $$0) {
      net.minecraft.world.entity.Leashable.LeashData $$1 = $$0.getLeashData();
      if ($$1 == null) {
         return null;
      } else {
         if ($$1.delayedLeashHolderId != 0 && $$0.level().isClientSide()) {
            net.minecraft.world.entity.Entity var3 = $$0.level().getEntity($$1.delayedLeashHolderId);
            if (var3 instanceof net.minecraft.world.entity.Entity) {
               $$1.setLeashHolder(var3);
            }
         }

         return $$1.leashHolder;
      }
   }

   static List<net.minecraft.world.entity.Leashable> leashableLeashedTo(net.minecraft.world.entity.Entity $$0) {
      return leashableInArea($$0, $$1 -> $$1.getLeashHolder() == $$0);
   }

   static List<net.minecraft.world.entity.Leashable> leashableInArea(net.minecraft.world.entity.Entity $$0, Predicate<net.minecraft.world.entity.Leashable> $$1) {
      return leashableInArea($$0.level(), $$0.getBoundingBox().getCenter(), $$1);
   }

   static List<net.minecraft.world.entity.Leashable> leashableInArea(Level $$0, Vec3 $$1, Predicate<net.minecraft.world.entity.Leashable> $$2) {
      double $$3 = 32.0;
      AABB $$4 = AABB.ofSize($$1, 32.0, 32.0, 32.0);
      return $$0.getEntitiesOfClass(
            net.minecraft.world.entity.Entity.class, $$4, $$1x -> $$1x instanceof net.minecraft.world.entity.Leashable $$2x && $$2.test($$2x)
         )
         .stream()
         .map(net.minecraft.world.entity.Leashable.class::cast)
         .toList();
   }

   public static final class LeashData {
      public static final Codec<net.minecraft.world.entity.Leashable.LeashData> CODEC = Codec.xor(UUIDUtil.CODEC.fieldOf("UUID").codec(), BlockPos.CODEC)
         .xmap(
            net.minecraft.world.entity.Leashable.LeashData::new,
            $$0 -> {
               if ($$0.leashHolder instanceof LeashFenceKnotEntity $$2) {
                  return Either.right($$2.getPos());
               } else {
                  return $$0.leashHolder != null
                     ? Either.left($$0.leashHolder.getUUID())
                     : Objects.requireNonNull($$0.delayedLeashInfo, "Invalid LeashData had no attachment");
               }
            }
         );
      int delayedLeashHolderId;
      
      public net.minecraft.world.entity.Entity leashHolder;
      
      public Either<UUID, BlockPos> delayedLeashInfo;
      public double angularMomentum;

      private LeashData(Either<UUID, BlockPos> $$0) {
         this.delayedLeashInfo = $$0;
      }

      LeashData(net.minecraft.world.entity.Entity $$0) {
         this.leashHolder = $$0;
      }

      LeashData(int $$0) {
         this.delayedLeashHolderId = $$0;
      }

      public void setLeashHolder(net.minecraft.world.entity.Entity $$0) {
         this.leashHolder = $$0;
         this.delayedLeashInfo = null;
         this.delayedLeashHolderId = 0;
      }
   }

   public record Wrench(Vec3 force, double torque) {
      static net.minecraft.world.entity.Leashable.Wrench ZERO = new net.minecraft.world.entity.Leashable.Wrench(Vec3.ZERO, 0.0);

      static double torqueFromForce(Vec3 $$0, Vec3 $$1) {
         return $$0.z * $$1.x - $$0.x * $$1.z;
      }

      static net.minecraft.world.entity.Leashable.Wrench accumulate(List<net.minecraft.world.entity.Leashable.Wrench> $$0) {
         if ($$0.isEmpty()) {
            return ZERO;
         } else {
            double $$1 = 0.0;
            double $$2 = 0.0;
            double $$3 = 0.0;
            double $$4 = 0.0;

            for (net.minecraft.world.entity.Leashable.Wrench $$5 : $$0) {
               Vec3 $$6 = $$5.force;
               $$1 += $$6.x;
               $$2 += $$6.y;
               $$3 += $$6.z;
               $$4 += $$5.torque;
            }

            return new net.minecraft.world.entity.Leashable.Wrench(new Vec3($$1, $$2, $$3), $$4);
         }
      }

      public net.minecraft.world.entity.Leashable.Wrench scale(double $$0) {
         return new net.minecraft.world.entity.Leashable.Wrench(this.force.scale($$0), this.torque * $$0);
      }
   }
}
