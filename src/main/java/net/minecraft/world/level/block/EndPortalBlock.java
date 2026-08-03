package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class EndPortalBlock extends BaseEntityBlock implements Portal {
   public static final MapCodec<EndPortalBlock> CODEC = simpleCodec(EndPortalBlock::new);
   private static final VoxelShape SHAPE = Block.column(16.0, 6.0, 12.0);

   @Override
   public MapCodec<EndPortalBlock> codec() {
      return CODEC;
   }

   protected EndPortalBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new TheEndPortalBlockEntity($$0, $$1);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected VoxelShape getEntityInsideCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Entity $$3) {
      return $$0.getShape($$1, $$2);
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if ($$3.canUsePortal(false)) {
         if (!$$1.isClientSide() && $$1.dimension() == net.minecraft.world.level.Level.END && $$3 instanceof ServerPlayer $$6 && !$$6.seenCredits) {
            $$6.showEndCredits();
         } else {
            $$3.setAsInsidePortal(this, $$2);
         }
      }
   }

   @Nullable
   @Override
   public TeleportTransition getPortalDestination(ServerLevel $$0, Entity $$1, BlockPos $$2) {
      LevelData.RespawnData $$3 = $$0.getRespawnData();
      ResourceKey<net.minecraft.world.level.Level> $$4 = $$0.dimension();
      boolean $$5 = $$4 == net.minecraft.world.level.Level.END;
      ResourceKey<net.minecraft.world.level.Level> $$6 = $$5 ? $$3.dimension() : net.minecraft.world.level.Level.END;
      BlockPos $$7 = $$5 ? $$3.pos() : ServerLevel.END_SPAWN_POINT;
      ServerLevel $$8 = $$0.getServer().getLevel($$6);
      if ($$8 == null) {
         return null;
      } else {
         Vec3 $$9 = $$7.getBottomCenter();
         float $$10;
         float $$11;
         Set<Relative> $$12;
         if (!$$5) {
            EndPlatformFeature.createEndPlatform($$8, BlockPos.containing($$9).below(), true);
            $$10 = Direction.WEST.toYRot();
            $$11 = 0.0F;
            $$12 = Relative.union(new Set[]{Relative.DELTA, Set.of(Relative.X_ROT)});
            if ($$1 instanceof ServerPlayer) {
               $$9 = $$9.subtract(0.0, 1.0, 0.0);
            }
         } else {
            $$10 = $$3.yaw();
            $$11 = $$3.pitch();
            $$12 = Relative.union(new Set[]{Relative.DELTA, Relative.ROTATION});
            if ($$1 instanceof ServerPlayer $$16) {
               return $$16.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
            }

            $$9 = $$1.adjustSpawnLocation($$8, $$7).getBottomCenter();
         }

         return new TeleportTransition($$8, $$9, Vec3.ZERO, $$10, $$11, $$12, TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      double $$4 = $$2.getX() + $$3.nextDouble();
      double $$5 = $$2.getY() + 0.8;
      double $$6 = $$2.getZ() + $$3.nextDouble();
      $$1.addParticle(ParticleTypes.SMOKE, $$4, $$5, $$6, 0.0, 0.0, 0.0);
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
   protected RenderShape getRenderShape(BlockState $$0) {
      return RenderShape.INVISIBLE;
   }
}
