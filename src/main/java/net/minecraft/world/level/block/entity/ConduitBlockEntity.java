package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ConduitBlockEntity extends BlockEntity {
   private static final int BLOCK_REFRESH_RATE = 2;
   private static final int EFFECT_DURATION = 13;
   private static final float ROTATION_SPEED = -0.0375F;
   private static final int MIN_ACTIVE_SIZE = 16;
   private static final int MIN_KILL_SIZE = 42;
   private static final int KILL_RANGE = 8;
   private static final Block[] VALID_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
   public int tickCount;
   private float activeRotation;
   private boolean isActive;
   private boolean isHunting;
   private final List<BlockPos> effectBlocks = Lists.newArrayList();
   @Nullable
   private EntityReference<LivingEntity> destroyTarget;
   private long nextAmbientSoundActivation;

   public ConduitBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.CONDUIT, $$0, $$1);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.destroyTarget = EntityReference.read($$0, "Target");
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      EntityReference.store(this.destroyTarget, $$0, "Target");
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   public static void clientTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, ConduitBlockEntity $$3) {
      $$3.tickCount++;
      long $$4 = $$0.getGameTime();
      List<BlockPos> $$5 = $$3.effectBlocks;
      if ($$4 % 40L == 0L) {
         $$3.isActive = updateShape($$0, $$1, $$5);
         updateHunting($$3, $$5);
      }

      LivingEntity $$6 = EntityReference.getLivingEntity($$3.destroyTarget, $$0);
      animationTick($$0, $$1, $$5, $$6, $$3.tickCount);
      if ($$3.isActive()) {
         $$3.activeRotation++;
      }
   }

   public static void serverTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, ConduitBlockEntity $$3) {
      $$3.tickCount++;
      long $$4 = $$0.getGameTime();
      List<BlockPos> $$5 = $$3.effectBlocks;
      if ($$4 % 40L == 0L) {
         boolean $$6 = updateShape($$0, $$1, $$5);
         if ($$6 != $$3.isActive) {
            SoundEvent $$7 = $$6 ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE;
            $$0.playSound(null, $$1, $$7, SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         $$3.isActive = $$6;
         updateHunting($$3, $$5);
         if ($$6) {
            applyEffects($$0, $$1, $$5);
            updateAndAttackTarget((ServerLevel)$$0, $$1, $$2, $$3, $$5.size() >= 42);
         }
      }

      if ($$3.isActive()) {
         if ($$4 % 80L == 0L) {
            $$0.playSound(null, $$1, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         if ($$4 > $$3.nextAmbientSoundActivation) {
            $$3.nextAmbientSoundActivation = $$4 + 60L + $$0.getRandom().nextInt(40);
            $$0.playSound(null, $$1, SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.BLOCKS, 1.0F, 1.0F);
         }
      }
   }

   private static void updateHunting(ConduitBlockEntity $$0, List<BlockPos> $$1) {
      $$0.setHunting($$1.size() >= 42);
   }

   private static boolean updateShape(net.minecraft.world.level.Level $$0, BlockPos $$1, List<BlockPos> $$2) {
      $$2.clear();

      for (int $$3 = -1; $$3 <= 1; $$3++) {
         for (int $$4 = -1; $$4 <= 1; $$4++) {
            for (int $$5 = -1; $$5 <= 1; $$5++) {
               BlockPos $$6 = $$1.offset($$3, $$4, $$5);
               if (!$$0.isWaterAt($$6)) {
                  return false;
               }
            }
         }
      }

      for (int $$7 = -2; $$7 <= 2; $$7++) {
         for (int $$8 = -2; $$8 <= 2; $$8++) {
            for (int $$9 = -2; $$9 <= 2; $$9++) {
               int $$10 = Math.abs($$7);
               int $$11 = Math.abs($$8);
               int $$12 = Math.abs($$9);
               if (($$10 > 1 || $$11 > 1 || $$12 > 1)
                  && ($$7 == 0 && ($$11 == 2 || $$12 == 2) || $$8 == 0 && ($$10 == 2 || $$12 == 2) || $$9 == 0 && ($$10 == 2 || $$11 == 2))) {
                  BlockPos $$13 = $$1.offset($$7, $$8, $$9);
                  BlockState $$14 = $$0.getBlockState($$13);

                  for (Block $$15 : VALID_BLOCKS) {
                     if ($$14.is($$15)) {
                        $$2.add($$13);
                     }
                  }
               }
            }
         }
      }

      return $$2.size() >= 16;
   }

   private static void applyEffects(net.minecraft.world.level.Level $$0, BlockPos $$1, List<BlockPos> $$2) {
      int $$3 = $$2.size();
      int $$4 = $$3 / 7 * 16;
      int $$5 = $$1.getX();
      int $$6 = $$1.getY();
      int $$7 = $$1.getZ();
      AABB $$8 = new AABB($$5, $$6, $$7, $$5 + 1, $$6 + 1, $$7 + 1).inflate($$4).expandTowards(0.0, $$0.getHeight(), 0.0);
      List<Player> $$9 = $$0.getEntitiesOfClass(Player.class, $$8);
      if (!$$9.isEmpty()) {
         for (Player $$10 : $$9) {
            if ($$1.closerThan($$10.blockPosition(), $$4) && $$10.isInWaterOrRain()) {
               $$10.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
            }
         }
      }
   }

   private static void updateAndAttackTarget(ServerLevel $$0, BlockPos $$1, BlockState $$2, ConduitBlockEntity $$3, boolean $$4) {
      EntityReference<LivingEntity> $$5 = updateDestroyTarget($$3.destroyTarget, $$0, $$1, $$4);
      LivingEntity $$6 = EntityReference.getLivingEntity($$5, $$0);
      if ($$6 != null) {
         $$0.playSound(null, $$6.getX(), $$6.getY(), $$6.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0F, 1.0F);
         $$6.hurtServer($$0, $$0.damageSources().magic(), 4.0F);
      }

      if (!Objects.equals($$5, $$3.destroyTarget)) {
         $$3.destroyTarget = $$5;
         $$0.sendBlockUpdated($$1, $$2, $$2, 2);
      }
   }

   @Nullable
   private static EntityReference<LivingEntity> updateDestroyTarget(@Nullable EntityReference<LivingEntity> $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if (!$$3) {
         return null;
      } else if ($$0 == null) {
         return selectNewTarget($$1, $$2);
      } else {
         LivingEntity $$4 = EntityReference.getLivingEntity($$0, $$1);
         return $$4 != null && $$4.isAlive() && $$2.closerThan($$4.blockPosition(), 8.0) ? $$0 : null;
      }
   }

   @Nullable
   private static EntityReference<LivingEntity> selectNewTarget(ServerLevel $$0, BlockPos $$1) {
      List<LivingEntity> $$2 = $$0.getEntitiesOfClass(LivingEntity.class, getDestroyRangeAABB($$1), $$0x -> $$0x instanceof Enemy && $$0x.isInWaterOrRain());
      return $$2.isEmpty() ? null : EntityReference.of((LivingEntity)Util.getRandom($$2, $$0.random));
   }

   private static AABB getDestroyRangeAABB(BlockPos $$0) {
      return new AABB($$0).inflate(8.0);
   }

   private static void animationTick(net.minecraft.world.level.Level $$0, BlockPos $$1, List<BlockPos> $$2, @Nullable Entity $$3, int $$4) {
      RandomSource $$5 = $$0.random;
      double $$6 = Mth.sin(($$4 + 35) * 0.1F) / 2.0F + 0.5F;
      $$6 = ($$6 * $$6 + $$6) * 0.3F;
      Vec3 $$7 = new Vec3($$1.getX() + 0.5, $$1.getY() + 1.5 + $$6, $$1.getZ() + 0.5);

      for (BlockPos $$8 : $$2) {
         if ($$5.nextInt(50) == 0) {
            BlockPos $$9 = $$8.subtract($$1);
            float $$10 = -0.5F + $$5.nextFloat() + $$9.getX();
            float $$11 = -2.0F + $$5.nextFloat() + $$9.getY();
            float $$12 = -0.5F + $$5.nextFloat() + $$9.getZ();
            $$0.addParticle(ParticleTypes.NAUTILUS, $$7.x, $$7.y, $$7.z, $$10, $$11, $$12);
         }
      }

      if ($$3 != null) {
         Vec3 $$13 = new Vec3($$3.getX(), $$3.getEyeY(), $$3.getZ());
         float $$14 = (-0.5F + $$5.nextFloat()) * (3.0F + $$3.getBbWidth());
         float $$15 = -1.0F + $$5.nextFloat() * $$3.getBbHeight();
         float $$16 = (-0.5F + $$5.nextFloat()) * (3.0F + $$3.getBbWidth());
         Vec3 $$17 = new Vec3($$14, $$15, $$16);
         $$0.addParticle(ParticleTypes.NAUTILUS, $$13.x, $$13.y, $$13.z, $$17.x, $$17.y, $$17.z);
      }
   }

   public boolean isActive() {
      return this.isActive;
   }

   public boolean isHunting() {
      return this.isHunting;
   }

   private void setHunting(boolean $$0) {
      this.isHunting = $$0;
   }

   public float getActiveRotation(float $$0) {
      return (this.activeRotation + $$0) * -0.0375F;
   }
}
