package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.util.BlockUtil.FoundRectangle;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class NetherPortalBlock extends Block implements Portal {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec<NetherPortalBlock> CODEC = simpleCodec(NetherPortalBlock::new);
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
   private static final Map<Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(Block.column(4.0, 16.0, 0.0, 16.0));

   @Override
   public MapCodec<NetherPortalBlock> codec() {
      return CODEC;
   }

   public NetherPortalBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Axis.X));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get($$0.getValue(AXIS));
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$1.isSpawningMonsters()
         && (Boolean)$$1.environmentAttributes().getValue(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, $$2)
         && $$3.nextInt(2000) < $$1.getDifficulty().getId()
         && $$1.anyPlayerCloseEnoughForSpawning($$2)) {
         while ($$1.getBlockState($$2).is(this)) {
            $$2 = $$2.below();
         }

         if ($$1.getBlockState($$2).isValidSpawn($$1, $$2, EntityType.ZOMBIFIED_PIGLIN)) {
            Entity $$4 = EntityType.ZOMBIFIED_PIGLIN.spawn($$1, $$2.above(), EntitySpawnReason.STRUCTURE);
            if ($$4 != null) {
               $$4.setPortalCooldown();
               Entity $$5 = $$4.getVehicle();
               if ($$5 != null) {
                  $$5.setPortalCooldown();
               }
            }
         }
      }
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      Axis $$8 = $$4.getAxis();
      Axis $$9 = $$0.getValue(AXIS);
      boolean $$10 = $$9 != $$8 && $$8.isHorizontal();
      return !$$10 && !$$6.is(this) && !PortalShape.findAnyShape($$1, $$3, $$9).isComplete()
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if ($$3.canUsePortal(false)) {
         $$3.setAsInsidePortal(this, $$2);
      }
   }

   @Override
   public int getPortalTransitionTime(ServerLevel $$0, Entity $$1) {
      return $$1 instanceof Player $$2
         ? Math.max(
            0,
            $$0.getGameRules()
               .get($$2.getAbilities().invulnerable ? GameRules.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY : GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY)
         )
         : 0;
   }

   
   @Override
   public TeleportTransition getPortalDestination(ServerLevel $$0, Entity $$1, BlockPos $$2) {
      ResourceKey<net.minecraft.world.level.Level> $$3 = $$0.dimension() == net.minecraft.world.level.Level.NETHER
         ? net.minecraft.world.level.Level.OVERWORLD
         : net.minecraft.world.level.Level.NETHER;
      ServerLevel $$4 = $$0.getServer().getLevel($$3);
      if ($$4 == null) {
         return null;
      } else {
         boolean $$5 = $$4.dimension() == net.minecraft.world.level.Level.NETHER;
         WorldBorder $$6 = $$4.getWorldBorder();
         double $$7 = DimensionType.getTeleportationScale($$0.dimensionType(), $$4.dimensionType());
         BlockPos $$8 = $$6.clampToBounds($$1.getX() * $$7, $$1.getY(), $$1.getZ() * $$7);
         return this.getExitPortal($$4, $$1, $$2, $$8, $$5, $$6);
      }
   }

   
   private TeleportTransition getExitPortal(ServerLevel $$0, Entity $$1, BlockPos $$2, BlockPos $$3, boolean $$4, WorldBorder $$5) {
      Optional<BlockPos> $$6 = $$0.getPortalForcer().findClosestPortalPosition($$3, $$4, $$5);
      FoundRectangle $$9;
      TeleportTransition.PostTeleportTransition $$10;
      if ($$6.isPresent()) {
         BlockPos $$7 = $$6.get();
         BlockState $$8 = $$0.getBlockState($$7);
         $$9 = BlockUtil.getLargestRectangleAround(
            $$7, $$8.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Axis.Y, 21, $$2x -> $$0.getBlockState($$2x) == $$8
         );
         $$10 = TeleportTransition.PLAY_PORTAL_SOUND.then($$1x -> $$1x.placePortalTicket($$7));
      } else {
         Axis $$11 = $$1.level().getBlockState($$2).getOptionalValue(AXIS).orElse(Axis.X);
         Optional<FoundRectangle> $$12 = $$0.getPortalForcer().createPortal($$3, $$11);
         if ($$12.isEmpty()) {
            LOGGER.error("Unable to create a portal, likely target out of worldborder");
            return null;
         }

         $$9 = $$12.get();
         $$10 = TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET);
      }

      return getDimensionTransitionFromExit($$1, $$2, $$9, $$0, $$10);
   }

   private static TeleportTransition getDimensionTransitionFromExit(
      Entity $$0, BlockPos $$1, FoundRectangle $$2, ServerLevel $$3, TeleportTransition.PostTeleportTransition $$4
   ) {
      BlockState $$5 = $$0.level().getBlockState($$1);
      Axis $$6;
      Vec3 $$8;
      if ($$5.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
         $$6 = $$5.getValue(BlockStateProperties.HORIZONTAL_AXIS);
         FoundRectangle $$7 = BlockUtil.getLargestRectangleAround($$1, $$6, 21, Axis.Y, 21, $$2x -> $$0.level().getBlockState($$2x) == $$5);
         $$8 = $$0.getRelativePortalPosition($$6, $$7);
      } else {
         $$6 = Axis.X;
         $$8 = new Vec3(0.5, 0.0, 0.0);
      }

      return createDimensionTransition($$3, $$2, $$6, $$8, $$0, $$4);
   }

   private static TeleportTransition createDimensionTransition(
      ServerLevel $$0, FoundRectangle $$1, Axis $$2, Vec3 $$3, Entity $$4, TeleportTransition.PostTeleportTransition $$5
   ) {
      BlockPos $$6 = $$1.minCorner;
      BlockState $$7 = $$0.getBlockState($$6);
      Axis $$8 = $$7.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Axis.X);
      double $$9 = $$1.axis1Size;
      double $$10 = $$1.axis2Size;
      EntityDimensions $$11 = $$4.getDimensions($$4.getPose());
      int $$12 = $$2 == $$8 ? 0 : 90;
      double $$13 = $$11.width() / 2.0 + ($$9 - $$11.width()) * $$3.x();
      double $$14 = ($$10 - $$11.height()) * $$3.y();
      double $$15 = 0.5 + $$3.z();
      boolean $$16 = $$8 == Axis.X;
      Vec3 $$17 = new Vec3($$6.getX() + ($$16 ? $$13 : $$15), $$6.getY() + $$14, $$6.getZ() + ($$16 ? $$15 : $$13));
      Vec3 $$18 = PortalShape.findCollisionFreePosition($$17, $$0, $$4, $$11);
      return new TeleportTransition($$0, $$18, Vec3.ZERO, $$12, 0.0F, Relative.union(new Set[]{Relative.DELTA, Relative.ROTATION}), $$5);
   }

   @Override
   public Portal.Transition getLocalTransition() {
      return Portal.Transition.CONFUSION;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$3.nextInt(100) == 0) {
         $$1.playLocalSound(
            $$2.getX() + 0.5, $$2.getY() + 0.5, $$2.getZ() + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, $$3.nextFloat() * 0.4F + 0.8F, false
         );
      }

      for (int $$4 = 0; $$4 < 4; $$4++) {
         double $$5 = $$2.getX() + $$3.nextDouble();
         double $$6 = $$2.getY() + $$3.nextDouble();
         double $$7 = $$2.getZ() + $$3.nextDouble();
         double $$8 = ($$3.nextFloat() - 0.5) * 0.5;
         double $$9 = ($$3.nextFloat() - 0.5) * 0.5;
         double $$10 = ($$3.nextFloat() - 0.5) * 0.5;
         int $$11 = $$3.nextInt(2) * 2 - 1;
         if (!$$1.getBlockState($$2.west()).is(this) && !$$1.getBlockState($$2.east()).is(this)) {
            $$5 = $$2.getX() + 0.5 + 0.25 * $$11;
            $$8 = $$3.nextFloat() * 2.0F * $$11;
         } else {
            $$7 = $$2.getZ() + 0.5 + 0.25 * $$11;
            $$10 = $$3.nextFloat() * 2.0F * $$11;
         }

         $$1.addParticle(ParticleTypes.PORTAL, $$5, $$6, $$7, $$8, $$9, $$10);
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return ItemStack.EMPTY;
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      switch ($$1) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            switch ((Axis)$$0.getValue(AXIS)) {
               case X:
                  return $$0.setValue(AXIS, Axis.Z);
               case Z:
                  return $$0.setValue(AXIS, Axis.X);
               default:
                  return $$0;
            }
         default:
            return $$0;
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AXIS);
   }
}
