package net.minecraft.world.entity.npc.villager;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractVillager extends net.minecraft.world.entity.AgeableMob implements InventoryCarrier, Npc, Merchant {
   private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);
   public static final int VILLAGER_SLOT_OFFSET = 300;
   private static final int VILLAGER_INVENTORY_SIZE = 8;
   @Nullable
   private Player tradingPlayer;
   @Nullable
   protected MerchantOffers offers;
   private final SimpleContainer inventory = new SimpleContainer(8);

   public AbstractVillager(net.minecraft.world.entity.EntityType<? extends AbstractVillager> $$0, Level $$1) {
      super($$0, $$1);
      this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      if ($$3 == null) {
         $$3 = new net.minecraft.world.entity.AgeableMob.AgeableMobGroupData(false);
      }

      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public int getUnhappyCounter() {
      return (Integer)this.entityData.get(DATA_UNHAPPY_COUNTER);
   }

   public void setUnhappyCounter(int $$0) {
      this.entityData.set(DATA_UNHAPPY_COUNTER, $$0);
   }

   public int getVillagerXp() {
      return 0;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_UNHAPPY_COUNTER, 0);
   }

   public void setTradingPlayer(@Nullable Player $$0) {
      this.tradingPlayer = $$0;
   }

   @Nullable
   public Player getTradingPlayer() {
      return this.tradingPlayer;
   }

   public boolean isTrading() {
      return this.tradingPlayer != null;
   }

   public MerchantOffers getOffers() {
      if (this.level() instanceof ServerLevel $$0) {
         if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.updateTrades($$0);
         }

         return this.offers;
      } else {
         throw new IllegalStateException("Cannot load Villager offers on the client");
      }
   }

   public void overrideOffers(@Nullable MerchantOffers $$0) {
   }

   public void overrideXp(int $$0) {
   }

   public void notifyTrade(MerchantOffer $$0) {
      $$0.increaseUses();
      this.ambientSoundTime = -this.getAmbientSoundInterval();
      this.rewardTradeXp($$0);
      if (this.tradingPlayer instanceof ServerPlayer) {
         CriteriaTriggers.TRADE.trigger((ServerPlayer)this.tradingPlayer, this, $$0.getResult());
      }
   }

   protected abstract void rewardTradeXp(MerchantOffer var1);

   public boolean showProgressBar() {
      return true;
   }

   public void notifyTradeUpdated(ItemStack $$0) {
      if (!this.level().isClientSide() && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
         this.ambientSoundTime = -this.getAmbientSoundInterval();
         this.makeSound(this.getTradeUpdatedSound(!$$0.isEmpty()));
      }
   }

   public SoundEvent getNotifyTradeSound() {
      return SoundEvents.VILLAGER_YES;
   }

   protected SoundEvent getTradeUpdatedSound(boolean $$0) {
      return $$0 ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
   }

   public void playCelebrateSound() {
      this.makeSound(SoundEvents.VILLAGER_CELEBRATE);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      if (!this.level().isClientSide()) {
         MerchantOffers $$1 = this.getOffers();
         if (!$$1.isEmpty()) {
            $$0.store("Offers", MerchantOffers.CODEC, $$1);
         }
      }

      this.writeInventoryToTag($$0);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.offers = (MerchantOffers)$$0.read("Offers", MerchantOffers.CODEC).orElse(null);
      this.readInventoryFromTag($$0);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.Entity teleport(TeleportTransition $$0) {
      this.stopTrading();
      return super.teleport($$0);
   }

   protected void stopTrading() {
      this.setTradingPlayer(null);
   }

   @Override
   public void die(DamageSource $$0) {
      super.die($$0);
      this.stopTrading();
   }

   protected void addParticlesAroundSelf(ParticleOptions $$0) {
      for (int $$1 = 0; $$1 < 5; $$1++) {
         double $$2 = this.random.nextGaussian() * 0.02;
         double $$3 = this.random.nextGaussian() * 0.02;
         double $$4 = this.random.nextGaussian() * 0.02;
         this.level().addParticle($$0, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), $$2, $$3, $$4);
      }
   }

   @Override
   public boolean canBeLeashed() {
      return false;
   }

   @Override
   public SimpleContainer getInventory() {
      return this.inventory;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SlotAccess getSlot(int $$0) {
      int $$1 = $$0 - 300;
      return $$1 >= 0 && $$1 < this.inventory.getContainerSize() ? this.inventory.getSlot($$1) : super.getSlot($$0);
   }

   protected abstract void updateTrades(ServerLevel var1);

   protected void addOffersFromItemListings(ServerLevel $$0, MerchantOffers $$1, VillagerTrades.ItemListing[] $$2, int $$3) {
      ArrayList<VillagerTrades.ItemListing> $$4 = Lists.newArrayList($$2);
      int $$5 = 0;

      while ($$5 < $$3 && !$$4.isEmpty()) {
         MerchantOffer $$6 = $$4.remove(this.random.nextInt($$4.size())).getOffer($$0, this, this.random);
         if ($$6 != null) {
            $$1.add($$6);
            $$5++;
         }
      }
   }

   @Override
   public Vec3 getRopeHoldPosition(float $$0) {
      float $$1 = Mth.lerp($$0, this.yBodyRotO, this.yBodyRot) * (float) (Math.PI / 180.0);
      Vec3 $$2 = new Vec3(0.0, this.getBoundingBox().getYsize() - 1.0, 0.2);
      return this.getPosition($$0).add($$2.yRot(-$$1));
   }

   public boolean isClientSide() {
      return this.level().isClientSide();
   }

   public boolean stillValid(Player $$0) {
      return this.getTradingPlayer() == $$0 && this.isAlive() && $$0.isWithinEntityInteractionRange(this, 4.0);
   }
}
