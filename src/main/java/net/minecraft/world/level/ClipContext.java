package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClipContext {
   private final Vec3 from;
   private final Vec3 to;
   private final net.minecraft.world.level.ClipContext.Block block;
   private final net.minecraft.world.level.ClipContext.Fluid fluid;
   private final CollisionContext collisionContext;

   public ClipContext(Vec3 $$0, Vec3 $$1, net.minecraft.world.level.ClipContext.Block $$2, net.minecraft.world.level.ClipContext.Fluid $$3, Entity $$4) {
      this($$0, $$1, $$2, $$3, CollisionContext.of($$4));
   }

   public ClipContext(
      Vec3 $$0, Vec3 $$1, net.minecraft.world.level.ClipContext.Block $$2, net.minecraft.world.level.ClipContext.Fluid $$3, CollisionContext $$4
   ) {
      this.from = $$0;
      this.to = $$1;
      this.block = $$2;
      this.fluid = $$3;
      this.collisionContext = $$4;
   }

   public Vec3 getTo() {
      return this.to;
   }

   public Vec3 getFrom() {
      return this.from;
   }

   public VoxelShape getBlockShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return this.block.get($$0, $$1, $$2, this.collisionContext);
   }

   public VoxelShape getFluidShape(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return this.fluid.canPick($$0) ? $$0.getShape($$1, $$2) : Shapes.empty();
   }

   public static enum Block implements net.minecraft.world.level.ClipContext.ShapeGetter {
      COLLIDER(BlockBehaviour.BlockStateBase::getCollisionShape),
      OUTLINE(BlockBehaviour.BlockStateBase::getShape),
      VISUAL(BlockBehaviour.BlockStateBase::getVisualShape),
      FALLDAMAGE_RESETTING(
         ($$0, $$1, $$2, $$3) -> {
            if ($$0.is(BlockTags.FALL_DAMAGE_RESETTING)) {
               return Shapes.block();
            } else {
               if ($$3 instanceof EntityCollisionContext $$4 && $$4.getEntity() != null && $$4.getEntity().getType() == EntityType.PLAYER) {
                  if ($$0.is(Blocks.END_GATEWAY) || $$0.is(Blocks.END_PORTAL)) {
                     return Shapes.block();
                  }

                  if ($$1 instanceof ServerLevel $$5
                     && $$0.is(Blocks.NETHER_PORTAL)
                     && $$5.getGameRules().get(GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY) == 0) {
                     return Shapes.block();
                  }
               }

               return Shapes.empty();
            }
         }
      );

      private final net.minecraft.world.level.ClipContext.ShapeGetter shapeGetter;

      private Block(final net.minecraft.world.level.ClipContext.ShapeGetter $$0) {
         this.shapeGetter = $$0;
      }

      @Override
      public VoxelShape get(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
         return this.shapeGetter.get($$0, $$1, $$2, $$3);
      }
   }

   public static enum Fluid {
      NONE($$0 -> false),
      SOURCE_ONLY(FluidState::isSource),
      ANY($$0 -> !$$0.isEmpty()),
      WATER($$0 -> $$0.is(FluidTags.WATER));

      private final Predicate<FluidState> canPick;

      private Fluid(final Predicate<FluidState> $$0) {
         this.canPick = $$0;
      }

      public boolean canPick(FluidState $$0) {
         return this.canPick.test($$0);
      }
   }

   public interface ShapeGetter {
      VoxelShape get(BlockState var1, net.minecraft.world.level.BlockGetter var2, BlockPos var3, CollisionContext var4);
   }
}
