package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FallingBlockEntity extends net.minecraft.world.entity.Entity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final BlockState DEFAULT_BLOCK_STATE = Blocks.SAND.defaultBlockState();
   private static final int DEFAULT_TIME = 0;
   private static final float DEFAULT_FALL_DAMAGE_PER_DISTANCE = 0.0F;
   private static final int DEFAULT_MAX_FALL_DAMAGE = 40;
   private static final boolean DEFAULT_DROP_ITEM = true;
   private static final boolean DEFAULT_CANCEL_DROP = false;
   private BlockState blockState = DEFAULT_BLOCK_STATE;
   public int time = 0;
   public boolean dropItem = true;
   private boolean cancelDrop = false;
   private boolean hurtEntities;
   private int fallDamageMax = 40;
   private float fallDamagePerDistance = 0.0F;
   @Nullable
   public CompoundTag blockData;
   public boolean forceTickAfterTeleportToDuplicate;
   protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

   public FallingBlockEntity(net.minecraft.world.entity.EntityType<? extends FallingBlockEntity> $$0, Level $$1) {
      super($$0, $$1);
   }

   private FallingBlockEntity(Level $$0, double $$1, double $$2, double $$3, BlockState $$4) {
      this(net.minecraft.world.entity.EntityType.FALLING_BLOCK, $$0);
      this.blockState = $$4;
      this.blocksBuilding = true;
      this.setPos($$1, $$2, $$3);
      this.setDeltaMovement(Vec3.ZERO);
      this.xo = $$1;
      this.yo = $$2;
      this.zo = $$3;
      this.setStartPos(this.blockPosition());
   }

   public static FallingBlockEntity fall(Level $$0, BlockPos $$1, BlockState $$2) {
      FallingBlockEntity $$3 = new FallingBlockEntity(
         $$0,
         $$1.getX() + 0.5,
         $$1.getY(),
         $$1.getZ() + 0.5,
         $$2.hasProperty(BlockStateProperties.WATERLOGGED) ? (BlockState)$$2.setValue(BlockStateProperties.WATERLOGGED, false) : $$2
      );
      $$0.setBlock($$1, $$2.getFluidState().createLegacyBlock(), 3);
      $$0.addFreshEntity($$3);
      return $$3;
   }

   @Override
   public boolean isAttackable() {
      return false;
   }

   @Override
   public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (!this.isInvulnerableToBase($$1)) {
         this.markHurt();
      }

      return false;
   }

   public void setStartPos(BlockPos $$0) {
      this.entityData.set(DATA_START_POS, $$0);
   }

   public BlockPos getStartPos() {
      return (BlockPos)this.entityData.get(DATA_START_POS);
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.NONE;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(DATA_START_POS, BlockPos.ZERO);
   }

   @Override
   public boolean isPickable() {
      return !this.isRemoved();
   }

   @Override
   protected double getDefaultGravity() {
      return 0.04;
   }

   @Override
   public void tick() {
      if (this.blockState.isAir()) {
         this.discard();
      } else {
         Block $$0 = this.blockState.getBlock();
         this.time++;
         this.applyGravity();
         this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
         this.applyEffectsFromBlocks();
         this.handlePortal();
         if (this.level() instanceof ServerLevel $$1 && (this.isAlive() || this.forceTickAfterTeleportToDuplicate)) {
            BlockPos $$2 = this.blockPosition();
            boolean $$3 = this.blockState.getBlock() instanceof ConcretePowderBlock;
            boolean $$4 = $$3 && this.level().getFluidState($$2).is(FluidTags.WATER);
            double $$5 = this.getDeltaMovement().lengthSqr();
            if ($$3 && $$5 > 1.0) {
               BlockHitResult $$6 = this.level()
                  .clip(
                     new ClipContext(
                        new Vec3(this.xo, this.yo, this.zo), this.position(), net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.SOURCE_ONLY, this
                     )
                  );
               if ($$6.getType() != Type.MISS && this.level().getFluidState($$6.getBlockPos()).is(FluidTags.WATER)) {
                  $$2 = $$6.getBlockPos();
                  $$4 = true;
               }
            }

            if (!this.onGround() && !$$4) {
               if (this.time > 100 && ($$2.getY() <= this.level().getMinY() || $$2.getY() > this.level().getMaxY()) || this.time > 600) {
                  if (this.dropItem && (Boolean)$$1.getGameRules().get(GameRules.ENTITY_DROPS)) {
                     this.spawnAtLocation($$1, $$0);
                  }

                  this.discard();
               }
            } else {
               BlockState $$7 = this.level().getBlockState($$2);
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
               if (!$$7.is(Blocks.MOVING_PISTON)) {
                  if (!this.cancelDrop) {
                     boolean $$8 = $$7.canBeReplaced(new DirectionalPlaceContext(this.level(), $$2, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                     boolean $$9 = FallingBlock.isFree(this.level().getBlockState($$2.below())) && (!$$3 || !$$4);
                     boolean $$10 = this.blockState.canSurvive(this.level(), $$2) && !$$9;
                     if ($$8 && $$10) {
                        if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState($$2).getType() == Fluids.WATER) {
                           this.blockState = (BlockState)this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                        }

                        if (this.level().setBlock($$2, this.blockState, 3)) {
                           $$1.getChunkSource().chunkMap.sendToTrackingPlayers(this, new ClientboundBlockUpdatePacket($$2, this.level().getBlockState($$2)));
                           this.discard();
                           if ($$0 instanceof Fallable $$11) {
                              $$11.onLand(this.level(), $$2, this.blockState, $$7, this);
                           }

                           if (this.blockData != null && this.blockState.hasBlockEntity()) {
                              BlockEntity $$12 = this.level().getBlockEntity($$2);
                              if ($$12 != null) {
                                 try {
                                    ScopedCollector $$13 = new ScopedCollector($$12.problemPath(), LOGGER);

                                    try {
                                       RegistryAccess $$14 = this.level().registryAccess();
                                       TagValueOutput $$15 = TagValueOutput.createWithContext($$13, $$14);
                                       $$12.saveWithoutMetadata($$15);
                                       CompoundTag $$16 = $$15.buildResult();
                                       this.blockData.forEach(($$1x, $$2x) -> $$16.put($$1x, $$2x.copy()));
                                       $$12.loadWithComponents(TagValueInput.create($$13, $$14, $$16));
                                    } catch (Throwable var18) {
                                       try {
                                          $$13.close();
                                       } catch (Throwable var17) {
                                          var18.addSuppressed(var17);
                                       }

                                       throw var18;
                                    }

                                    $$13.close();
                                 } catch (Exception var19) {
                                    LOGGER.error("Failed to load block entity from falling block", var19);
                                 }

                                 $$12.setChanged();
                              }
                           }
                        } else if (this.dropItem && (Boolean)$$1.getGameRules().get(GameRules.ENTITY_DROPS)) {
                           this.discard();
                           this.callOnBrokenAfterFall($$0, $$2);
                           this.spawnAtLocation($$1, $$0);
                        }
                     } else {
                        this.discard();
                        if (this.dropItem && (Boolean)$$1.getGameRules().get(GameRules.ENTITY_DROPS)) {
                           this.callOnBrokenAfterFall($$0, $$2);
                           this.spawnAtLocation($$1, $$0);
                        }
                     }
                  } else {
                     this.discard();
                     this.callOnBrokenAfterFall($$0, $$2);
                  }
               }
            }
         }

         this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
      }
   }

   public void callOnBrokenAfterFall(Block $$0, BlockPos $$1) {
      if ($$0 instanceof Fallable) {
         ((Fallable)$$0).onBrokenAfterFall(this.level(), $$1, this);
      }
   }

   @Override
   public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
      if (!this.hurtEntities) {
         return false;
      } else {
         int $$3 = Mth.ceil($$0 - 1.0);
         if ($$3 < 0) {
            return false;
         } else {
            Predicate<net.minecraft.world.entity.Entity> $$4 = net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR
               .and(net.minecraft.world.entity.EntitySelector.LIVING_ENTITY_STILL_ALIVE);
            DamageSource $$6 = this.blockState.getBlock() instanceof Fallable $$5 ? $$5.getFallDamageSource(this) : this.damageSources().fallingBlock(this);
            float $$7 = Math.min(Mth.floor($$3 * this.fallDamagePerDistance), this.fallDamageMax);
            this.level().getEntities(this, this.getBoundingBox(), $$4).forEach($$2x -> $$2x.hurt($$6, $$7));
            boolean $$8 = this.blockState.is(BlockTags.ANVIL);
            if ($$8 && $$7 > 0.0F && this.random.nextFloat() < 0.05F + $$3 * 0.05F) {
               BlockState $$9 = AnvilBlock.damage(this.blockState);
               if ($$9 == null) {
                  this.cancelDrop = true;
               } else {
                  this.blockState = $$9;
               }
            }

            return false;
         }
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      $$0.store("BlockState", BlockState.CODEC, this.blockState);
      $$0.putInt("Time", this.time);
      $$0.putBoolean("DropItem", this.dropItem);
      $$0.putBoolean("HurtEntities", this.hurtEntities);
      $$0.putFloat("FallHurtAmount", this.fallDamagePerDistance);
      $$0.putInt("FallHurtMax", this.fallDamageMax);
      if (this.blockData != null) {
         $$0.store("TileEntityData", CompoundTag.CODEC, this.blockData);
      }

      $$0.putBoolean("CancelDrop", this.cancelDrop);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.blockState = $$0.read("BlockState", BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE);
      this.time = $$0.getIntOr("Time", 0);
      boolean $$1 = this.blockState.is(BlockTags.ANVIL);
      this.hurtEntities = $$0.getBooleanOr("HurtEntities", $$1);
      this.fallDamagePerDistance = $$0.getFloatOr("FallHurtAmount", 0.0F);
      this.fallDamageMax = $$0.getIntOr("FallHurtMax", 40);
      this.dropItem = $$0.getBooleanOr("DropItem", true);
      this.blockData = (CompoundTag)$$0.read("TileEntityData", CompoundTag.CODEC).orElse(null);
      this.cancelDrop = $$0.getBooleanOr("CancelDrop", false);
   }

   public void setHurtsEntities(float $$0, int $$1) {
      this.hurtEntities = true;
      this.fallDamagePerDistance = $$0;
      this.fallDamageMax = $$1;
   }

   public void disableDrop() {
      this.cancelDrop = true;
   }

   @Override
   public boolean displayFireAnimation() {
      return false;
   }

   @Override
   public void fillCrashReportCategory(CrashReportCategory $$0) {
      super.fillCrashReportCategory($$0);
      $$0.setDetail("Immitating BlockState", this.blockState.toString());
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   @Override
   protected Component getTypeName() {
      return Component.translatable("entity.minecraft.falling_block_type", new Object[]{this.blockState.getBlock().getName()});
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
      return new ClientboundAddEntityPacket(this, $$0, Block.getId(this.getBlockState()));
   }

   @Override
   public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
      super.recreateFromPacket($$0);
      this.blockState = Block.stateById($$0.getData());
      this.blocksBuilding = true;
      double $$1 = $$0.getX();
      double $$2 = $$0.getY();
      double $$3 = $$0.getZ();
      this.setPos($$1, $$2, $$3);
      this.setStartPos(this.blockPosition());
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.Entity teleport(TeleportTransition $$0) {
      ResourceKey<Level> $$1 = $$0.newLevel().dimension();
      ResourceKey<Level> $$2 = this.level().dimension();
      boolean $$3 = ($$2 == Level.END || $$1 == Level.END) && $$2 != $$1;
      net.minecraft.world.entity.Entity $$4 = super.teleport($$0);
      this.forceTickAfterTeleportToDuplicate = $$4 != null && $$3;
      return $$4;
   }
}
