package net.minecraft.world.entity.boss.enderdragon;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class EnderDragon extends net.minecraft.world.entity.Mob implements Enemy {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderDragon.class, EntityDataSerializers.INT);
   private static final TargetingConditions CRYSTAL_DESTROY_TARGETING = TargetingConditions.forCombat().range(64.0);
   private static final int GROWL_INTERVAL_MIN = 200;
   private static final int GROWL_INTERVAL_MAX = 400;
   private static final float SITTING_ALLOWED_DAMAGE_PERCENTAGE = 0.25F;
   private static final String DRAGON_DEATH_TIME_KEY = "DragonDeathTime";
   private static final String DRAGON_PHASE_KEY = "DragonPhase";
   private static final int DEFAULT_DEATH_TIME = 0;
   public final DragonFlightHistory flightHistory = new DragonFlightHistory();
   private final EnderDragonPart[] subEntities;
   public final EnderDragonPart head;
   private final EnderDragonPart neck;
   private final EnderDragonPart body;
   private final EnderDragonPart tail1;
   private final EnderDragonPart tail2;
   private final EnderDragonPart tail3;
   private final EnderDragonPart wing1;
   private final EnderDragonPart wing2;
   public float oFlapTime;
   public float flapTime;
   public boolean inWall;
   public int dragonDeathTime = 0;
   public float yRotA;
   @Nullable
   public EndCrystal nearestCrystal;
   @Nullable
   private EndDragonFight dragonFight;
   private BlockPos fightOrigin = BlockPos.ZERO;
   private final EnderDragonPhaseManager phaseManager;
   private int growlTime = 100;
   private float sittingDamageReceived;
   private final Node[] nodes = new Node[24];
   private final int[] nodeAdjacency = new int[24];
   private final BinaryHeap openSet = new BinaryHeap();

   public EnderDragon(net.minecraft.world.entity.EntityType<? extends EnderDragon> $$0, Level $$1) {
      super(net.minecraft.world.entity.EntityType.ENDER_DRAGON, $$1);
      this.head = new EnderDragonPart(this, "head", 1.0F, 1.0F);
      this.neck = new EnderDragonPart(this, "neck", 3.0F, 3.0F);
      this.body = new EnderDragonPart(this, "body", 5.0F, 3.0F);
      this.tail1 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.tail2 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.tail3 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.wing1 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
      this.wing2 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
      this.subEntities = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
      this.setHealth(this.getMaxHealth());
      this.noPhysics = true;
      this.phaseManager = new EnderDragonPhaseManager(this);
   }

   public void setDragonFight(EndDragonFight $$0) {
      this.dragonFight = $$0;
   }

   public void setFightOrigin(BlockPos $$0) {
      this.fightOrigin = $$0;
   }

   public BlockPos getFightOrigin() {
      return this.fightOrigin;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.CAMERA_DISTANCE, 16.0);
   }

   @Override
   public boolean isFlapping() {
      float $$0 = Mth.cos(this.flapTime * (float) (Math.PI * 2));
      float $$1 = Mth.cos(this.oFlapTime * (float) (Math.PI * 2));
      return $$1 <= -0.3F && $$0 >= -0.3F;
   }

   @Override
   public void onFlap() {
      if (this.level().isClientSide() && !this.isSilent()) {
         this.level()
            .playLocalSound(
               this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false
            );
      }
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_PHASE, EnderDragonPhase.HOVERING.getId());
   }

   @Override
   public void aiStep() {
      this.processFlappingMovement();
      if (this.level().isClientSide()) {
         this.setHealth(this.getHealth());
         if (!this.isSilent() && !this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
            this.level()
               .playLocalSound(
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  SoundEvents.ENDER_DRAGON_GROWL,
                  this.getSoundSource(),
                  2.5F,
                  0.8F + this.random.nextFloat() * 0.3F,
                  false
               );
            this.growlTime = 200 + this.random.nextInt(200);
         }
      }

      if (this.dragonFight == null && this.level() instanceof ServerLevel $$0) {
         EndDragonFight $$1 = $$0.getDragonFight();
         if ($$1 != null && this.getUUID().equals($$1.getDragonUUID())) {
            this.dragonFight = $$1;
         }
      }

      this.oFlapTime = this.flapTime;
      if (this.isDeadOrDying()) {
         float $$2 = (this.random.nextFloat() - 0.5F) * 8.0F;
         float $$3 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float $$4 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level().addParticle(ParticleTypes.EXPLOSION, this.getX() + $$2, this.getY() + 2.0 + $$3, this.getZ() + $$4, 0.0, 0.0, 0.0);
      } else {
         this.checkCrystals();
         Vec3 $$5 = this.getDeltaMovement();
         float $$6 = 0.2F / ((float)$$5.horizontalDistance() * 10.0F + 1.0F);
         $$6 *= (float)Math.pow(2.0, $$5.y);
         if (this.phaseManager.getCurrentPhase().isSitting()) {
            this.flapTime += 0.1F;
         } else if (this.inWall) {
            this.flapTime += $$6 * 0.5F;
         } else {
            this.flapTime += $$6;
         }

         this.setYRot(Mth.wrapDegrees(this.getYRot()));
         if (this.isNoAi()) {
            this.flapTime = 0.5F;
         } else {
            this.flightHistory.record(this.getY(), this.getYRot());
            if (this.level() instanceof ServerLevel $$7) {
               DragonPhaseInstance $$9 = this.phaseManager.getCurrentPhase();
               $$9.doServerTick($$7);
               if (this.phaseManager.getCurrentPhase() != $$9) {
                  $$9 = this.phaseManager.getCurrentPhase();
                  $$9.doServerTick($$7);
               }

               Vec3 $$10 = $$9.getFlyTargetLocation();
               if ($$10 != null) {
                  double $$11 = $$10.x - this.getX();
                  double $$12 = $$10.y - this.getY();
                  double $$13 = $$10.z - this.getZ();
                  double $$14 = $$11 * $$11 + $$12 * $$12 + $$13 * $$13;
                  float $$15 = $$9.getFlySpeed();
                  double $$16 = Math.sqrt($$11 * $$11 + $$13 * $$13);
                  if ($$16 > 0.0) {
                     $$12 = Mth.clamp($$12 / $$16, -$$15, $$15);
                  }

                  this.setDeltaMovement(this.getDeltaMovement().add(0.0, $$12 * 0.01, 0.0));
                  this.setYRot(Mth.wrapDegrees(this.getYRot()));
                  Vec3 $$17 = $$10.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                  Vec3 $$18 = new Vec3(
                        Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)), this.getDeltaMovement().y, -Mth.cos(this.getYRot() * (float) (Math.PI / 180.0))
                     )
                     .normalize();
                  float $$19 = Math.max(((float)$$18.dot($$17) + 0.5F) / 1.5F, 0.0F);
                  if (Math.abs($$11) > 1.0E-5F || Math.abs($$13) > 1.0E-5F) {
                     float $$20 = Mth.clamp(Mth.wrapDegrees(180.0F - (float)Mth.atan2($$11, $$13) * (180.0F / (float)Math.PI) - this.getYRot()), -50.0F, 50.0F);
                     this.yRotA *= 0.8F;
                     this.yRotA = this.yRotA + $$20 * $$9.getTurnSpeed();
                     this.setYRot(this.getYRot() + this.yRotA * 0.1F);
                  }

                  float $$21 = (float)(2.0 / ($$14 + 1.0));
                  float $$22 = 0.06F;
                  this.moveRelative(0.06F * ($$19 * $$21 + (1.0F - $$21)), new Vec3(0.0, 0.0, -1.0));
                  if (this.inWall) {
                     this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement().scale(0.8F));
                  } else {
                     this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
                  }

                  Vec3 $$23 = this.getDeltaMovement().normalize();
                  double $$24 = 0.8 + 0.15 * ($$23.dot($$18) + 1.0) / 2.0;
                  this.setDeltaMovement(this.getDeltaMovement().multiply($$24, 0.91F, $$24));
               }
            } else {
               this.interpolation.interpolate();
               this.phaseManager.getCurrentPhase().doClientTick();
            }

            if (!this.level().isClientSide()) {
               this.applyEffectsFromBlocks();
            }

            this.yBodyRot = this.getYRot();
            Vec3[] $$25 = new Vec3[this.subEntities.length];

            for (int $$26 = 0; $$26 < this.subEntities.length; $$26++) {
               $$25[$$26] = new Vec3(this.subEntities[$$26].getX(), this.subEntities[$$26].getY(), this.subEntities[$$26].getZ());
            }

            float $$27 = (float)(this.flightHistory.get(5).y() - this.flightHistory.get(10).y()) * 10.0F * (float) (Math.PI / 180.0);
            float $$28 = Mth.cos($$27);
            float $$29 = Mth.sin($$27);
            float $$30 = this.getYRot() * (float) (Math.PI / 180.0);
            float $$31 = Mth.sin($$30);
            float $$32 = Mth.cos($$30);
            this.tickPart(this.body, $$31 * 0.5F, 0.0, -$$32 * 0.5F);
            this.tickPart(this.wing1, $$32 * 4.5F, 2.0, $$31 * 4.5F);
            this.tickPart(this.wing2, $$32 * -4.5F, 2.0, $$31 * -4.5F);
            if (this.level() instanceof ServerLevel $$33 && this.hurtTime == 0) {
               this.knockBack(
                  $$33,
                  $$33.getEntities(
                     this,
                     this.wing1.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0),
                     net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR
                  )
               );
               this.knockBack(
                  $$33,
                  $$33.getEntities(
                     this,
                     this.wing2.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0),
                     net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR
                  )
               );
               this.hurt(
                  $$33, $$33.getEntities(this, this.head.getBoundingBox().inflate(1.0), net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR)
               );
               this.hurt(
                  $$33, $$33.getEntities(this, this.neck.getBoundingBox().inflate(1.0), net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR)
               );
            }

            float $$34 = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0) - this.yRotA * 0.01F);
            float $$35 = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0) - this.yRotA * 0.01F);
            float $$36 = this.getHeadYOffset();
            this.tickPart(this.head, $$34 * 6.5F * $$28, $$36 + $$29 * 6.5F, -$$35 * 6.5F * $$28);
            this.tickPart(this.neck, $$34 * 5.5F * $$28, $$36 + $$29 * 5.5F, -$$35 * 5.5F * $$28);
            DragonFlightHistory.Sample $$37 = this.flightHistory.get(5);

            for (int $$38 = 0; $$38 < 3; $$38++) {
               EnderDragonPart $$39 = null;
               if ($$38 == 0) {
                  $$39 = this.tail1;
               }

               if ($$38 == 1) {
                  $$39 = this.tail2;
               }

               if ($$38 == 2) {
                  $$39 = this.tail3;
               }

               DragonFlightHistory.Sample $$40 = this.flightHistory.get(12 + $$38 * 2);
               float $$41 = this.getYRot() * (float) (Math.PI / 180.0) + this.rotWrap($$40.yRot() - $$37.yRot()) * (float) (Math.PI / 180.0);
               float $$42 = Mth.sin($$41);
               float $$43 = Mth.cos($$41);
               float $$44 = 1.5F;
               float $$45 = ($$38 + 1) * 2.0F;
               this.tickPart($$39, -($$31 * 1.5F + $$42 * $$45) * $$28, $$40.y() - $$37.y() - ($$45 + 1.5F) * $$29 + 1.5, ($$32 * 1.5F + $$43 * $$45) * $$28);
            }

            if (this.level() instanceof ServerLevel $$46) {
               this.inWall = this.checkWalls($$46, this.head.getBoundingBox())
                  | this.checkWalls($$46, this.neck.getBoundingBox())
                  | this.checkWalls($$46, this.body.getBoundingBox());
               if (this.dragonFight != null) {
                  this.dragonFight.updateDragon(this);
               }
            }

            for (int $$47 = 0; $$47 < this.subEntities.length; $$47++) {
               this.subEntities[$$47].xo = $$25[$$47].x;
               this.subEntities[$$47].yo = $$25[$$47].y;
               this.subEntities[$$47].zo = $$25[$$47].z;
               this.subEntities[$$47].xOld = $$25[$$47].x;
               this.subEntities[$$47].yOld = $$25[$$47].y;
               this.subEntities[$$47].zOld = $$25[$$47].z;
            }
         }
      }
   }

   private void tickPart(EnderDragonPart $$0, double $$1, double $$2, double $$3) {
      $$0.setPos(this.getX() + $$1, this.getY() + $$2, this.getZ() + $$3);
   }

   private float getHeadYOffset() {
      if (this.phaseManager.getCurrentPhase().isSitting()) {
         return -1.0F;
      } else {
         DragonFlightHistory.Sample $$0 = this.flightHistory.get(5);
         DragonFlightHistory.Sample $$1 = this.flightHistory.get(0);
         return (float)($$0.y() - $$1.y());
      }
   }

   private void checkCrystals() {
      if (this.nearestCrystal != null) {
         if (this.nearestCrystal.isRemoved()) {
            this.nearestCrystal = null;
         } else if (this.tickCount % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getHealth() + 1.0F);
         }
      }

      if (this.random.nextInt(10) == 0) {
         List<EndCrystal> $$0 = this.level().getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0));
         EndCrystal $$1 = null;
         double $$2 = Double.MAX_VALUE;

         for (EndCrystal $$3 : $$0) {
            double $$4 = $$3.distanceToSqr(this);
            if ($$4 < $$2) {
               $$2 = $$4;
               $$1 = $$3;
            }
         }

         this.nearestCrystal = $$1;
      }
   }

   private void knockBack(ServerLevel $$0, List<net.minecraft.world.entity.Entity> $$1) {
      double $$2 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0;
      double $$3 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0;

      for (net.minecraft.world.entity.Entity $$4 : $$1) {
         if ($$4 instanceof net.minecraft.world.entity.LivingEntity $$5) {
            double $$6 = $$4.getX() - $$2;
            double $$7 = $$4.getZ() - $$3;
            double $$8 = Math.max($$6 * $$6 + $$7 * $$7, 0.1);
            $$4.push($$6 / $$8 * 4.0, 0.2F, $$7 / $$8 * 4.0);
            if (!this.phaseManager.getCurrentPhase().isSitting() && $$5.getLastHurtByMobTimestamp() < $$4.tickCount - 2) {
               DamageSource $$9 = this.damageSources().mobAttack(this);
               $$4.hurtServer($$0, $$9, 5.0F);
               EnchantmentHelper.doPostAttackEffects($$0, $$4, $$9);
            }
         }
      }
   }

   private void hurt(ServerLevel $$0, List<net.minecraft.world.entity.Entity> $$1) {
      for (net.minecraft.world.entity.Entity $$2 : $$1) {
         if ($$2 instanceof net.minecraft.world.entity.LivingEntity) {
            DamageSource $$3 = this.damageSources().mobAttack(this);
            $$2.hurtServer($$0, $$3, 10.0F);
            EnchantmentHelper.doPostAttackEffects($$0, $$2, $$3);
         }
      }
   }

   private float rotWrap(double $$0) {
      return (float)Mth.wrapDegrees($$0);
   }

   private boolean checkWalls(ServerLevel $$0, AABB $$1) {
      int $$2 = Mth.floor($$1.minX);
      int $$3 = Mth.floor($$1.minY);
      int $$4 = Mth.floor($$1.minZ);
      int $$5 = Mth.floor($$1.maxX);
      int $$6 = Mth.floor($$1.maxY);
      int $$7 = Mth.floor($$1.maxZ);
      boolean $$8 = false;
      boolean $$9 = false;

      for (int $$10 = $$2; $$10 <= $$5; $$10++) {
         for (int $$11 = $$3; $$11 <= $$6; $$11++) {
            for (int $$12 = $$4; $$12 <= $$7; $$12++) {
               BlockPos $$13 = new BlockPos($$10, $$11, $$12);
               BlockState $$14 = $$0.getBlockState($$13);
               if (!$$14.isAir() && !$$14.is(BlockTags.DRAGON_TRANSPARENT)) {
                  if ((Boolean)$$0.getGameRules().get(GameRules.MOB_GRIEFING) && !$$14.is(BlockTags.DRAGON_IMMUNE)) {
                     $$9 = $$0.removeBlock($$13, false) || $$9;
                  } else {
                     $$8 = true;
                  }
               }
            }
         }
      }

      if ($$9) {
         BlockPos $$15 = new BlockPos(
            $$2 + this.random.nextInt($$5 - $$2 + 1), $$3 + this.random.nextInt($$6 - $$3 + 1), $$4 + this.random.nextInt($$7 - $$4 + 1)
         );
         $$0.levelEvent(2008, $$15, 0);
      }

      return $$8;
   }

   public boolean hurt(ServerLevel $$0, EnderDragonPart $$1, DamageSource $$2, float $$3) {
      if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
         return false;
      } else {
         $$3 = this.phaseManager.getCurrentPhase().onHurt($$2, $$3);
         if ($$1 != this.head) {
            $$3 = $$3 / 4.0F + Math.min($$3, 1.0F);
         }

         if ($$3 < 0.01F) {
            return false;
         } else {
            if ($$2.getEntity() instanceof Player || $$2.is(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) {
               float $$4 = this.getHealth();
               this.reallyHurt($$0, $$2, $$3);
               if (this.isDeadOrDying() && !this.phaseManager.getCurrentPhase().isSitting()) {
                  this.setHealth(1.0F);
                  this.phaseManager.setPhase(EnderDragonPhase.DYING);
               }

               if (this.phaseManager.getCurrentPhase().isSitting()) {
                  this.sittingDamageReceived = this.sittingDamageReceived + $$4 - this.getHealth();
                  if (this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
                     this.sittingDamageReceived = 0.0F;
                     this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
                  }
               }
            }

            return true;
         }
      }
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      return this.hurt($$0, this.body, $$1, $$2);
   }

   protected void reallyHurt(ServerLevel $$0, DamageSource $$1, float $$2) {
      super.hurtServer($$0, $$1, $$2);
   }

   @Override
   public void kill(ServerLevel $$0) {
      this.remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED);
      this.gameEvent(GameEvent.ENTITY_DIE);
      if (this.dragonFight != null) {
         this.dragonFight.updateDragon(this);
         this.dragonFight.setDragonKilled(this);
      }
   }

   @Override
   protected void tickDeath() {
      if (this.dragonFight != null) {
         this.dragonFight.updateDragon(this);
      }

      this.dragonDeathTime++;
      if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
         float $$0 = (this.random.nextFloat() - 0.5F) * 8.0F;
         float $$1 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float $$2 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + $$0, this.getY() + 2.0 + $$1, this.getZ() + $$2, 0.0, 0.0, 0.0);
      }

      int $$3 = 500;
      if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
         $$3 = 12000;
      }

      if (this.level() instanceof ServerLevel $$4) {
         if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && (Boolean)$$4.getGameRules().get(GameRules.MOB_DROPS)) {
            net.minecraft.world.entity.ExperienceOrb.award($$4, this.position(), Mth.floor($$3 * 0.08F));
         }

         if (this.dragonDeathTime == 1 && !this.isSilent()) {
            $$4.globalLevelEvent(1028, this.blockPosition(), 0);
         }
      }

      Vec3 $$5 = new Vec3(0.0, 0.1F, 0.0);
      this.move(net.minecraft.world.entity.MoverType.SELF, $$5);

      for (EnderDragonPart $$6 : this.subEntities) {
         $$6.setOldPosAndRot();
         $$6.setPos($$6.position().add($$5));
      }

      if (this.dragonDeathTime == 200 && this.level() instanceof ServerLevel $$7) {
         if ((Boolean)$$7.getGameRules().get(GameRules.MOB_DROPS)) {
            net.minecraft.world.entity.ExperienceOrb.award($$7, this.position(), Mth.floor($$3 * 0.2F));
         }

         if (this.dragonFight != null) {
            this.dragonFight.setDragonKilled(this);
         }

         this.remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED);
         this.gameEvent(GameEvent.ENTITY_DIE);
      }
   }

   public int findClosestNode() {
      if (this.nodes[0] == null) {
         for (int $$0 = 0; $$0 < 24; $$0++) {
            int $$1 = 5;
            int $$3;
            int $$4;
            if ($$0 < 12) {
               $$3 = Mth.floor(60.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 12) * $$0)));
               $$4 = Mth.floor(60.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 12) * $$0)));
            } else if ($$0 < 20) {
               int $$2 = $$0 - 12;
               $$3 = Mth.floor(40.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 8) * $$2)));
               $$4 = Mth.floor(40.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 8) * $$2)));
               $$1 += 10;
            } else {
               int var7 = $$0 - 20;
               $$3 = Mth.floor(20.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 4) * var7)));
               $$4 = Mth.floor(20.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 4) * var7)));
            }

            int $$9 = Math.max(73, this.level().getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos($$3, 0, $$4)).getY() + $$1);
            this.nodes[$$0] = new Node($$3, $$9, $$4);
         }

         this.nodeAdjacency[0] = 6146;
         this.nodeAdjacency[1] = 8197;
         this.nodeAdjacency[2] = 8202;
         this.nodeAdjacency[3] = 16404;
         this.nodeAdjacency[4] = 32808;
         this.nodeAdjacency[5] = 32848;
         this.nodeAdjacency[6] = 65696;
         this.nodeAdjacency[7] = 131392;
         this.nodeAdjacency[8] = 131712;
         this.nodeAdjacency[9] = 263424;
         this.nodeAdjacency[10] = 526848;
         this.nodeAdjacency[11] = 525313;
         this.nodeAdjacency[12] = 1581057;
         this.nodeAdjacency[13] = 3166214;
         this.nodeAdjacency[14] = 2138120;
         this.nodeAdjacency[15] = 6373424;
         this.nodeAdjacency[16] = 4358208;
         this.nodeAdjacency[17] = 12910976;
         this.nodeAdjacency[18] = 9044480;
         this.nodeAdjacency[19] = 9706496;
         this.nodeAdjacency[20] = 15216640;
         this.nodeAdjacency[21] = 13688832;
         this.nodeAdjacency[22] = 11763712;
         this.nodeAdjacency[23] = 8257536;
      }

      return this.findClosestNode(this.getX(), this.getY(), this.getZ());
   }

   public int findClosestNode(double $$0, double $$1, double $$2) {
      float $$3 = 10000.0F;
      int $$4 = 0;
      Node $$5 = new Node(Mth.floor($$0), Mth.floor($$1), Mth.floor($$2));
      int $$6 = 0;
      if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
         $$6 = 12;
      }

      for (int $$7 = $$6; $$7 < 24; $$7++) {
         if (this.nodes[$$7] != null) {
            float $$8 = this.nodes[$$7].distanceToSqr($$5);
            if ($$8 < $$3) {
               $$3 = $$8;
               $$4 = $$7;
            }
         }
      }

      return $$4;
   }

   @Nullable
   public Path findPath(int $$0, int $$1, @Nullable Node $$2) {
      for (int $$3 = 0; $$3 < 24; $$3++) {
         Node $$4 = this.nodes[$$3];
         $$4.closed = false;
         $$4.f = 0.0F;
         $$4.g = 0.0F;
         $$4.h = 0.0F;
         $$4.cameFrom = null;
         $$4.heapIdx = -1;
      }

      Node $$5 = this.nodes[$$0];
      Node $$6 = this.nodes[$$1];
      $$5.g = 0.0F;
      $$5.h = $$5.distanceTo($$6);
      $$5.f = $$5.h;
      this.openSet.clear();
      this.openSet.insert($$5);
      Node $$7 = $$5;
      int $$8 = 0;
      if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
         $$8 = 12;
      }

      while (!this.openSet.isEmpty()) {
         Node $$9 = this.openSet.pop();
         if ($$9.equals($$6)) {
            if ($$2 != null) {
               $$2.cameFrom = $$6;
               $$6 = $$2;
            }

            return this.reconstructPath($$5, $$6);
         }

         if ($$9.distanceTo($$6) < $$7.distanceTo($$6)) {
            $$7 = $$9;
         }

         $$9.closed = true;
         int $$10 = 0;

         for (int $$11 = 0; $$11 < 24; $$11++) {
            if (this.nodes[$$11] == $$9) {
               $$10 = $$11;
               break;
            }
         }

         for (int $$12 = $$8; $$12 < 24; $$12++) {
            if ((this.nodeAdjacency[$$10] & 1 << $$12) > 0) {
               Node $$13 = this.nodes[$$12];
               if (!$$13.closed) {
                  float $$14 = $$9.g + $$9.distanceTo($$13);
                  if (!$$13.inOpenSet() || $$14 < $$13.g) {
                     $$13.cameFrom = $$9;
                     $$13.g = $$14;
                     $$13.h = $$13.distanceTo($$6);
                     if ($$13.inOpenSet()) {
                        this.openSet.changeCost($$13, $$13.g + $$13.h);
                     } else {
                        $$13.f = $$13.g + $$13.h;
                        this.openSet.insert($$13);
                     }
                  }
               }
            }
         }
      }

      if ($$7 == $$5) {
         return null;
      } else {
         LOGGER.debug("Failed to find path from {} to {}", $$0, $$1);
         if ($$2 != null) {
            $$2.cameFrom = $$7;
            $$7 = $$2;
         }

         return this.reconstructPath($$5, $$7);
      }
   }

   private Path reconstructPath(Node $$0, Node $$1) {
      List<Node> $$2 = Lists.newArrayList();
      Node $$3 = $$1;
      $$2.add(0, $$1);

      while ($$3.cameFrom != null) {
         $$3 = $$3.cameFrom;
         $$2.add(0, $$3);
      }

      return new Path($$2, new BlockPos($$1.x, $$1.y, $$1.z), true);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
      $$0.putInt("DragonDeathTime", this.dragonDeathTime);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      $$0.getInt("DragonPhase").ifPresent($$0x -> this.phaseManager.setPhase(EnderDragonPhase.getById($$0x)));
      this.dragonDeathTime = $$0.getIntOr("DragonDeathTime", 0);
   }

   @Override
   public void checkDespawn() {
   }

   public EnderDragonPart[] getSubEntities() {
      return this.subEntities;
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENDER_DRAGON_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.ENDER_DRAGON_HURT;
   }

   @Override
   protected float getSoundVolume() {
      return 5.0F;
   }

   public Vec3 getHeadLookVector(float $$0) {
      DragonPhaseInstance $$1 = this.phaseManager.getCurrentPhase();
      EnderDragonPhase<? extends DragonPhaseInstance> $$2 = $$1.getPhase();
      Vec3 $$8;
      if ($$2 == EnderDragonPhase.LANDING || $$2 == EnderDragonPhase.TAKEOFF) {
         BlockPos $$3 = this.level().getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.fightOrigin));
         float $$4 = Math.max((float)Math.sqrt($$3.distToCenterSqr(this.position())) / 4.0F, 1.0F);
         float $$5 = 6.0F / $$4;
         float $$6 = this.getXRot();
         float $$7 = 1.5F;
         this.setXRot(-$$5 * 1.5F * 5.0F);
         $$8 = this.getViewVector($$0);
         this.setXRot($$6);
      } else if ($$1.isSitting()) {
         float $$9 = this.getXRot();
         float $$10 = 1.5F;
         this.setXRot(-45.0F);
         $$8 = this.getViewVector($$0);
         this.setXRot($$9);
      } else {
         $$8 = this.getViewVector($$0);
      }

      return $$8;
   }

   public void onCrystalDestroyed(ServerLevel $$0, EndCrystal $$1, BlockPos $$2, DamageSource $$3) {
      Player $$5;
      if ($$3.getEntity() instanceof Player $$4) {
         $$5 = $$4;
      } else {
         $$5 = $$0.getNearestPlayer(CRYSTAL_DESTROY_TARGETING, $$2.getX(), $$2.getY(), $$2.getZ());
      }

      if ($$1 == this.nearestCrystal) {
         this.hurt($$0, this.head, this.damageSources().explosion($$1, $$5), 10.0F);
      }

      this.phaseManager.getCurrentPhase().onCrystalDestroyed($$1, $$2, $$3, $$5);
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (DATA_PHASE.equals($$0) && this.level().isClientSide()) {
         this.phaseManager.setPhase(EnderDragonPhase.getById((Integer)this.getEntityData().get(DATA_PHASE)));
      }

      super.onSyncedDataUpdated($$0);
   }

   public EnderDragonPhaseManager getPhaseManager() {
      return this.phaseManager;
   }

   @Nullable
   public EndDragonFight getDragonFight() {
      return this.dragonFight;
   }

   @Override
   public boolean addEffect(MobEffectInstance $$0, @Nullable net.minecraft.world.entity.Entity $$1) {
      return false;
   }

   @Override
   protected boolean canRide(net.minecraft.world.entity.Entity $$0) {
      return false;
   }

   @Override
   public boolean canUsePortal(boolean $$0) {
      return false;
   }

   @Override
   public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
      super.recreateFromPacket($$0);
      EnderDragonPart[] $$1 = this.getSubEntities();

      for (int $$2 = 0; $$2 < $$1.length; $$2++) {
         $$1[$$2].setId($$2 + $$0.getId() + 1);
      }
   }

   @Override
   public boolean canAttack(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.canBeSeenAsEnemy();
   }

   @Override
   protected float sanitizeScale(float $$0) {
      return 1.0F;
   }
}
