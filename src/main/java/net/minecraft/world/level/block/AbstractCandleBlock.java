package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractCandleBlock extends Block {
   public static final int LIGHT_PER_CANDLE = 3;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   @Override
   protected abstract MapCodec<? extends AbstractCandleBlock> codec();

   protected AbstractCandleBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   protected abstract Iterable<Vec3> getParticleOffsets(BlockState var1);

   public static boolean isLit(BlockState $$0) {
      return $$0.hasProperty(LIT) && ($$0.is(BlockTags.CANDLES) || $$0.is(BlockTags.CANDLE_CAKES)) && $$0.getValue(LIT);
   }

   @Override
   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
      if (!$$0.isClientSide() && $$3.isOnFire() && this.canBeLit($$1)) {
         setLit($$0, $$1, $$2.getBlockPos(), true);
      }
   }

   protected boolean canBeLit(BlockState $$0) {
      return !$$0.getValue(LIT);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(LIT)) {
         this.getParticleOffsets($$0).forEach($$3x -> addParticlesAndSound($$1, $$3x.add($$2.getX(), $$2.getY(), $$2.getZ()), $$3));
      }
   }

   private static void addParticlesAndSound(net.minecraft.world.level.Level $$0, Vec3 $$1, RandomSource $$2) {
      float $$3 = $$2.nextFloat();
      if ($$3 < 0.3F) {
         $$0.addParticle(ParticleTypes.SMOKE, $$1.x, $$1.y, $$1.z, 0.0, 0.0, 0.0);
         if ($$3 < 0.17F) {
            $$0.playLocalSound(
               $$1.x + 0.5,
               $$1.y + 0.5,
               $$1.z + 0.5,
               SoundEvents.CANDLE_AMBIENT,
               SoundSource.BLOCKS,
               1.0F + $$2.nextFloat(),
               $$2.nextFloat() * 0.7F + 0.3F,
               false
            );
         }
      }

      $$0.addParticle(ParticleTypes.SMALL_FLAME, $$1.x, $$1.y, $$1.z, 0.0, 0.0, 0.0);
   }

   public static void extinguish(@Nullable Player $$0, BlockState $$1, net.minecraft.world.level.LevelAccessor $$2, BlockPos $$3) {
      setLit($$2, $$1, $$3, false);
      if ($$1.getBlock() instanceof AbstractCandleBlock) {
         ((AbstractCandleBlock)$$1.getBlock())
            .getParticleOffsets($$1)
            .forEach($$2x -> $$2.addParticle(ParticleTypes.SMOKE, $$3.getX() + $$2x.x(), $$3.getY() + $$2x.y(), $$3.getZ() + $$2x.z(), 0.0, 0.1F, 0.0));
      }

      $$2.playSound(null, $$3, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
      $$2.gameEvent($$0, GameEvent.BLOCK_CHANGE, $$3);
   }

   private static void setLit(net.minecraft.world.level.LevelAccessor $$0, BlockState $$1, BlockPos $$2, boolean $$3) {
      $$0.setBlock($$2, $$1.setValue(LIT, $$3), 11);
   }

   @Override
   protected void onExplosionHit(BlockState $$0, ServerLevel $$1, BlockPos $$2, net.minecraft.world.level.Explosion $$3, BiConsumer<ItemStack, BlockPos> $$4) {
      if ($$3.canTriggerBlocks() && $$0.getValue(LIT)) {
         extinguish(null, $$0, $$1, $$2);
      }

      super.onExplosionHit($$0, $$1, $$2, $$3, $$4);
   }
}
