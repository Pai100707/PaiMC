package net.minecraft.world.entity.animal.cow;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class MushroomCow extends AbstractCow implements net.minecraft.world.entity.Shearable {
   private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.INT);
   private static final int MUTATE_CHANCE = 1024;
   private static final String TAG_STEW_EFFECTS = "stew_effects";
   @Nullable
   private SuspiciousStewEffects stewEffects;
   @Nullable
   private UUID lastLightningBoltUUID;

   public MushroomCow(net.minecraft.world.entity.EntityType<? extends MushroomCow> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      return $$1.getBlockState($$0.below()).is(Blocks.MYCELIUM) ? 10.0F : $$1.getPathfindingCostFromLightLevels($$0);
   }

   public static boolean checkMushroomSpawnRules(
      net.minecraft.world.entity.EntityType<MushroomCow> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return $$1.getBlockState($$3.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && isBrightEnoughToSpawn($$1, $$3);
   }

   @Override
   public void thunderHit(ServerLevel $$0, net.minecraft.world.entity.LightningBolt $$1) {
      UUID $$2 = $$1.getUUID();
      if (!$$2.equals(this.lastLightningBoltUUID)) {
         this.setVariant(this.getVariant() == MushroomCow.Variant.RED ? MushroomCow.Variant.BROWN : MushroomCow.Variant.RED);
         this.lastLightningBoltUUID = $$2;
         this.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0F, 1.0F);
      }
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_TYPE, MushroomCow.Variant.DEFAULT.id);
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if ($$2.is(Items.BOWL) && !this.isBaby()) {
         boolean $$3 = false;
         ItemStack $$4;
         if (this.stewEffects != null) {
            $$3 = true;
            $$4 = new ItemStack(Items.SUSPICIOUS_STEW);
            $$4.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.stewEffects);
            this.stewEffects = null;
         } else {
            $$4 = new ItemStack(Items.MUSHROOM_STEW);
         }

         ItemStack $$6 = ItemUtils.createFilledResult($$2, $$0, $$4, false);
         $$0.setItemInHand($$1, $$6);
         SoundEvent $$7;
         if ($$3) {
            $$7 = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
         } else {
            $$7 = SoundEvents.MOOSHROOM_MILK;
         }

         this.playSound($$7, 1.0F, 1.0F);
         return InteractionResult.SUCCESS;
      } else if ($$2.is(Items.SHEARS) && this.readyForShearing()) {
         if (this.level() instanceof ServerLevel $$9) {
            this.shear($$9, SoundSource.PLAYERS, $$2);
            this.gameEvent(GameEvent.SHEAR, $$0);
            $$2.hurtAndBreak(1, $$0, $$1.asEquipmentSlot());
         }

         return InteractionResult.SUCCESS;
      } else if (this.getVariant() == MushroomCow.Variant.BROWN) {
         Optional<SuspiciousStewEffects> $$10 = this.getEffectsFromItemStack($$2);
         if ($$10.isEmpty()) {
            return super.mobInteract($$0, $$1);
         } else {
            if (this.stewEffects != null) {
               for (int $$11 = 0; $$11 < 2; $$11++) {
                  this.level()
                     .addParticle(
                        ParticleTypes.SMOKE,
                        this.getX() + this.random.nextDouble() / 2.0,
                        this.getY(0.5),
                        this.getZ() + this.random.nextDouble() / 2.0,
                        0.0,
                        this.random.nextDouble() / 5.0,
                        0.0
                     );
               }
            } else {
               $$2.consume(1, $$0);
               SpellParticleOption $$12 = SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0F);

               for (int $$13 = 0; $$13 < 4; $$13++) {
                  this.level()
                     .addParticle(
                        $$12,
                        this.getX() + this.random.nextDouble() / 2.0,
                        this.getY(0.5),
                        this.getZ() + this.random.nextDouble() / 2.0,
                        0.0,
                        this.random.nextDouble() / 5.0,
                        0.0
                     );
               }

               this.stewEffects = $$10.get();
               this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0F, 1.0F);
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return super.mobInteract($$0, $$1);
      }
   }

   @Override
   public void shear(ServerLevel $$0, SoundSource $$1, ItemStack $$2) {
      $$0.playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, $$1, 1.0F, 1.0F);
      this.convertTo(net.minecraft.world.entity.EntityType.COW, net.minecraft.world.entity.ConversionParams.single(this, false, false), $$2x -> {
         $$0.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
         this.dropFromShearingLootTable($$0, BuiltInLootTables.SHEAR_MOOSHROOM, $$2, ($$0xx, $$1xx) -> {
            for (int $$2xx = 0; $$2xx < $$1xx.getCount(); $$2xx++) {
               $$0xx.addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(1.0), this.getZ(), $$1xx.copyWithCount(1)));
            }
         });
      });
   }

   @Override
   public boolean readyForShearing() {
      return this.isAlive() && !this.isBaby();
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("Type", MushroomCow.Variant.CODEC, this.getVariant());
      $$0.storeNullable("stew_effects", SuspiciousStewEffects.CODEC, this.stewEffects);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setVariant($$0.read("Type", MushroomCow.Variant.CODEC).orElse(MushroomCow.Variant.DEFAULT));
      this.stewEffects = (SuspiciousStewEffects)$$0.read("stew_effects", SuspiciousStewEffects.CODEC).orElse(null);
   }

   private Optional<SuspiciousStewEffects> getEffectsFromItemStack(ItemStack $$0) {
      SuspiciousEffectHolder $$1 = SuspiciousEffectHolder.tryGet($$0.getItem());
      return $$1 != null ? Optional.of($$1.getSuspiciousEffects()) : Optional.empty();
   }

   private void setVariant(MushroomCow.Variant $$0) {
      this.entityData.set(DATA_TYPE, $$0.id);
   }

   public MushroomCow.Variant getVariant() {
      return MushroomCow.Variant.byId((Integer)this.entityData.get(DATA_TYPE));
   }

   @Nullable
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.MOOSHROOM_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVariant()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.MOOSHROOM_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.MOOSHROOM_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.MOOSHROOM_VARIANT, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   @Nullable
   public MushroomCow getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      MushroomCow $$2 = net.minecraft.world.entity.EntityType.MOOSHROOM.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null) {
         $$2.setVariant(this.getOffspringVariant((MushroomCow)$$1));
      }

      return $$2;
   }

   private MushroomCow.Variant getOffspringVariant(MushroomCow $$0) {
      MushroomCow.Variant $$1 = this.getVariant();
      MushroomCow.Variant $$2 = $$0.getVariant();
      MushroomCow.Variant $$3;
      if ($$1 == $$2 && this.random.nextInt(1024) == 0) {
         $$3 = $$1 == MushroomCow.Variant.BROWN ? MushroomCow.Variant.RED : MushroomCow.Variant.BROWN;
      } else {
         $$3 = this.random.nextBoolean() ? $$1 : $$2;
      }

      return $$3;
   }

   public static enum Variant implements StringRepresentable {
      RED("red", 0, Blocks.RED_MUSHROOM.defaultBlockState()),
      BROWN("brown", 1, Blocks.BROWN_MUSHROOM.defaultBlockState());

      public static final MushroomCow.Variant DEFAULT = RED;
      public static final Codec<MushroomCow.Variant> CODEC = StringRepresentable.fromEnum(MushroomCow.Variant::values);
      private static final IntFunction<MushroomCow.Variant> BY_ID = ByIdMap.continuous(MushroomCow.Variant::id, values(), OutOfBoundsStrategy.CLAMP);
      public static final StreamCodec<ByteBuf, MushroomCow.Variant> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, MushroomCow.Variant::id);
      private final String type;
      final int id;
      private final BlockState blockState;

      private Variant(final String $$0, final int $$1, final BlockState $$2) {
         this.type = $$0;
         this.id = $$1;
         this.blockState = $$2;
      }

      public BlockState getBlockState() {
         return this.blockState;
      }

      public String getSerializedName() {
         return this.type;
      }

      private int id() {
         return this.id;
      }

      static MushroomCow.Variant byId(int $$0) {
         return BY_ID.apply($$0);
      }
   }
}
