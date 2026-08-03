package net.minecraft.world.entity.boss.enderdragon;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class EnderDragonPart extends net.minecraft.world.entity.Entity {
   public final EnderDragon parentMob;
   public final String name;
   private final net.minecraft.world.entity.EntityDimensions size;

   public EnderDragonPart(EnderDragon $$0, String $$1, float $$2, float $$3) {
      super($$0.getType(), $$0.level());
      this.size = net.minecraft.world.entity.EntityDimensions.scalable($$2, $$3);
      this.refreshDimensions();
      this.parentMob = $$0;
      this.name = $$1;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
   }

   @Override
   public boolean isPickable() {
      return true;
   }

   @Nullable
   @Override
   public ItemStack getPickResult() {
      return this.parentMob.getPickResult();
   }

   @Override
   public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      return this.isInvulnerableToBase($$1) ? false : this.parentMob.hurt($$0, this, $$1, $$2);
   }

   @Override
   public boolean is(net.minecraft.world.entity.Entity $$0) {
      return this == $$0 || this.parentMob == $$0;
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
      throw new UnsupportedOperationException();
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.size;
   }

   @Override
   public boolean shouldBeSaved() {
      return false;
   }
}
