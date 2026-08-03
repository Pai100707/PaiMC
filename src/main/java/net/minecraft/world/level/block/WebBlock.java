package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WebBlock extends Block {
   public static final MapCodec<WebBlock> CODEC = simpleCodec(WebBlock::new);

   @Override
   public MapCodec<WebBlock> codec() {
      return CODEC;
   }

   public WebBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      Vec3 $$6 = new Vec3(0.25, 0.05F, 0.25);
      if ($$3 instanceof LivingEntity $$7 && $$7.hasEffect(MobEffects.WEAVING)) {
         $$6 = new Vec3(0.5, 0.25, 0.5);
      }

      $$3.makeStuckInBlock($$0, $$6);
   }
}
