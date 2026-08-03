package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EndGatewayBlock extends BaseEntityBlock implements Portal {
   public static final MapCodec<EndGatewayBlock> CODEC = simpleCodec(EndGatewayBlock::new);

   @Override
   public MapCodec<EndGatewayBlock> codec() {
      return CODEC;
   }

   protected EndGatewayBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new TheEndGatewayBlockEntity($$0, $$1);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return createTickerHelper(
         $$2, BlockEntityType.END_GATEWAY, $$0.isClientSide() ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::portalTick
      );
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      BlockEntity $$4 = $$1.getBlockEntity($$2);
      if ($$4 instanceof TheEndGatewayBlockEntity) {
         int $$5 = ((TheEndGatewayBlockEntity)$$4).getParticleAmount();

         for (int $$6 = 0; $$6 < $$5; $$6++) {
            double $$7 = $$2.getX() + $$3.nextDouble();
            double $$8 = $$2.getY() + $$3.nextDouble();
            double $$9 = $$2.getZ() + $$3.nextDouble();
            double $$10 = ($$3.nextDouble() - 0.5) * 0.5;
            double $$11 = ($$3.nextDouble() - 0.5) * 0.5;
            double $$12 = ($$3.nextDouble() - 0.5) * 0.5;
            int $$13 = $$3.nextInt(2) * 2 - 1;
            if ($$3.nextBoolean()) {
               $$9 = $$2.getZ() + 0.5 + 0.25 * $$13;
               $$12 = $$3.nextFloat() * 2.0F * $$13;
            } else {
               $$7 = $$2.getX() + 0.5 + 0.25 * $$13;
               $$10 = $$3.nextFloat() * 2.0F * $$13;
            }

            $$1.addParticle(ParticleTypes.PORTAL, $$7, $$8, $$9, $$10, $$11, $$12);
         }
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return ItemStack.EMPTY;
   }

   @Override
   protected boolean canBeReplaced(BlockState $$0, Fluid $$1) {
      return false;
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if ($$3.canUsePortal(false) && !$$1.isClientSide() && $$1.getBlockEntity($$2) instanceof TheEndGatewayBlockEntity $$7 && !$$7.isCoolingDown()) {
         $$3.setAsInsidePortal(this, $$2);
         TheEndGatewayBlockEntity.triggerCooldown($$1, $$2, $$0, $$7);
      }
   }

   @Nullable
   @Override
   public TeleportTransition getPortalDestination(ServerLevel $$0, Entity $$1, BlockPos $$2) {
      if ($$0.getBlockEntity($$2) instanceof TheEndGatewayBlockEntity $$4) {
         Vec3 $$6 = $$4.getPortalPosition($$0, $$2);
         if ($$6 == null) {
            return null;
         } else {
            return $$1 instanceof ThrownEnderpearl
               ? new TeleportTransition($$0, $$6, Vec3.ZERO, 0.0F, 0.0F, Set.of(), TeleportTransition.PLACE_PORTAL_TICKET)
               : new TeleportTransition(
                  $$0, $$6, Vec3.ZERO, 0.0F, 0.0F, Relative.union(new Set[]{Relative.DELTA, Relative.ROTATION}), TeleportTransition.PLACE_PORTAL_TICKET
               );
         }
      } else {
         return null;
      }
   }

   @Override
   protected RenderShape getRenderShape(BlockState $$0) {
      return RenderShape.INVISIBLE;
   }
}
