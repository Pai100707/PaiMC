package net.minecraft.world.entity.decoration.painting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.util.Util;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Painting extends HangingEntity {
   private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(
      Painting.class, EntityDataSerializers.PAINTING_VARIANT
   );
   public static final float DEPTH = 0.0625F;

   public Painting(net.minecraft.world.entity.EntityType<? extends Painting> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_PAINTING_VARIANT_ID, VariantUtils.getAny(this.registryAccess(), Registries.PAINTING_VARIANT));
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      super.onSyncedDataUpdated($$0);
      if (DATA_PAINTING_VARIANT_ID.equals($$0)) {
         this.recalculateBoundingBox();
      }
   }

   private void setVariant(Holder<PaintingVariant> $$0) {
      this.entityData.set(DATA_PAINTING_VARIANT_ID, $$0);
   }

   public Holder<PaintingVariant> getVariant() {
      return (Holder<PaintingVariant>)this.entityData.get(DATA_PAINTING_VARIANT_ID);
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.PAINTING_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVariant()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.PAINTING_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.PAINTING_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.PAINTING_VARIANT, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   public static Optional<Painting> create(Level $$0, BlockPos $$1, Direction $$2) {
      Painting $$3 = new Painting($$0, $$1);
      List<Holder<PaintingVariant>> $$4 = new ArrayList<>();
      $$0.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT).getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach($$4::add);
      if ($$4.isEmpty()) {
         return Optional.empty();
      } else {
         $$3.setDirection($$2);
         $$4.removeIf($$1x -> {
            $$3.setVariant($$1x);
            return !$$3.survives();
         });
         if ($$4.isEmpty()) {
            return Optional.empty();
         } else {
            int $$5 = $$4.stream().mapToInt(Painting::variantArea).max().orElse(0);
            $$4.removeIf($$1x -> variantArea($$1x) < $$5);
            Optional<Holder<PaintingVariant>> $$6 = Util.getRandomSafe($$4, $$3.random);
            if ($$6.isEmpty()) {
               return Optional.empty();
            } else {
               $$3.setVariant($$6.get());
               $$3.setDirection($$2);
               return Optional.of($$3);
            }
         }
      }
   }

   private static int variantArea(Holder<PaintingVariant> $$0) {
      return ((PaintingVariant)$$0.value()).area();
   }

   private Painting(Level $$0, BlockPos $$1) {
      super(net.minecraft.world.entity.EntityType.PAINTING, $$0, $$1);
   }

   public Painting(Level $$0, BlockPos $$1, Direction $$2, Holder<PaintingVariant> $$3) {
      this($$0, $$1);
      this.setVariant($$3);
      this.setDirection($$2);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      $$0.store("facing", Direction.LEGACY_ID_CODEC_2D, this.getDirection());
      super.addAdditionalSaveData($$0);
      VariantUtils.writeVariant($$0, this.getVariant());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      Direction $$1 = $$0.read("facing", Direction.LEGACY_ID_CODEC_2D).orElse(Direction.SOUTH);
      super.readAdditionalSaveData($$0);
      this.setDirection($$1);
      VariantUtils.<PaintingVariant>readVariant($$0, Registries.PAINTING_VARIANT).ifPresent(this::setVariant);
   }

   @Override
   protected AABB calculateBoundingBox(BlockPos $$0, Direction $$1) {
      float $$2 = 0.46875F;
      Vec3 $$3 = Vec3.atCenterOf($$0).relative($$1, -0.46875);
      PaintingVariant $$4 = (PaintingVariant)this.getVariant().value();
      double $$5 = this.offsetForPaintingSize($$4.width());
      double $$6 = this.offsetForPaintingSize($$4.height());
      Direction $$7 = $$1.getCounterClockWise();
      Vec3 $$8 = $$3.relative($$7, $$5).relative(Direction.UP, $$6);
      Axis $$9 = $$1.getAxis();
      double $$10 = $$9 == Axis.X ? 0.0625 : $$4.width();
      double $$11 = $$4.height();
      double $$12 = $$9 == Axis.Z ? 0.0625 : $$4.width();
      return AABB.ofSize($$8, $$10, $$11, $$12);
   }

   private double offsetForPaintingSize(int $$0) {
      return $$0 % 2 == 0 ? 0.5 : 0.0;
   }

   @Override
   public void dropItem(ServerLevel $$0, net.minecraft.world.entity.Entity $$1) {
      if ((Boolean)$$0.getGameRules().get(GameRules.ENTITY_DROPS)) {
         this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
         if (!($$1 instanceof Player $$2 && $$2.hasInfiniteMaterials())) {
            this.spawnAtLocation($$0, Items.PAINTING);
         }
      }
   }

   @Override
   public void playPlacementSound() {
      this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
   }

   @Override
   public void snapTo(double $$0, double $$1, double $$2, float $$3, float $$4) {
      this.setPos($$0, $$1, $$2);
   }

   @Override
   public Vec3 trackingPosition() {
      return Vec3.atLowerCornerOf(this.pos);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
      return new ClientboundAddEntityPacket(this, this.getDirection().get3DDataValue(), this.getPos());
   }

   @Override
   public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
      super.recreateFromPacket($$0);
      this.setDirection(Direction.from3DDataValue($$0.getData()));
   }

   @Override
   public ItemStack getPickResult() {
      return new ItemStack(Items.PAINTING);
   }
}
