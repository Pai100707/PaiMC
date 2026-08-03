package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WitherRoseBlock extends FlowerBlock {
   public static final MapCodec<WitherRoseBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply($$0, WitherRoseBlock::new)
   );

   @Override
   public MapCodec<WitherRoseBlock> codec() {
      return CODEC;
   }

   public WitherRoseBlock(Holder<MobEffect> $$0, float $$1, BlockBehaviour.Properties $$2) {
      this(makeEffectList($$0, $$1), $$2);
   }

   public WitherRoseBlock(SuspiciousStewEffects $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return super.mayPlaceOn($$0, $$1, $$2) || $$0.is(Blocks.NETHERRACK) || $$0.is(Blocks.SOUL_SAND) || $$0.is(Blocks.SOUL_SOIL);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      VoxelShape $$4 = this.getShape($$0, $$1, $$2, CollisionContext.empty());
      Vec3 $$5 = $$4.bounds().getCenter();
      double $$6 = $$2.getX() + $$5.x;
      double $$7 = $$2.getZ() + $$5.z;

      for (int $$8 = 0; $$8 < 3; $$8++) {
         if ($$3.nextBoolean()) {
            $$1.addParticle(
               ParticleTypes.SMOKE, $$6 + $$3.nextDouble() / 5.0, $$2.getY() + (0.5 - $$3.nextDouble()), $$7 + $$3.nextDouble() / 5.0, 0.0, 0.0, 0.0
            );
         }
      }
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if ($$1 instanceof ServerLevel $$6
         && $$1.getDifficulty() != Difficulty.PEACEFUL
         && $$3 instanceof LivingEntity $$7
         && !$$7.isInvulnerableTo($$6, $$1.damageSources().wither())) {
         $$7.addEffect(this.getBeeInteractionEffect());
      }
   }

   @Override
   public MobEffectInstance getBeeInteractionEffect() {
      return new MobEffectInstance(MobEffects.WITHER, 40);
   }
}
