package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity extends net.minecraft.world.entity.Entity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private int checkInterval;
   protected BlockPos pos;

   protected BlockAttachedEntity(net.minecraft.world.entity.EntityType<? extends BlockAttachedEntity> $$0, Level $$1) {
      super($$0, $$1);
   }

   protected BlockAttachedEntity(net.minecraft.world.entity.EntityType<? extends BlockAttachedEntity> $$0, Level $$1, BlockPos $$2) {
      this($$0, $$1);
      this.pos = $$2;
   }

   protected abstract void recalculateBoundingBox();

   @Override
   public void tick() {
      if (this.level() instanceof ServerLevel $$0) {
         this.checkBelowWorld();
         if (this.checkInterval++ == 100) {
            this.checkInterval = 0;
            if (!this.isRemoved() && !this.survives()) {
               this.discard();
               this.dropItem($$0, null);
            }
         }
      }
   }

   public abstract boolean survives();

   @Override
   public boolean isPickable() {
      return true;
   }

   @Override
   public boolean skipAttackInteraction(net.minecraft.world.entity.Entity $$0) {
      if ($$0 instanceof Player $$1) {
         return !this.level().mayInteract($$1, this.pos) ? true : this.hurtOrSimulate(this.damageSources().playerAttack($$1), 0.0F);
      } else {
         return false;
      }
   }

   @Override
   public boolean hurtClient(DamageSource $$0) {
      return !this.isInvulnerableToBase($$0);
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (this.isInvulnerableToBase($$1)) {
         return false;
      } else if (!(Boolean)$$0.getGameRules().get(GameRules.MOB_GRIEFING) && $$1.getEntity() instanceof net.minecraft.world.entity.Mob) {
         return false;
      } else {
         if (!this.isRemoved()) {
            this.kill($$0);
            this.markHurt();
            this.dropItem($$0, $$1.getEntity());
         }

         return true;
      }
   }

   @Override
   public boolean ignoreExplosion(Explosion $$0) {
      net.minecraft.world.entity.Entity $$1 = $$0.getDirectSourceEntity();
      if ($$1 != null && $$1.isInWater()) {
         return true;
      } else {
         return $$0.shouldAffectBlocklikeEntities() ? super.ignoreExplosion($$0) : true;
      }
   }

   @Override
   public void move(net.minecraft.world.entity.MoverType $$0, Vec3 $$1) {
      if (this.level() instanceof ServerLevel $$2 && !this.isRemoved() && $$1.lengthSqr() > 0.0) {
         this.kill($$2);
         this.dropItem($$2, null);
      }
   }

   @Override
   public void push(double $$0, double $$1, double $$2) {
      if (this.level() instanceof ServerLevel $$3 && !this.isRemoved() && $$0 * $$0 + $$1 * $$1 + $$2 * $$2 > 0.0) {
         this.kill($$3);
         this.dropItem($$3, null);
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      $$0.store("block_pos", BlockPos.CODEC, this.getPos());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      BlockPos $$1 = (BlockPos)$$0.read("block_pos", BlockPos.CODEC).orElse(null);
      if ($$1 != null && $$1.closerThan(this.blockPosition(), 16.0)) {
         this.pos = $$1;
      } else {
         LOGGER.error("Block-attached entity at invalid position: {}", $$1);
      }
   }

   public abstract void dropItem(ServerLevel var1, net.minecraft.world.entity.Entity var2);

   @Override
   protected boolean repositionEntityAfterLoad() {
      return false;
   }

   @Override
   public void setPos(double $$0, double $$1, double $$2) {
      this.pos = BlockPos.containing($$0, $$1, $$2);
      this.recalculateBoundingBox();
      this.needsSync = true;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   @Override
   public void thunderHit(ServerLevel $$0, net.minecraft.world.entity.LightningBolt $$1) {
   }

   @Override
   public void refreshDimensions() {
   }
}
