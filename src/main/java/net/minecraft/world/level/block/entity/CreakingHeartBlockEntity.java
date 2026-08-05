package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos.TraversalNodeStatus;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.util.Util;
import net.minecraft.util.SpawnUtil.Strategy;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class CreakingHeartBlockEntity extends BlockEntity {
   private static final int PLAYER_DETECTION_RANGE = 32;
   public static final int CREAKING_ROAMING_RADIUS = 32;
   private static final int DISTANCE_CREAKING_TOO_FAR = 34;
   private static final int SPAWN_RANGE_XZ = 16;
   private static final int SPAWN_RANGE_Y = 8;
   private static final int ATTEMPTS_PER_SPAWN = 5;
   private static final int UPDATE_TICKS = 20;
   private static final int UPDATE_TICKS_VARIANCE = 5;
   private static final int HURT_CALL_TOTAL_TICKS = 100;
   private static final int NUMBER_OF_HURT_CALLS = 10;
   private static final int HURT_CALL_INTERVAL = 10;
   private static final int HURT_CALL_PARTICLE_TICKS = 50;
   private static final int MAX_DEPTH = 2;
   private static final int MAX_COUNT = 64;
   private static final int TICKS_GRACE_PERIOD = 30;
   private static final Optional<Creaking> NO_CREAKING = Optional.empty();
   
   private Either<Creaking, UUID> creakingInfo;
   private long ticksExisted;
   private int ticker;
   private int emitter;
   
   private Vec3 emitterTarget;
   private int outputSignal;

   public CreakingHeartBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.CREAKING_HEART, $$0, $$1);
   }

   public static void serverTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, CreakingHeartBlockEntity $$3) {
      $$3.ticksExisted++;
      if ($$0 instanceof ServerLevel $$4) {
         int $$6 = $$3.computeAnalogOutputSignal();
         if ($$3.outputSignal != $$6) {
            $$3.outputSignal = $$6;
            $$0.updateNeighbourForOutputSignal($$1, Blocks.CREAKING_HEART);
         }

         if ($$3.emitter > 0) {
            if ($$3.emitter > 50) {
               $$3.emitParticles($$4, 1, true);
               $$3.emitParticles($$4, 1, false);
            }

            if ($$3.emitter % 10 == 0 && $$3.emitterTarget != null) {
               $$3.getCreakingProtector().ifPresent($$1x -> $$3.emitterTarget = $$1x.getBoundingBox().getCenter());
               Vec3 $$7 = Vec3.atCenterOf($$1);
               float $$8 = 0.2F + 0.8F * (100 - $$3.emitter) / 100.0F;
               Vec3 $$9 = $$7.subtract($$3.emitterTarget).scale($$8).add($$3.emitterTarget);
               BlockPos $$10 = BlockPos.containing($$9);
               float $$11 = $$3.emitter / 2.0F / 100.0F + 0.5F;
               $$4.playSound(null, $$10, SoundEvents.CREAKING_HEART_HURT, SoundSource.BLOCKS, $$11, 1.0F);
            }

            $$3.emitter--;
         }

         if ($$3.ticker-- < 0) {
            $$3.ticker = $$3.level == null ? 20 : $$3.level.random.nextInt(5) + 20;
            BlockState $$12 = updateCreakingState($$0, $$2, $$1, $$3);
            if ($$12 != $$2) {
               $$0.setBlock($$1, $$12, 3);
               if ($$12.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.UPROOTED) {
                  return;
               }
            }

            if ($$3.creakingInfo == null) {
               if ($$12.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.AWAKE) {
                  if ($$4.isSpawningMonsters()) {
                     Player $$15 = $$0.getNearestPlayer($$1.getX(), $$1.getY(), $$1.getZ(), 32.0, false);
                     if ($$15 != null) {
                        Creaking $$16 = spawnProtector($$4, $$3);
                        if ($$16 != null) {
                           $$3.setCreakingInfo($$16);
                           $$16.makeSound(SoundEvents.CREAKING_SPAWN);
                           $$0.playSound(null, $$3.getBlockPos(), SoundEvents.CREAKING_HEART_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                     }
                  }
               }
            } else {
               Optional<Creaking> $$13 = $$3.getCreakingProtector();
               if ($$13.isPresent()) {
                  Creaking $$14 = $$13.get();
                  if (!(Boolean)$$0.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, $$1) && !$$14.isPersistenceRequired()
                     || $$3.distanceToCreaking() > 34.0
                     || $$14.playerIsStuckInYou()) {
                     $$3.removeProtector(null);
                  }
               }
            }
         }
      }
   }

   private static BlockState updateCreakingState(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, CreakingHeartBlockEntity $$3) {
      if (!CreakingHeartBlock.hasRequiredLogs($$1, $$0, $$2) && $$3.creakingInfo == null) {
         return $$1.setValue(CreakingHeartBlock.STATE, CreakingHeartState.UPROOTED);
      } else {
         CreakingHeartState $$4 = $$0.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, $$2)
            ? CreakingHeartState.AWAKE
            : CreakingHeartState.DORMANT;
         return $$1.setValue(CreakingHeartBlock.STATE, $$4);
      }
   }

   private double distanceToCreaking() {
      return this.getCreakingProtector().map($$0 -> Math.sqrt($$0.distanceToSqr(Vec3.atBottomCenterOf(this.getBlockPos())))).orElse(0.0);
   }

   private void clearCreakingInfo() {
      this.creakingInfo = null;
      this.setChanged();
   }

   public void setCreakingInfo(Creaking $$0) {
      this.creakingInfo = Either.left($$0);
      this.setChanged();
   }

   public void setCreakingInfo(UUID $$0) {
      this.creakingInfo = Either.right($$0);
      this.ticksExisted = 0L;
      this.setChanged();
   }

   private Optional<Creaking> getCreakingProtector() {
      if (this.creakingInfo == null) {
         return NO_CREAKING;
      } else {
         if (this.creakingInfo.left().isPresent()) {
            Creaking $$0 = (Creaking)this.creakingInfo.left().get();
            if (!$$0.isRemoved()) {
               return Optional.of($$0);
            }

            this.setCreakingInfo($$0.getUUID());
         }

         if (this.level instanceof ServerLevel $$1 && this.creakingInfo.right().isPresent()) {
            UUID $$2 = (UUID)this.creakingInfo.right().get();
            if ($$1.getEntity($$2) instanceof Creaking $$4) {
               this.setCreakingInfo($$4);
               return Optional.of($$4);
            } else {
               if (this.ticksExisted >= 30L) {
                  this.clearCreakingInfo();
               }

               return NO_CREAKING;
            }
         } else {
            return NO_CREAKING;
         }
      }
   }

   
   private static Creaking spawnProtector(ServerLevel $$0, CreakingHeartBlockEntity $$1) {
      BlockPos $$2 = $$1.getBlockPos();
      Optional<Creaking> $$3 = SpawnUtil.trySpawnMob(
         EntityType.CREAKING, EntitySpawnReason.SPAWNER, $$0, $$2, 5, 16, 8, Strategy.ON_TOP_OF_COLLIDER_NO_LEAVES, true
      );
      if ($$3.isEmpty()) {
         return null;
      } else {
         Creaking $$4 = $$3.get();
         $$0.gameEvent($$4, GameEvent.ENTITY_PLACE, $$4.position());
         $$0.broadcastEntityEvent($$4, (byte)60);
         $$4.setTransient($$2);
         return $$4;
      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   public void creakingHurt() {
      if (this.getCreakingProtector().orElse(null) instanceof Creaking $$0) {
         if (this.level instanceof ServerLevel $$2) {
            if (this.emitter <= 0) {
               this.emitParticles($$2, 20, false);
               if (this.getBlockState().getValue(CreakingHeartBlock.STATE) == CreakingHeartState.AWAKE) {
                  int $$4 = this.level.getRandom().nextIntBetweenInclusive(2, 3);

                  for (int $$5 = 0; $$5 < $$4; $$5++) {
                     this.spreadResin($$2).ifPresent($$0x -> {
                        this.level.playSound(null, $$0x, SoundEvents.RESIN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        this.level.gameEvent(GameEvent.BLOCK_PLACE, $$0x, GameEvent.Context.of(this.getBlockState()));
                     });
                  }
               }

               this.emitter = 100;
               this.emitterTarget = $$0.getBoundingBox().getCenter();
            }
         }
      }
   }

   private Optional<BlockPos> spreadResin(ServerLevel $$0) {
      Mutable<BlockPos> $$1 = new MutableObject(null);
      BlockPos.breadthFirstTraversal(this.worldPosition, 2, 64, ($$1x, $$2) -> {
         for (Direction $$3 : Util.shuffledCopy(Direction.values(), $$0.random)) {
            BlockPos $$4 = $$1x.relative($$3);
            if ($$0.getBlockState($$4).is(BlockTags.PALE_OAK_LOGS)) {
               $$2.accept($$4);
            }
         }
      }, $$2 -> {
         if (!$$0.getBlockState($$2).is(BlockTags.PALE_OAK_LOGS)) {
            return TraversalNodeStatus.ACCEPT;
         } else {
            for (Direction $$3 : Util.shuffledCopy(Direction.values(), $$0.random)) {
               BlockPos $$4 = $$2.relative($$3);
               BlockState $$5 = $$0.getBlockState($$4);
               Direction $$6 = $$3.getOpposite();
               if ($$5.isAir()) {
                  $$5 = Blocks.RESIN_CLUMP.defaultBlockState();
               } else if ($$5.is(Blocks.WATER) && $$5.getFluidState().isSource()) {
                  $$5 = Blocks.RESIN_CLUMP.defaultBlockState().setValue(MultifaceBlock.WATERLOGGED, true);
               }

               if ($$5.is(Blocks.RESIN_CLUMP) && !MultifaceBlock.hasFace($$5, $$6)) {
                  $$0.setBlock($$4, $$5.setValue(MultifaceBlock.getFaceProperty($$6), true), 3);
                  $$1.setValue($$4);
                  return TraversalNodeStatus.STOP;
               }
            }

            return TraversalNodeStatus.ACCEPT;
         }
      });
      return Optional.ofNullable((BlockPos)$$1.get());
   }

   private void emitParticles(ServerLevel $$0, int $$1, boolean $$2) {
      if (this.getCreakingProtector().orElse(null) instanceof Creaking $$3) {
         int $$5 = $$2 ? 16545810 : 6250335;
         RandomSource $$6 = $$0.random;

         for (double $$7 = 0.0; $$7 < $$1; $$7++) {
            AABB $$8 = $$3.getBoundingBox();
            Vec3 $$9 = $$8.getMinPosition().add($$6.nextDouble() * $$8.getXsize(), $$6.nextDouble() * $$8.getYsize(), $$6.nextDouble() * $$8.getZsize());
            Vec3 $$10 = Vec3.atLowerCornerOf(this.getBlockPos()).add($$6.nextDouble(), $$6.nextDouble(), $$6.nextDouble());
            if ($$2) {
               Vec3 $$11 = $$9;
               $$9 = $$10;
               $$10 = $$11;
            }

            TrailParticleOption $$12 = new TrailParticleOption($$10, $$5, $$6.nextInt(40) + 10);
            $$0.sendParticles($$12, true, true, $$9.x, $$9.y, $$9.z, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   @Override
   public void preRemoveSideEffects(BlockPos $$0, BlockState $$1) {
      this.removeProtector(null);
   }

   public void removeProtector(DamageSource $$0) {
      if (this.getCreakingProtector().orElse(null) instanceof Creaking $$1) {
         if ($$0 == null) {
            $$1.tearDown();
         } else {
            $$1.creakingDeathEffects($$0);
            $$1.setTearingDown();
            $$1.setHealth(0.0F);
         }

         this.clearCreakingInfo();
      }
   }

   public boolean isProtector(Creaking $$0) {
      return this.getCreakingProtector().map($$1 -> $$1 == $$0).orElse(false);
   }

   public int getAnalogOutputSignal() {
      return this.outputSignal;
   }

   public int computeAnalogOutputSignal() {
      if (this.creakingInfo != null && !this.getCreakingProtector().isEmpty()) {
         double $$0 = this.distanceToCreaking();
         double $$1 = Math.clamp($$0, 0.0, 32.0) / 32.0;
         return 15 - (int)Math.floor($$1 * 15.0);
      } else {
         return 0;
      }
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      $$0.<UUID>read("creaking", UUIDUtil.CODEC).ifPresentOrElse(this::setCreakingInfo, this::clearCreakingInfo);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (this.creakingInfo != null) {
         $$0.store("creaking", UUIDUtil.CODEC, (UUID)this.creakingInfo.map(Entity::getUUID, $$0x -> $$0x));
      }
   }
}
