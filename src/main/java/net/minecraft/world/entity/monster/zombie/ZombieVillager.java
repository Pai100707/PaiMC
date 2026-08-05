package net.minecraft.world.entity.monster.zombie;

import com.google.common.annotations.VisibleForTesting;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ZombieVillager extends Zombie implements VillagerDataHolder {
   private static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(
      ZombieVillager.class, EntityDataSerializers.VILLAGER_DATA
   );
   private static final int VILLAGER_CONVERSION_WAIT_MIN = 3600;
   private static final int VILLAGER_CONVERSION_WAIT_MAX = 6000;
   private static final int MAX_SPECIAL_BLOCKS_COUNT = 14;
   private static final int SPECIAL_BLOCK_RADIUS = 4;
   private static final int NOT_CONVERTING = -1;
   private static final int DEFAULT_XP = 0;
   private static final Set<net.minecraft.world.entity.EntitySpawnReason> REASONS_NOT_TO_SET_TYPE = EnumSet.of(
      net.minecraft.world.entity.EntitySpawnReason.LOAD,
      net.minecraft.world.entity.EntitySpawnReason.DIMENSION_TRAVEL,
      net.minecraft.world.entity.EntitySpawnReason.CONVERSION,
      net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE,
      net.minecraft.world.entity.EntitySpawnReason.SPAWNER,
      net.minecraft.world.entity.EntitySpawnReason.TRIAL_SPAWNER
   );
   private int villagerConversionTime;
   
   private UUID conversionStarter;
   
   private GossipContainer gossips;
   
   private MerchantOffers tradeOffers;
   private int villagerXp = 0;

   public ZombieVillager(net.minecraft.world.entity.EntityType<? extends ZombieVillager> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_CONVERTING_ID, false);
      $$0.define(DATA_VILLAGER_DATA, this.initializeVillagerData());
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("VillagerData", VillagerData.CODEC, this.getVillagerData());
      $$0.storeNullable("Offers", MerchantOffers.CODEC, this.tradeOffers);
      $$0.storeNullable("Gossips", GossipContainer.CODEC, this.gossips);
      $$0.putInt("ConversionTime", this.isConverting() ? this.villagerConversionTime : -1);
      $$0.storeNullable("ConversionPlayer", UUIDUtil.CODEC, this.conversionStarter);
      $$0.putInt("Xp", this.villagerXp);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.entityData.set(DATA_VILLAGER_DATA, $$0.read("VillagerData", VillagerData.CODEC).orElseGet(this::initializeVillagerData));
      this.tradeOffers = (MerchantOffers)$$0.read("Offers", MerchantOffers.CODEC).orElse(null);
      this.gossips = (GossipContainer)$$0.read("Gossips", GossipContainer.CODEC).orElse(null);
      int $$1 = $$0.getIntOr("ConversionTime", -1);
      if ($$1 != -1) {
         UUID $$2 = (UUID)$$0.read("ConversionPlayer", UUIDUtil.CODEC).orElse(null);
         this.startConverting($$2, $$1);
      } else {
         this.getEntityData().set(DATA_CONVERTING_ID, false);
         this.villagerConversionTime = -1;
      }

      this.villagerXp = $$0.getIntOr("Xp", 0);
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      if (!REASONS_NOT_TO_SET_TYPE.contains($$2)) {
         this.setVillagerData(this.getVillagerData().withType($$0.registryAccess(), VillagerType.byBiome($$0.getBiome(this.blockPosition()))));
      }

      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   private VillagerData initializeVillagerData() {
      Optional<Reference<VillagerProfession>> $$0 = BuiltInRegistries.VILLAGER_PROFESSION.getRandom(this.random);
      VillagerData $$1 = Villager.createDefaultVillagerData();
      if ($$0.isPresent()) {
         $$1 = $$1.withProfession((Holder<VillagerProfession>)$$0.get());
      }

      return $$1;
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide() && this.isAlive() && this.isConverting()) {
         int $$0 = this.getConversionProgress();
         this.villagerConversionTime -= $$0;
         if (this.villagerConversionTime <= 0) {
            this.finishConversion((ServerLevel)this.level());
         }
      }

      super.tick();
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if ($$2.is(Items.GOLDEN_APPLE)) {
         if (this.hasEffect(MobEffects.WEAKNESS)) {
            $$2.consume(1, $$0);
            if (!this.level().isClientSide()) {
               this.startConverting($$0.getUUID(), this.random.nextInt(2401) + 3600);
            }

            return InteractionResult.SUCCESS_SERVER;
         } else {
            return InteractionResult.CONSUME;
         }
      } else {
         return super.mobInteract($$0, $$1);
      }
   }

   @Override
   protected boolean convertsInWater() {
      return false;
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return !this.isConverting() && this.villagerXp == 0;
   }

   public boolean isConverting() {
      return (Boolean)this.getEntityData().get(DATA_CONVERTING_ID);
   }

   private void startConverting(UUID $$0, int $$1) {
      this.conversionStarter = $$0;
      this.villagerConversionTime = $$1;
      this.getEntityData().set(DATA_CONVERTING_ID, true);
      this.removeEffect(MobEffects.WEAKNESS);
      this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, $$1, Math.min(this.level().getDifficulty().getId() - 1, 0)));
      this.level().broadcastEntityEvent(this, (byte)16);
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 16) {
         if (!this.isSilent()) {
            this.level()
               .playLocalSound(
                  this.getX(),
                  this.getEyeY(),
                  this.getZ(),
                  SoundEvents.ZOMBIE_VILLAGER_CURE,
                  this.getSoundSource(),
                  1.0F + this.random.nextFloat(),
                  this.random.nextFloat() * 0.7F + 0.3F,
                  false
               );
         }
      } else {
         super.handleEntityEvent($$0);
      }
   }

   private void finishConversion(ServerLevel $$0) {
      this.convertTo(
         net.minecraft.world.entity.EntityType.VILLAGER,
         net.minecraft.world.entity.ConversionParams.single(this, false, false),
         $$1 -> {
            for (net.minecraft.world.entity.EquipmentSlot $$2 : this.dropPreservedEquipment(
               $$0, $$0xx -> !EnchantmentHelper.has($$0xx, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
            )) {
               net.minecraft.world.entity.SlotAccess $$3 = $$1.getSlot($$2.getIndex() + 300);
               if ($$3 != null) {
                  $$3.set(this.getItemBySlot($$2));
               }
            }

            $$1.setVillagerData(this.getVillagerData());
            if (this.gossips != null) {
               $$1.setGossips(this.gossips);
            }

            if (this.tradeOffers != null) {
               $$1.setOffers(this.tradeOffers.copy());
            }

            $$1.setVillagerXp(this.villagerXp);
            $$1.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$1.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.CONVERSION, null);
            $$1.refreshBrain($$0);
            if (this.conversionStarter != null) {
               Player $$4 = $$0.getPlayerByUUID(this.conversionStarter);
               if ($$4 instanceof ServerPlayer) {
                  CriteriaTriggers.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayer)$$4, this, $$1);
                  $$0.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, $$4, $$1);
               }
            }

            $$1.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
            if (!this.isSilent()) {
               $$0.levelEvent(null, 1027, this.blockPosition(), 0);
            }
         }
      );
   }

   @VisibleForTesting
   public void setVillagerConversionTime(int $$0) {
      this.villagerConversionTime = $$0;
   }

   private int getConversionProgress() {
      int $$0 = 1;
      if (this.random.nextFloat() < 0.01F) {
         int $$1 = 0;
         MutableBlockPos $$2 = new MutableBlockPos();

         for (int $$3 = (int)this.getX() - 4; $$3 < (int)this.getX() + 4 && $$1 < 14; $$3++) {
            for (int $$4 = (int)this.getY() - 4; $$4 < (int)this.getY() + 4 && $$1 < 14; $$4++) {
               for (int $$5 = (int)this.getZ() - 4; $$5 < (int)this.getZ() + 4 && $$1 < 14; $$5++) {
                  BlockState $$6 = this.level().getBlockState($$2.set($$3, $$4, $$5));
                  if ($$6.is(Blocks.IRON_BARS) || $$6.getBlock() instanceof BedBlock) {
                     if (this.random.nextFloat() < 0.3F) {
                        $$0++;
                     }

                     $$1++;
                  }
               }
            }
         }
      }

      return $$0;
   }

   @Override
   public float getVoicePitch() {
      return this.isBaby()
         ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.0F
         : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
   }

   @Override
   public SoundEvent getAmbientSound() {
      return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
   }

   @Override
   public SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.ZOMBIE_VILLAGER_HURT;
   }

   @Override
   public SoundEvent getDeathSound() {
      return SoundEvents.ZOMBIE_VILLAGER_DEATH;
   }

   @Override
   public SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_VILLAGER_STEP;
   }

   public void setTradeOffers(MerchantOffers $$0) {
      this.tradeOffers = $$0;
   }

   public void setGossips(GossipContainer $$0) {
      this.gossips = $$0;
   }

   @Override
   public void setVillagerData(VillagerData $$0) {
      VillagerData $$1 = this.getVillagerData();
      if (!$$1.profession().equals($$0.profession())) {
         this.tradeOffers = null;
      }

      this.entityData.set(DATA_VILLAGER_DATA, $$0);
   }

   @Override
   public VillagerData getVillagerData() {
      return (VillagerData)this.entityData.get(DATA_VILLAGER_DATA);
   }

   public int getVillagerXp() {
      return this.villagerXp;
   }

   public void setVillagerXp(int $$0) {
      this.villagerXp = $$0;
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.VILLAGER_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVillagerData().type()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.VILLAGER_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.VILLAGER_VARIANT) {
         Holder<VillagerType> $$2 = castComponentValue(DataComponents.VILLAGER_VARIANT, $$1);
         this.setVillagerData(this.getVillagerData().withType($$2));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }
}
