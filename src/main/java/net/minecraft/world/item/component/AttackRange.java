package net.minecraft.world.item.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record AttackRange(float minRange, float maxRange, float minCreativeRange, float maxCreativeRange, float hitboxMargin, float mobFactor) {
   public static final Codec<AttackRange> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.floatRange(0.0F, 64.0F).optionalFieldOf("min_reach", 0.0F).forGetter(AttackRange::minRange),
            ExtraCodecs.floatRange(0.0F, 64.0F).optionalFieldOf("max_reach", 3.0F).forGetter(AttackRange::maxRange),
            ExtraCodecs.floatRange(0.0F, 64.0F).optionalFieldOf("min_creative_reach", 0.0F).forGetter(AttackRange::minCreativeRange),
            ExtraCodecs.floatRange(0.0F, 64.0F).optionalFieldOf("max_creative_reach", 5.0F).forGetter(AttackRange::maxCreativeRange),
            ExtraCodecs.floatRange(0.0F, 1.0F).optionalFieldOf("hitbox_margin", 0.3F).forGetter(AttackRange::hitboxMargin),
            Codec.floatRange(0.0F, 2.0F).optionalFieldOf("mob_factor", 1.0F).forGetter(AttackRange::mobFactor)
         )
         .apply($$0, AttackRange::new)
   );
   public static final StreamCodec<ByteBuf, AttackRange> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT,
      AttackRange::minRange,
      ByteBufCodecs.FLOAT,
      AttackRange::maxRange,
      ByteBufCodecs.FLOAT,
      AttackRange::minCreativeRange,
      ByteBufCodecs.FLOAT,
      AttackRange::maxCreativeRange,
      ByteBufCodecs.FLOAT,
      AttackRange::hitboxMargin,
      ByteBufCodecs.FLOAT,
      AttackRange::mobFactor,
      AttackRange::new
   );

   public static AttackRange defaultFor(LivingEntity $$0) {
      return new AttackRange(
         0.0F,
         (float)$$0.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE),
         0.0F,
         (float)$$0.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE),
         0.0F,
         1.0F
      );
   }

   public HitResult getClosesetHit(Entity $$0, float $$1, Predicate<Entity> $$2) {
      Either<BlockHitResult, Collection<EntityHitResult>> $$3 = ProjectileUtil.getHitEntitiesAlong($$0, this, $$2, Block.OUTLINE);
      if ($$3.left().isPresent()) {
         return (HitResult)$$3.left().get();
      } else {
         Collection<EntityHitResult> $$4 = (Collection<EntityHitResult>)$$3.right().get();
         EntityHitResult $$5 = null;
         Vec3 $$6 = $$0.getEyePosition($$1);
         double $$7 = Double.MAX_VALUE;

         for (EntityHitResult $$8 : $$4) {
            double $$9 = $$6.distanceToSqr($$8.getLocation());
            if ($$9 < $$7) {
               $$7 = $$9;
               $$5 = $$8;
            }
         }

         if ($$5 != null) {
            return $$5;
         } else {
            Vec3 $$10 = $$0.getHeadLookAngle();
            Vec3 $$11 = $$0.getEyePosition($$1).add($$10);
            return BlockHitResult.miss($$11, Direction.getApproximateNearest($$10), BlockPos.containing($$11));
         }
      }
   }

   public float effectiveMinRange(Entity $$0) {
      if ($$0 instanceof Player $$1) {
         if ($$1.isSpectator()) {
            return 0.0F;
         } else {
            return $$1.isCreative() ? this.minCreativeRange : this.minRange;
         }
      } else {
         return this.minRange * this.mobFactor;
      }
   }

   public float effectiveMaxRange(Entity $$0) {
      if ($$0 instanceof Player $$1) {
         return $$1.isCreative() ? this.maxCreativeRange : this.maxRange;
      } else {
         return this.maxRange * this.mobFactor;
      }
   }

   public boolean isInRange(LivingEntity $$0, Vec3 $$1) {
      return this.isInRange($$0, $$1::distanceToSqr, 0.0);
   }

   public boolean isInRange(LivingEntity $$0, AABB $$1, double $$2) {
      return this.isInRange($$0, $$1::distanceToSqr, $$2);
   }

   private boolean isInRange(LivingEntity $$0, ToDoubleFunction<Vec3> $$1, double $$2) {
      double $$3 = Math.sqrt($$1.applyAsDouble($$0.getEyePosition()));
      double $$4 = this.effectiveMinRange($$0) - this.hitboxMargin - $$2;
      double $$5 = this.effectiveMaxRange($$0) + this.hitboxMargin + $$2;
      return $$3 >= $$4 && $$3 <= $$5;
   }
}
