package net.minecraft.world.entity.animal.cow;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Cow extends AbstractCow {
   private static final EntityDataAccessor<Holder<CowVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Cow.class, EntityDataSerializers.COW_VARIANT);

   public Cow(net.minecraft.world.entity.EntityType<? extends Cow> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), CowVariants.TEMPERATE));
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      VariantUtils.writeVariant($$0, this.getVariant());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      VariantUtils.<CowVariant>readVariant($$0, Registries.COW_VARIANT).ifPresent(this::setVariant);
   }

   
   public Cow getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Cow $$2 = net.minecraft.world.entity.EntityType.COW.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null && $$1 instanceof Cow $$3) {
         $$2.setVariant(this.random.nextBoolean() ? this.getVariant() : $$3.getVariant());
      }

      return $$2;
   }

   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      VariantUtils.<CowVariant>selectVariantToSpawn(SpawnContext.create($$0, this.blockPosition()), Registries.COW_VARIANT).ifPresent(this::setVariant);
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public void setVariant(Holder<CowVariant> $$0) {
      this.entityData.set(DATA_VARIANT_ID, $$0);
   }

   public Holder<CowVariant> getVariant() {
      return (Holder<CowVariant>)this.entityData.get(DATA_VARIANT_ID);
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.COW_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVariant()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.COW_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.COW_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.COW_VARIANT, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }
}
