package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public class ServerExplosion implements net.minecraft.world.level.Explosion {
   private static final net.minecraft.world.level.ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new net.minecraft.world.level.ExplosionDamageCalculator();
   private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
   private static final float LARGE_EXPLOSION_RADIUS = 2.0F;
   private final boolean fire;
   private final net.minecraft.world.level.Explosion.BlockInteraction blockInteraction;
   private final ServerLevel level;
   private final Vec3 center;
   @Nullable
   private final Entity source;
   private final float radius;
   private final DamageSource damageSource;
   private final net.minecraft.world.level.ExplosionDamageCalculator damageCalculator;
   private final Map<Player, Vec3> hitPlayers = new HashMap<>();

   public ServerExplosion(
      ServerLevel $$0,
      @Nullable Entity $$1,
      @Nullable DamageSource $$2,
      @Nullable net.minecraft.world.level.ExplosionDamageCalculator $$3,
      Vec3 $$4,
      float $$5,
      boolean $$6,
      net.minecraft.world.level.Explosion.BlockInteraction $$7
   ) {
      this.level = $$0;
      this.source = $$1;
      this.radius = $$5;
      this.center = $$4;
      this.fire = $$6;
      this.blockInteraction = $$7;
      this.damageSource = $$2 == null ? $$0.damageSources().explosion(this) : $$2;
      this.damageCalculator = $$3 == null ? this.makeDamageCalculator($$1) : $$3;
   }

   private net.minecraft.world.level.ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity $$0) {
      return (net.minecraft.world.level.ExplosionDamageCalculator)($$0 == null
         ? EXPLOSION_DAMAGE_CALCULATOR
         : new net.minecraft.world.level.EntityBasedExplosionDamageCalculator($$0));
   }

   public static float getSeenPercent(Vec3 $$0, Entity $$1) {
      AABB $$2 = $$1.getBoundingBox();
      double $$3 = 1.0 / (($$2.maxX - $$2.minX) * 2.0 + 1.0);
      double $$4 = 1.0 / (($$2.maxY - $$2.minY) * 2.0 + 1.0);
      double $$5 = 1.0 / (($$2.maxZ - $$2.minZ) * 2.0 + 1.0);
      double $$6 = (1.0 - Math.floor(1.0 / $$3) * $$3) / 2.0;
      double $$7 = (1.0 - Math.floor(1.0 / $$5) * $$5) / 2.0;
      if (!($$3 < 0.0) && !($$4 < 0.0) && !($$5 < 0.0)) {
         int $$8 = 0;
         int $$9 = 0;

         for (double $$10 = 0.0; $$10 <= 1.0; $$10 += $$3) {
            for (double $$11 = 0.0; $$11 <= 1.0; $$11 += $$4) {
               for (double $$12 = 0.0; $$12 <= 1.0; $$12 += $$5) {
                  double $$13 = Mth.lerp($$10, $$2.minX, $$2.maxX);
                  double $$14 = Mth.lerp($$11, $$2.minY, $$2.maxY);
                  double $$15 = Mth.lerp($$12, $$2.minZ, $$2.maxZ);
                  Vec3 $$16 = new Vec3($$13 + $$6, $$14, $$15 + $$7);
                  if ($$1.level()
                        .clip(
                           new net.minecraft.world.level.ClipContext(
                              $$16, $$0, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, $$1
                           )
                        )
                        .getType()
                     == Type.MISS) {
                     $$8++;
                  }

                  $$9++;
               }
            }
         }

         return (float)$$8 / $$9;
      } else {
         return 0.0F;
      }
   }

   @Override
   public float radius() {
      return this.radius;
   }

   @Override
   public Vec3 center() {
      return this.center;
   }

   private List<BlockPos> calculateExplodedPositions() {
      Set<BlockPos> $$0 = new HashSet<>();
      int $$1 = 16;

      for (int $$2 = 0; $$2 < 16; $$2++) {
         for (int $$3 = 0; $$3 < 16; $$3++) {
            for (int $$4 = 0; $$4 < 16; $$4++) {
               if ($$2 == 0 || $$2 == 15 || $$3 == 0 || $$3 == 15 || $$4 == 0 || $$4 == 15) {
                  double $$5 = $$2 / 15.0F * 2.0F - 1.0F;
                  double $$6 = $$3 / 15.0F * 2.0F - 1.0F;
                  double $$7 = $$4 / 15.0F * 2.0F - 1.0F;
                  double $$8 = Math.sqrt($$5 * $$5 + $$6 * $$6 + $$7 * $$7);
                  $$5 /= $$8;
                  $$6 /= $$8;
                  $$7 /= $$8;
                  float $$9 = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                  double $$10 = this.center.x;
                  double $$11 = this.center.y;
                  double $$12 = this.center.z;

                  for (float $$13 = 0.3F; $$9 > 0.0F; $$9 -= 0.22500001F) {
                     BlockPos $$14 = BlockPos.containing($$10, $$11, $$12);
                     BlockState $$15 = this.level.getBlockState($$14);
                     FluidState $$16 = this.level.getFluidState($$14);
                     if (!this.level.isInWorldBounds($$14)) {
                        break;
                     }

                     Optional<Float> $$17 = this.damageCalculator.getBlockExplosionResistance(this, this.level, $$14, $$15, $$16);
                     if ($$17.isPresent()) {
                        $$9 -= ($$17.get() + 0.3F) * 0.3F;
                     }

                     if ($$9 > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, $$14, $$15, $$9)) {
                        $$0.add($$14);
                     }

                     $$10 += $$5 * 0.3F;
                     $$11 += $$6 * 0.3F;
                     $$12 += $$7 * 0.3F;
                  }
               }
            }
         }
      }

      return new ObjectArrayList($$0);
   }

   private void hurtEntities() {
      if (!(this.radius < 1.0E-5F)) {
         float $$0 = this.radius * 2.0F;
         int $$1 = Mth.floor(this.center.x - $$0 - 1.0);
         int $$2 = Mth.floor(this.center.x + $$0 + 1.0);
         int $$3 = Mth.floor(this.center.y - $$0 - 1.0);
         int $$4 = Mth.floor(this.center.y + $$0 + 1.0);
         int $$5 = Mth.floor(this.center.z - $$0 - 1.0);
         int $$6 = Mth.floor(this.center.z + $$0 + 1.0);

         for (Entity $$8 : this.level.getEntities(this.source, new AABB($$1, $$3, $$5, $$2, $$4, $$6))) {
            if (!$$8.ignoreExplosion(this)) {
               double $$9 = Math.sqrt($$8.distanceToSqr(this.center)) / $$0;
               if (!($$9 > 1.0)) {
                  Vec3 $$10 = $$8 instanceof PrimedTnt ? $$8.position() : $$8.getEyePosition();
                  Vec3 $$11 = $$10.subtract(this.center).normalize();
                  boolean $$12 = this.damageCalculator.shouldDamageEntity(this, $$8);
                  float $$13 = this.damageCalculator.getKnockbackMultiplier($$8);
                  float $$14 = !$$12 && $$13 == 0.0F ? 0.0F : getSeenPercent(this.center, $$8);
                  if ($$12) {
                     $$8.hurtServer(this.level, this.damageSource, this.damageCalculator.getEntityDamageAmount(this, $$8, $$14));
                  }

                  double $$16 = $$8 instanceof LivingEntity $$15 ? $$15.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE) : 0.0;
                  double $$17 = (1.0 - $$9) * $$14 * $$13 * (1.0 - $$16);
                  Vec3 $$18 = $$11.scale($$17);
                  $$8.push($$18);
                  if ($$8.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && $$8 instanceof Projectile $$19) {
                     $$19.setOwner(this.damageSource.getEntity());
                  } else if ($$8 instanceof Player $$20 && !$$20.isSpectator() && (!$$20.isCreative() || !$$20.getAbilities().flying)) {
                     this.hitPlayers.put($$20, $$18);
                  }

                  $$8.onExplosionHit(this.source);
               }
            }
         }
      }
   }

   private void interactWithBlocks(List<BlockPos> $$0) {
      List<net.minecraft.world.level.ServerExplosion.StackCollector> $$1 = new ArrayList<>();
      Util.shuffle($$0, this.level.random);

      for (BlockPos $$2 : $$0) {
         this.level.getBlockState($$2).onExplosionHit(this.level, $$2, this, ($$1x, $$2x) -> addOrAppendStack($$1, $$1x, $$2x));
      }

      for (net.minecraft.world.level.ServerExplosion.StackCollector $$3 : $$1) {
         Block.popResource(this.level, $$3.pos, $$3.stack);
      }
   }

   private void createFire(List<BlockPos> $$0) {
      for (BlockPos $$1 : $$0) {
         if (this.level.random.nextInt(3) == 0 && this.level.getBlockState($$1).isAir() && this.level.getBlockState($$1.below()).isSolidRender()) {
            this.level.setBlockAndUpdate($$1, BaseFireBlock.getState(this.level, $$1));
         }
      }
   }

   public int explode() {
      this.level.gameEvent(this.source, GameEvent.EXPLODE, this.center);
      List<BlockPos> $$0 = this.calculateExplodedPositions();
      this.hurtEntities();
      if (this.interactsWithBlocks()) {
         ProfilerFiller $$1 = Profiler.get();
         $$1.push("explosion_blocks");
         this.interactWithBlocks($$0);
         $$1.pop();
      }

      if (this.fire) {
         this.createFire($$0);
      }

      return $$0.size();
   }

   private static void addOrAppendStack(List<net.minecraft.world.level.ServerExplosion.StackCollector> $$0, ItemStack $$1, BlockPos $$2) {
      for (net.minecraft.world.level.ServerExplosion.StackCollector $$3 : $$0) {
         $$3.tryMerge($$1);
         if ($$1.isEmpty()) {
            return;
         }
      }

      $$0.add(new net.minecraft.world.level.ServerExplosion.StackCollector($$2, $$1));
   }

   private boolean interactsWithBlocks() {
      return this.blockInteraction != net.minecraft.world.level.Explosion.BlockInteraction.KEEP;
   }

   public Map<Player, Vec3> getHitPlayers() {
      return this.hitPlayers;
   }

   @Override
   public ServerLevel level() {
      return this.level;
   }

   @Nullable
   @Override
   public LivingEntity getIndirectSourceEntity() {
      return net.minecraft.world.level.Explosion.getIndirectSourceEntity(this.source);
   }

   @Nullable
   @Override
   public Entity getDirectSourceEntity() {
      return this.source;
   }

   public DamageSource getDamageSource() {
      return this.damageSource;
   }

   @Override
   public net.minecraft.world.level.Explosion.BlockInteraction getBlockInteraction() {
      return this.blockInteraction;
   }

   @Override
   public boolean canTriggerBlocks() {
      if (this.blockInteraction != net.minecraft.world.level.Explosion.BlockInteraction.TRIGGER_BLOCK) {
         return false;
      } else {
         return this.source != null && this.source.getType() == EntityType.BREEZE_WIND_CHARGE ? this.level.getGameRules().get(GameRules.MOB_GRIEFING) : true;
      }
   }

   @Override
   public boolean shouldAffectBlocklikeEntities() {
      boolean $$0 = this.level.getGameRules().get(GameRules.MOB_GRIEFING);
      boolean $$1 = this.source == null || this.source.getType() != EntityType.BREEZE_WIND_CHARGE && this.source.getType() != EntityType.WIND_CHARGE;
      return $$0 ? $$1 : this.blockInteraction.shouldAffectBlocklikeEntities() && $$1;
   }

   public boolean isSmall() {
      return this.radius < 2.0F || !this.interactsWithBlocks();
   }

   static class StackCollector {
      final BlockPos pos;
      ItemStack stack;

      StackCollector(BlockPos $$0, ItemStack $$1) {
         this.pos = $$0;
         this.stack = $$1;
      }

      public void tryMerge(ItemStack $$0) {
         if (ItemEntity.areMergable(this.stack, $$0)) {
            this.stack = ItemEntity.merge(this.stack, $$0, 16);
         }
      }
   }
}
