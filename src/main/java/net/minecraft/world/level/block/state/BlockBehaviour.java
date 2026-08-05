package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockBehaviour implements FeatureElement {
   protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{
      Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP
   };
   protected final boolean hasCollision;
   protected final float explosionResistance;
   protected final boolean isRandomlyTicking;
   protected final SoundType soundType;
   protected final float friction;
   protected final float speedFactor;
   protected final float jumpFactor;
   protected final boolean dynamicShape;
   protected final FeatureFlagSet requiredFeatures;
   protected final BlockBehaviour.Properties properties;
   protected final Optional<ResourceKey<LootTable>> drops;
   protected final String descriptionId;

   public BlockBehaviour(BlockBehaviour.Properties $$0) {
      this.hasCollision = $$0.hasCollision;
      this.drops = $$0.effectiveDrops();
      this.descriptionId = $$0.effectiveDescriptionId();
      this.explosionResistance = $$0.explosionResistance;
      this.isRandomlyTicking = $$0.isRandomlyTicking;
      this.soundType = $$0.soundType;
      this.friction = $$0.friction;
      this.speedFactor = $$0.speedFactor;
      this.jumpFactor = $$0.jumpFactor;
      this.dynamicShape = $$0.dynamicShape;
      this.requiredFeatures = $$0.requiredFeatures;
      this.properties = $$0;
   }

   public BlockBehaviour.Properties properties() {
      return this.properties;
   }

   protected abstract MapCodec<? extends Block> codec();

   protected static <B extends Block> RecordCodecBuilder<B, BlockBehaviour.Properties> propertiesCodec() {
      return BlockBehaviour.Properties.CODEC.fieldOf("properties").forGetter(BlockBehaviour::properties);
   }

   public static <B extends Block> MapCodec<B> simpleCodec(Function<BlockBehaviour.Properties, B> $$0) {
      return RecordCodecBuilder.mapCodec($$1 -> $$1.group(propertiesCodec()).apply($$1, $$0));
   }

   protected void updateIndirectNeighbourShapes(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, @Block.UpdateFlags int $$3, int $$4) {
   }

   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      switch ($$1) {
         case LAND:
            return !$$0.isCollisionShapeFullBlock(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
         case WATER:
            return $$0.getFluidState().is(FluidTags.WATER);
         case AIR:
            return !$$0.isCollisionShapeFullBlock(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
         default:
            return false;
      }
   }

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
      return $$0;
   }

   protected boolean skipRendering(BlockState $$0, BlockState $$1, Direction $$2) {
      return false;
   }

   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
   }

   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
   }

   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
   }

   protected void onExplosionHit(BlockState $$0, ServerLevel $$1, BlockPos $$2, net.minecraft.world.level.Explosion $$3, BiConsumer<ItemStack, BlockPos> $$4) {
      if (!$$0.isAir() && $$3.getBlockInteraction() != net.minecraft.world.level.Explosion.BlockInteraction.TRIGGER_BLOCK) {
         Block $$5 = $$0.getBlock();
         boolean $$6 = $$3.getIndirectSourceEntity() instanceof Player;
         if ($$5.dropFromExplosion($$3)) {
            BlockEntity $$7 = $$0.hasBlockEntity() ? $$1.getBlockEntity($$2) : null;
            LootParams.Builder $$8 = new LootParams.Builder($$1)
               .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf($$2))
               .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
               .withOptionalParameter(LootContextParams.BLOCK_ENTITY, $$7)
               .withOptionalParameter(LootContextParams.THIS_ENTITY, $$3.getDirectSourceEntity());
            if ($$3.getBlockInteraction() == net.minecraft.world.level.Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
               $$8.withParameter(LootContextParams.EXPLOSION_RADIUS, $$3.radius());
            }

            $$0.spawnAfterBreak($$1, $$2, ItemStack.EMPTY, $$6);
            $$0.getDrops($$8).forEach($$2x -> $$4.accept($$2x, $$2));
         }

         $$1.setBlock($$2, Blocks.AIR.defaultBlockState(), 3);
         $$5.wasExploded($$1, $$2, $$3);
      }
   }

   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      return InteractionResult.PASS;
   }

   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      return InteractionResult.TRY_WITH_EMPTY_HAND;
   }

   protected boolean triggerEvent(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, int $$3, int $$4) {
      return false;
   }

   protected RenderShape getRenderShape(BlockState $$0) {
      return RenderShape.MODEL;
   }

   protected boolean useShapeForLightOcclusion(BlockState $$0) {
      return false;
   }

   protected boolean isSignalSource(BlockState $$0) {
      return false;
   }

   protected FluidState getFluidState(BlockState $$0) {
      return Fluids.EMPTY.defaultFluidState();
   }

   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return false;
   }

   protected float getMaxHorizontalOffset() {
      return 0.25F;
   }

   protected float getMaxVerticalOffset() {
      return 0.2F;
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   protected boolean shouldChangedStateKeepBlockEntity(BlockState $$0) {
      return false;
   }

   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0;
   }

   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0;
   }

   protected boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
      return $$0.canBeReplaced() && ($$1.getItemInHand().isEmpty() || !$$1.getItemInHand().is(this.asItem()));
   }

   protected boolean canBeReplaced(BlockState $$0, Fluid $$1) {
      return $$0.canBeReplaced() || !$$0.isSolid();
   }

   protected List<ItemStack> getDrops(BlockState $$0, LootParams.Builder $$1) {
      if (this.drops.isEmpty()) {
         return Collections.emptyList();
      } else {
         LootParams $$2 = $$1.withParameter(LootContextParams.BLOCK_STATE, $$0).create(LootContextParamSets.BLOCK);
         ServerLevel $$3 = $$2.getLevel();
         LootTable $$4 = $$3.getServer().reloadableRegistries().getLootTable(this.drops.get());
         return $$4.getRandomItems($$2);
      }
   }

   protected long getSeed(BlockState $$0, BlockPos $$1) {
      return Mth.getSeed($$1);
   }

   protected VoxelShape getOcclusionShape(BlockState $$0) {
      return $$0.getShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
   }

   protected VoxelShape getBlockSupportShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return this.getCollisionShape($$0, $$1, $$2, CollisionContext.empty());
   }

   protected VoxelShape getInteractionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return Shapes.empty();
   }

   protected int getLightBlock(BlockState $$0) {
      if ($$0.isSolidRender()) {
         return 15;
      } else {
         return $$0.propagatesSkylightDown() ? 0 : 1;
      }
   }

   
   protected MenuProvider getMenuProvider(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      return null;
   }

   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return true;
   }

   protected float getShadeBrightness(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.isCollisionShapeFullBlock($$1, $$2) ? 0.2F : 1.0F;
   }

   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return 0;
   }

   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return Shapes.block();
   }

   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.hasCollision ? $$0.getShape($$1, $$2) : Shapes.empty();
   }

   protected VoxelShape getEntityInsideCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Entity $$3) {
      return Shapes.block();
   }

   protected boolean isCollisionShapeFullBlock(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return Block.isShapeFullBlock($$0.getCollisionShape($$1, $$2));
   }

   protected VoxelShape getVisualShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.getCollisionShape($$0, $$1, $$2, $$3);
   }

   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
   }

   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
   }

   protected float getDestroyProgress(BlockState $$0, Player $$1, net.minecraft.world.level.BlockGetter $$2, BlockPos $$3) {
      float $$4 = $$0.getDestroySpeed($$2, $$3);
      if ($$4 == -1.0F) {
         return 0.0F;
      } else {
         int $$5 = $$1.hasCorrectToolForDrops($$0) ? 30 : 100;
         return $$1.getDestroySpeed($$0) / $$4 / $$5;
      }
   }

   protected void spawnAfterBreak(BlockState $$0, ServerLevel $$1, BlockPos $$2, ItemStack $$3, boolean $$4) {
   }

   protected void attack(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3) {
   }

   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return 0;
   }

   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
   }

   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return 0;
   }

   public final Optional<ResourceKey<LootTable>> getLootTable() {
      return this.drops;
   }

   public final String getDescriptionId() {
      return this.descriptionId;
   }

   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
   }

   protected boolean propagatesSkylightDown(BlockState $$0) {
      return !Block.isShapeFullBlock($$0.getShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) && $$0.getFluidState().isEmpty();
   }

   protected boolean isRandomlyTicking(BlockState $$0) {
      return this.isRandomlyTicking;
   }

   protected SoundType getSoundType(BlockState $$0) {
      return this.soundType;
   }

   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(this.asItem());
   }

   public abstract Item asItem();

   protected abstract Block asBlock();

   public MapColor defaultMapColor() {
      return this.properties.mapColor.apply(this.asBlock().defaultBlockState());
   }

   public float defaultDestroyTime() {
      return this.properties.destroyTime;
   }

   public abstract static class BlockStateBase extends StateHolder<Block, BlockState> {
      private static final Direction[] DIRECTIONS = Direction.values();
      private static final VoxelShape[] EMPTY_OCCLUSION_SHAPES = (VoxelShape[])Util.make(
         new VoxelShape[DIRECTIONS.length], $$0 -> Arrays.fill($$0, Shapes.empty())
      );
      private static final VoxelShape[] FULL_BLOCK_OCCLUSION_SHAPES = (VoxelShape[])Util.make(
         new VoxelShape[DIRECTIONS.length], $$0 -> Arrays.fill($$0, Shapes.block())
      );
      private final int lightEmission;
      private final boolean useShapeForLightOcclusion;
      private final boolean isAir;
      private final boolean ignitedByLava;
      @Deprecated
      private final boolean liquid;
      @Deprecated
      private boolean legacySolid;
      private final PushReaction pushReaction;
      private final MapColor mapColor;
      private final float destroySpeed;
      private final boolean requiresCorrectToolForDrops;
      private final boolean canOcclude;
      private final BlockBehaviour.StatePredicate isRedstoneConductor;
      private final BlockBehaviour.StatePredicate isSuffocating;
      private final BlockBehaviour.StatePredicate isViewBlocking;
      private final BlockBehaviour.StatePredicate hasPostProcess;
      private final BlockBehaviour.StatePredicate emissiveRendering;
      
      private final BlockBehaviour.OffsetFunction offsetFunction;
      private final boolean spawnTerrainParticles;
      private final NoteBlockInstrument instrument;
      private final boolean replaceable;
      
      private BlockBehaviour.BlockStateBase.Cache cache;
      private FluidState fluidState = Fluids.EMPTY.defaultFluidState();
      private boolean isRandomlyTicking;
      private boolean solidRender;
      private VoxelShape occlusionShape;
      private VoxelShape[] occlusionShapesByFace;
      private boolean propagatesSkylightDown;
      private int lightBlock;

      protected BlockStateBase(Block $$0, Reference2ObjectArrayMap<Property<?>, Comparable<?>> $$1, MapCodec<BlockState> $$2) {
         super($$0, $$1, $$2);
         BlockBehaviour.Properties $$3 = $$0.properties;
         this.lightEmission = $$3.lightEmission.applyAsInt(this.asState());
         this.useShapeForLightOcclusion = $$0.useShapeForLightOcclusion(this.asState());
         this.isAir = $$3.isAir;
         this.ignitedByLava = $$3.ignitedByLava;
         this.liquid = $$3.liquid;
         this.pushReaction = $$3.pushReaction;
         this.mapColor = $$3.mapColor.apply(this.asState());
         this.destroySpeed = $$3.destroyTime;
         this.requiresCorrectToolForDrops = $$3.requiresCorrectToolForDrops;
         this.canOcclude = $$3.canOcclude;
         this.isRedstoneConductor = $$3.isRedstoneConductor;
         this.isSuffocating = $$3.isSuffocating;
         this.isViewBlocking = $$3.isViewBlocking;
         this.hasPostProcess = $$3.hasPostProcess;
         this.emissiveRendering = $$3.emissiveRendering;
         this.offsetFunction = $$3.offsetFunction;
         this.spawnTerrainParticles = $$3.spawnTerrainParticles;
         this.instrument = $$3.instrument;
         this.replaceable = $$3.replaceable;
      }

      private boolean calculateSolid() {
         if (this.owner.properties.forceSolidOn) {
            return true;
         } else if (this.owner.properties.forceSolidOff) {
            return false;
         } else if (this.cache == null) {
            return false;
         } else {
            VoxelShape $$0 = this.cache.collisionShape;
            if ($$0.isEmpty()) {
               return false;
            } else {
               AABB $$1 = $$0.bounds();
               return $$1.getSize() >= 0.7291666666666666 ? true : $$1.getYsize() >= 1.0;
            }
         }
      }

      public void initCache() {
         this.fluidState = this.owner.getFluidState(this.asState());
         this.isRandomlyTicking = this.owner.isRandomlyTicking(this.asState());
         if (!this.getBlock().hasDynamicShape()) {
            this.cache = new BlockBehaviour.BlockStateBase.Cache(this.asState());
         }

         this.legacySolid = this.calculateSolid();
         this.occlusionShape = this.canOcclude ? this.owner.getOcclusionShape(this.asState()) : Shapes.empty();
         this.solidRender = Block.isShapeFullBlock(this.occlusionShape);
         if (this.occlusionShape.isEmpty()) {
            this.occlusionShapesByFace = EMPTY_OCCLUSION_SHAPES;
         } else if (this.solidRender) {
            this.occlusionShapesByFace = FULL_BLOCK_OCCLUSION_SHAPES;
         } else {
            this.occlusionShapesByFace = new VoxelShape[DIRECTIONS.length];

            for (Direction $$0 : DIRECTIONS) {
               this.occlusionShapesByFace[$$0.ordinal()] = this.occlusionShape.getFaceShape($$0);
            }
         }

         this.propagatesSkylightDown = this.owner.propagatesSkylightDown(this.asState());
         this.lightBlock = this.owner.getLightBlock(this.asState());
      }

      public Block getBlock() {
         return this.owner;
      }

      public Holder<Block> getBlockHolder() {
         return this.owner.builtInRegistryHolder();
      }

      @Deprecated
      public boolean blocksMotion() {
         Block $$0 = this.getBlock();
         return $$0 != Blocks.COBWEB && $$0 != Blocks.BAMBOO_SAPLING && this.isSolid();
      }

      @Deprecated
      public boolean isSolid() {
         return this.legacySolid;
      }

      public boolean isValidSpawn(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, EntityType<?> $$2) {
         return this.getBlock().properties.isValidSpawn.test(this.asState(), $$0, $$1, $$2);
      }

      public boolean propagatesSkylightDown() {
         return this.propagatesSkylightDown;
      }

      public int getLightBlock() {
         return this.lightBlock;
      }

      public VoxelShape getFaceOcclusionShape(Direction $$0) {
         return this.occlusionShapesByFace[$$0.ordinal()];
      }

      public VoxelShape getOcclusionShape() {
         return this.occlusionShape;
      }

      public boolean hasLargeCollisionShape() {
         return this.cache == null || this.cache.largeCollisionShape;
      }

      public boolean useShapeForLightOcclusion() {
         return this.useShapeForLightOcclusion;
      }

      public int getLightEmission() {
         return this.lightEmission;
      }

      public boolean isAir() {
         return this.isAir;
      }

      public boolean ignitedByLava() {
         return this.ignitedByLava;
      }

      @Deprecated
      public boolean liquid() {
         return this.liquid;
      }

      public MapColor getMapColor(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.mapColor;
      }

      public BlockState rotate(Rotation $$0) {
         return this.getBlock().rotate(this.asState(), $$0);
      }

      public BlockState mirror(Mirror $$0) {
         return this.getBlock().mirror(this.asState(), $$0);
      }

      public RenderShape getRenderShape() {
         return this.getBlock().getRenderShape(this.asState());
      }

      public boolean emissiveRendering(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.emissiveRendering.test(this.asState(), $$0, $$1);
      }

      public float getShadeBrightness(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.getBlock().getShadeBrightness(this.asState(), $$0, $$1);
      }

      public boolean isRedstoneConductor(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.isRedstoneConductor.test(this.asState(), $$0, $$1);
      }

      public boolean isSignalSource() {
         return this.getBlock().isSignalSource(this.asState());
      }

      public int getSignal(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
         return this.getBlock().getSignal(this.asState(), $$0, $$1, $$2);
      }

      public boolean hasAnalogOutputSignal() {
         return this.getBlock().hasAnalogOutputSignal(this.asState());
      }

      public int getAnalogOutputSignal(net.minecraft.world.level.Level $$0, BlockPos $$1, Direction $$2) {
         return this.getBlock().getAnalogOutputSignal(this.asState(), $$0, $$1, $$2);
      }

      public float getDestroySpeed(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.destroySpeed;
      }

      public float getDestroyProgress(Player $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
         return this.getBlock().getDestroyProgress(this.asState(), $$0, $$1, $$2);
      }

      public int getDirectSignal(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
         return this.getBlock().getDirectSignal(this.asState(), $$0, $$1, $$2);
      }

      public PushReaction getPistonPushReaction() {
         return this.pushReaction;
      }

      public boolean isSolidRender() {
         return this.solidRender;
      }

      public boolean canOcclude() {
         return this.canOcclude;
      }

      public boolean skipRendering(BlockState $$0, Direction $$1) {
         return this.getBlock().skipRendering(this.asState(), $$0, $$1);
      }

      public VoxelShape getShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.getShape($$0, $$1, CollisionContext.empty());
      }

      public VoxelShape getShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, CollisionContext $$2) {
         return this.getBlock().getShape(this.asState(), $$0, $$1, $$2);
      }

      public VoxelShape getCollisionShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.cache != null ? this.cache.collisionShape : this.getCollisionShape($$0, $$1, CollisionContext.empty());
      }

      public VoxelShape getCollisionShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, CollisionContext $$2) {
         return this.getBlock().getCollisionShape(this.asState(), $$0, $$1, $$2);
      }

      public VoxelShape getEntityInsideCollisionShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Entity $$2) {
         return this.getBlock().getEntityInsideCollisionShape(this.asState(), $$0, $$1, $$2);
      }

      public VoxelShape getBlockSupportShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.getBlock().getBlockSupportShape(this.asState(), $$0, $$1);
      }

      public VoxelShape getVisualShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, CollisionContext $$2) {
         return this.getBlock().getVisualShape(this.asState(), $$0, $$1, $$2);
      }

      public VoxelShape getInteractionShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.getBlock().getInteractionShape(this.asState(), $$0, $$1);
      }

      public final boolean entityCanStandOn(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Entity $$2) {
         return this.entityCanStandOnFace($$0, $$1, $$2, Direction.UP);
      }

      public final boolean entityCanStandOnFace(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Entity $$2, Direction $$3) {
         return Block.isFaceFull(this.getCollisionShape($$0, $$1, CollisionContext.of($$2)), $$3);
      }

      public Vec3 getOffset(BlockPos $$0) {
         BlockBehaviour.OffsetFunction $$1 = this.offsetFunction;
         return $$1 != null ? $$1.evaluate(this.asState(), $$0) : Vec3.ZERO;
      }

      public boolean hasOffsetFunction() {
         return this.offsetFunction != null;
      }

      public boolean triggerEvent(net.minecraft.world.level.Level $$0, BlockPos $$1, int $$2, int $$3) {
         return this.getBlock().triggerEvent(this.asState(), $$0, $$1, $$2, $$3);
      }

      public void handleNeighborChanged(net.minecraft.world.level.Level $$0, BlockPos $$1, Block $$2, Orientation $$3, boolean $$4) {
         this.getBlock().neighborChanged(this.asState(), $$0, $$1, $$2, $$3, $$4);
      }

      public final void updateNeighbourShapes(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, @Block.UpdateFlags int $$2) {
         this.updateNeighbourShapes($$0, $$1, $$2, 512);
      }

      public final void updateNeighbourShapes(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, @Block.UpdateFlags int $$2, int $$3) {
         MutableBlockPos $$4 = new MutableBlockPos();

         for (Direction $$5 : BlockBehaviour.UPDATE_SHAPE_ORDER) {
            $$4.setWithOffset($$1, $$5);
            $$0.neighborShapeChanged($$5.getOpposite(), $$4, $$1, this.asState(), $$2, $$3);
         }
      }

      public final void updateIndirectNeighbourShapes(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, @Block.UpdateFlags int $$2) {
         this.updateIndirectNeighbourShapes($$0, $$1, $$2, 512);
      }

      public void updateIndirectNeighbourShapes(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, @Block.UpdateFlags int $$2, int $$3) {
         this.getBlock().updateIndirectNeighbourShapes(this.asState(), $$0, $$1, $$2, $$3);
      }

      public void onPlace(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
         this.getBlock().onPlace(this.asState(), $$0, $$1, $$2, $$3);
      }

      public void affectNeighborsAfterRemoval(ServerLevel $$0, BlockPos $$1, boolean $$2) {
         this.getBlock().affectNeighborsAfterRemoval(this.asState(), $$0, $$1, $$2);
      }

      public void onExplosionHit(ServerLevel $$0, BlockPos $$1, net.minecraft.world.level.Explosion $$2, BiConsumer<ItemStack, BlockPos> $$3) {
         this.getBlock().onExplosionHit(this.asState(), $$0, $$1, $$2, $$3);
      }

      public void tick(ServerLevel $$0, BlockPos $$1, RandomSource $$2) {
         this.getBlock().tick(this.asState(), $$0, $$1, $$2);
      }

      public void randomTick(ServerLevel $$0, BlockPos $$1, RandomSource $$2) {
         this.getBlock().randomTick(this.asState(), $$0, $$1, $$2);
      }

      public void entityInside(net.minecraft.world.level.Level $$0, BlockPos $$1, Entity $$2, InsideBlockEffectApplier $$3, boolean $$4) {
         this.getBlock().entityInside(this.asState(), $$0, $$1, $$2, $$3, $$4);
      }

      public void spawnAfterBreak(ServerLevel $$0, BlockPos $$1, ItemStack $$2, boolean $$3) {
         this.getBlock().spawnAfterBreak(this.asState(), $$0, $$1, $$2, $$3);
      }

      public List<ItemStack> getDrops(LootParams.Builder $$0) {
         return this.getBlock().getDrops(this.asState(), $$0);
      }

      public InteractionResult useItemOn(ItemStack $$0, net.minecraft.world.level.Level $$1, Player $$2, InteractionHand $$3, BlockHitResult $$4) {
         return this.getBlock().useItemOn($$0, this.asState(), $$1, $$4.getBlockPos(), $$2, $$3, $$4);
      }

      public InteractionResult useWithoutItem(net.minecraft.world.level.Level $$0, Player $$1, BlockHitResult $$2) {
         return this.getBlock().useWithoutItem(this.asState(), $$0, $$2.getBlockPos(), $$1, $$2);
      }

      public void attack(net.minecraft.world.level.Level $$0, BlockPos $$1, Player $$2) {
         this.getBlock().attack(this.asState(), $$0, $$1, $$2);
      }

      public boolean isSuffocating(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.isSuffocating.test(this.asState(), $$0, $$1);
      }

      public boolean isViewBlocking(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.isViewBlocking.test(this.asState(), $$0, $$1);
      }

      public BlockState updateShape(
         net.minecraft.world.level.LevelReader $$0,
         net.minecraft.world.level.ScheduledTickAccess $$1,
         BlockPos $$2,
         Direction $$3,
         BlockPos $$4,
         BlockState $$5,
         RandomSource $$6
      ) {
         return this.getBlock().updateShape(this.asState(), $$0, $$1, $$2, $$3, $$4, $$5, $$6);
      }

      public boolean isPathfindable(PathComputationType $$0) {
         return this.getBlock().isPathfindable(this.asState(), $$0);
      }

      public boolean canBeReplaced(BlockPlaceContext $$0) {
         return this.getBlock().canBeReplaced(this.asState(), $$0);
      }

      public boolean canBeReplaced(Fluid $$0) {
         return this.getBlock().canBeReplaced(this.asState(), $$0);
      }

      public boolean canBeReplaced() {
         return this.replaceable;
      }

      public boolean canSurvive(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
         return this.getBlock().canSurvive(this.asState(), $$0, $$1);
      }

      public boolean hasPostProcess(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.hasPostProcess.test(this.asState(), $$0, $$1);
      }

      
      public MenuProvider getMenuProvider(net.minecraft.world.level.Level $$0, BlockPos $$1) {
         return this.getBlock().getMenuProvider(this.asState(), $$0, $$1);
      }

      public boolean is(TagKey<Block> $$0) {
         return this.getBlock().builtInRegistryHolder().is($$0);
      }

      public boolean is(TagKey<Block> $$0, Predicate<BlockBehaviour.BlockStateBase> $$1) {
         return this.is($$0) && $$1.test(this);
      }

      public boolean is(HolderSet<Block> $$0) {
         return $$0.contains(this.getBlock().builtInRegistryHolder());
      }

      public boolean is(Holder<Block> $$0) {
         return this.is((Block)$$0.value());
      }

      public Stream<TagKey<Block>> getTags() {
         return this.getBlock().builtInRegistryHolder().tags();
      }

      public boolean hasBlockEntity() {
         return this.getBlock() instanceof EntityBlock;
      }

      public boolean shouldChangedStateKeepBlockEntity(BlockState $$0) {
         return this.getBlock().shouldChangedStateKeepBlockEntity($$0);
      }

      
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockEntityType<T> $$1) {
         return this.getBlock() instanceof EntityBlock ? ((EntityBlock)this.getBlock()).getTicker($$0, this.asState(), $$1) : null;
      }

      public boolean is(Block $$0) {
         return this.getBlock() == $$0;
      }

      public boolean is(ResourceKey<Block> $$0) {
         return this.getBlock().builtInRegistryHolder().is($$0);
      }

      public FluidState getFluidState() {
         return this.fluidState;
      }

      public boolean isRandomlyTicking() {
         return this.isRandomlyTicking;
      }

      public long getSeed(BlockPos $$0) {
         return this.getBlock().getSeed(this.asState(), $$0);
      }

      public SoundType getSoundType() {
         return this.getBlock().getSoundType(this.asState());
      }

      public void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
         this.getBlock().onProjectileHit($$0, $$1, $$2, $$3);
      }

      public boolean isFaceSturdy(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
         return this.isFaceSturdy($$0, $$1, $$2, SupportType.FULL);
      }

      public boolean isFaceSturdy(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2, SupportType $$3) {
         return this.cache != null ? this.cache.isFaceSturdy($$2, $$3) : $$3.isSupporting(this.asState(), $$0, $$1, $$2);
      }

      public boolean isCollisionShapeFullBlock(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
         return this.cache != null ? this.cache.isCollisionShapeFullBlock : this.getBlock().isCollisionShapeFullBlock(this.asState(), $$0, $$1);
      }

      public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, boolean $$2) {
         return this.getBlock().getCloneItemStack($$0, $$1, this.asState(), $$2);
      }

      protected abstract BlockState asState();

      public boolean requiresCorrectToolForDrops() {
         return this.requiresCorrectToolForDrops;
      }

      public boolean shouldSpawnTerrainParticles() {
         return this.spawnTerrainParticles;
      }

      public NoteBlockInstrument instrument() {
         return this.instrument;
      }

      static final class Cache {
         private static final Direction[] DIRECTIONS = Direction.values();
         private static final int SUPPORT_TYPE_COUNT = SupportType.values().length;
         protected final VoxelShape collisionShape;
         protected final boolean largeCollisionShape;
         private final boolean[] faceSturdy;
         protected final boolean isCollisionShapeFullBlock;

         Cache(BlockState $$0) {
            Block $$1 = $$0.getBlock();
            this.collisionShape = $$1.getCollisionShape($$0, net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
            if (!this.collisionShape.isEmpty() && $$0.hasOffsetFunction()) {
               throw new IllegalStateException(
                  String.format(
                     Locale.ROOT,
                     "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.",
                     BuiltInRegistries.BLOCK.getKey($$1)
                  )
               );
            } else {
               this.largeCollisionShape = Arrays.stream(Axis.values())
                  .anyMatch($$0x -> this.collisionShape.min($$0x) < 0.0 || this.collisionShape.max($$0x) > 1.0);
               this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];

               for (Direction $$2 : DIRECTIONS) {
                  for (SupportType $$3 : SupportType.values()) {
                     this.faceSturdy[getFaceSupportIndex($$2, $$3)] = $$3.isSupporting(
                        $$0, net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO, $$2
                     );
                  }
               }

               this.isCollisionShapeFullBlock = Block.isShapeFullBlock(
                  $$0.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO)
               );
            }
         }

         public boolean isFaceSturdy(Direction $$0, SupportType $$1) {
            return this.faceSturdy[getFaceSupportIndex($$0, $$1)];
         }

         private static int getFaceSupportIndex(Direction $$0, SupportType $$1) {
            return $$0.ordinal() * SUPPORT_TYPE_COUNT + $$1.ordinal();
         }
      }
   }

   @FunctionalInterface
   public interface OffsetFunction {
      Vec3 evaluate(BlockState var1, BlockPos var2);
   }

   public static enum OffsetType {
      NONE,
      XZ,
      XYZ;
   }

   public static class Properties {
      public static final Codec<BlockBehaviour.Properties> CODEC = MapCodec.unitCodec(() -> of());
      Function<BlockState, MapColor> mapColor = $$0 -> MapColor.NONE;
      boolean hasCollision = true;
      SoundType soundType = SoundType.STONE;
      ToIntFunction<BlockState> lightEmission = $$0 -> 0;
      float explosionResistance;
      float destroyTime;
      boolean requiresCorrectToolForDrops;
      boolean isRandomlyTicking;
      float friction = 0.6F;
      float speedFactor = 1.0F;
      float jumpFactor = 1.0F;
      
      private ResourceKey<Block> id;
      private DependantName<Block, Optional<ResourceKey<LootTable>>> drops = $$0 -> Optional.of(
         ResourceKey.create(Registries.LOOT_TABLE, $$0.identifier().withPrefix("blocks/"))
      );
      private DependantName<Block, String> descriptionId = $$0 -> Util.makeDescriptionId("block", $$0.identifier());
      boolean canOcclude = true;
      boolean isAir;
      boolean ignitedByLava;
      @Deprecated
      boolean liquid;
      @Deprecated
      boolean forceSolidOff;
      boolean forceSolidOn;
      PushReaction pushReaction = PushReaction.NORMAL;
      boolean spawnTerrainParticles = true;
      NoteBlockInstrument instrument = NoteBlockInstrument.HARP;
      boolean replaceable;
      BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn = ($$0, $$1, $$2, $$3) -> $$0.isFaceSturdy($$1, $$2, Direction.UP)
         && $$0.getLightEmission() < 14;
      BlockBehaviour.StatePredicate isRedstoneConductor = ($$0, $$1, $$2) -> $$0.isCollisionShapeFullBlock($$1, $$2);
      BlockBehaviour.StatePredicate isSuffocating = ($$0, $$1, $$2) -> $$0.blocksMotion() && $$0.isCollisionShapeFullBlock($$1, $$2);
      BlockBehaviour.StatePredicate isViewBlocking = this.isSuffocating;
      BlockBehaviour.StatePredicate hasPostProcess = ($$0, $$1, $$2) -> false;
      BlockBehaviour.StatePredicate emissiveRendering = ($$0, $$1, $$2) -> false;
      boolean dynamicShape;
      FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
      
      BlockBehaviour.OffsetFunction offsetFunction;

      private Properties() {
      }

      public static BlockBehaviour.Properties of() {
         return new BlockBehaviour.Properties();
      }

      public static BlockBehaviour.Properties ofFullCopy(BlockBehaviour $$0) {
         BlockBehaviour.Properties $$1 = ofLegacyCopy($$0);
         BlockBehaviour.Properties $$2 = $$0.properties;
         $$1.jumpFactor = $$2.jumpFactor;
         $$1.isRedstoneConductor = $$2.isRedstoneConductor;
         $$1.isValidSpawn = $$2.isValidSpawn;
         $$1.hasPostProcess = $$2.hasPostProcess;
         $$1.isSuffocating = $$2.isSuffocating;
         $$1.isViewBlocking = $$2.isViewBlocking;
         $$1.drops = $$2.drops;
         $$1.descriptionId = $$2.descriptionId;
         return $$1;
      }

      @Deprecated
      public static BlockBehaviour.Properties ofLegacyCopy(BlockBehaviour $$0) {
         BlockBehaviour.Properties $$1 = new BlockBehaviour.Properties();
         BlockBehaviour.Properties $$2 = $$0.properties;
         $$1.destroyTime = $$2.destroyTime;
         $$1.explosionResistance = $$2.explosionResistance;
         $$1.hasCollision = $$2.hasCollision;
         $$1.isRandomlyTicking = $$2.isRandomlyTicking;
         $$1.lightEmission = $$2.lightEmission;
         $$1.mapColor = $$2.mapColor;
         $$1.soundType = $$2.soundType;
         $$1.friction = $$2.friction;
         $$1.speedFactor = $$2.speedFactor;
         $$1.dynamicShape = $$2.dynamicShape;
         $$1.canOcclude = $$2.canOcclude;
         $$1.isAir = $$2.isAir;
         $$1.ignitedByLava = $$2.ignitedByLava;
         $$1.liquid = $$2.liquid;
         $$1.forceSolidOff = $$2.forceSolidOff;
         $$1.forceSolidOn = $$2.forceSolidOn;
         $$1.pushReaction = $$2.pushReaction;
         $$1.requiresCorrectToolForDrops = $$2.requiresCorrectToolForDrops;
         $$1.offsetFunction = $$2.offsetFunction;
         $$1.spawnTerrainParticles = $$2.spawnTerrainParticles;
         $$1.requiredFeatures = $$2.requiredFeatures;
         $$1.emissiveRendering = $$2.emissiveRendering;
         $$1.instrument = $$2.instrument;
         $$1.replaceable = $$2.replaceable;
         return $$1;
      }

      public BlockBehaviour.Properties mapColor(DyeColor $$0) {
         this.mapColor = $$1 -> $$0.getMapColor();
         return this;
      }

      public BlockBehaviour.Properties mapColor(MapColor $$0) {
         this.mapColor = $$1 -> $$0;
         return this;
      }

      public BlockBehaviour.Properties mapColor(Function<BlockState, MapColor> $$0) {
         this.mapColor = $$0;
         return this;
      }

      public BlockBehaviour.Properties noCollision() {
         this.hasCollision = false;
         this.canOcclude = false;
         return this;
      }

      public BlockBehaviour.Properties noOcclusion() {
         this.canOcclude = false;
         return this;
      }

      public BlockBehaviour.Properties friction(float $$0) {
         this.friction = $$0;
         return this;
      }

      public BlockBehaviour.Properties speedFactor(float $$0) {
         this.speedFactor = $$0;
         return this;
      }

      public BlockBehaviour.Properties jumpFactor(float $$0) {
         this.jumpFactor = $$0;
         return this;
      }

      public BlockBehaviour.Properties sound(SoundType $$0) {
         this.soundType = $$0;
         return this;
      }

      public BlockBehaviour.Properties lightLevel(ToIntFunction<BlockState> $$0) {
         this.lightEmission = $$0;
         return this;
      }

      public BlockBehaviour.Properties strength(float $$0, float $$1) {
         return this.destroyTime($$0).explosionResistance($$1);
      }

      public BlockBehaviour.Properties instabreak() {
         return this.strength(0.0F);
      }

      public BlockBehaviour.Properties strength(float $$0) {
         this.strength($$0, $$0);
         return this;
      }

      public BlockBehaviour.Properties randomTicks() {
         this.isRandomlyTicking = true;
         return this;
      }

      public BlockBehaviour.Properties dynamicShape() {
         this.dynamicShape = true;
         return this;
      }

      public BlockBehaviour.Properties noLootTable() {
         this.drops = DependantName.fixed(Optional.empty());
         return this;
      }

      public BlockBehaviour.Properties overrideLootTable(Optional<ResourceKey<LootTable>> $$0) {
         this.drops = DependantName.fixed($$0);
         return this;
      }

      protected Optional<ResourceKey<LootTable>> effectiveDrops() {
         return (Optional<ResourceKey<LootTable>>)this.drops.get(Objects.requireNonNull(this.id, "Block id not set"));
      }

      public BlockBehaviour.Properties ignitedByLava() {
         this.ignitedByLava = true;
         return this;
      }

      public BlockBehaviour.Properties liquid() {
         this.liquid = true;
         return this;
      }

      public BlockBehaviour.Properties forceSolidOn() {
         this.forceSolidOn = true;
         return this;
      }

      @Deprecated
      public BlockBehaviour.Properties forceSolidOff() {
         this.forceSolidOff = true;
         return this;
      }

      public BlockBehaviour.Properties pushReaction(PushReaction $$0) {
         this.pushReaction = $$0;
         return this;
      }

      public BlockBehaviour.Properties air() {
         this.isAir = true;
         return this;
      }

      public BlockBehaviour.Properties isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> $$0) {
         this.isValidSpawn = $$0;
         return this;
      }

      public BlockBehaviour.Properties isRedstoneConductor(BlockBehaviour.StatePredicate $$0) {
         this.isRedstoneConductor = $$0;
         return this;
      }

      public BlockBehaviour.Properties isSuffocating(BlockBehaviour.StatePredicate $$0) {
         this.isSuffocating = $$0;
         return this;
      }

      public BlockBehaviour.Properties isViewBlocking(BlockBehaviour.StatePredicate $$0) {
         this.isViewBlocking = $$0;
         return this;
      }

      public BlockBehaviour.Properties hasPostProcess(BlockBehaviour.StatePredicate $$0) {
         this.hasPostProcess = $$0;
         return this;
      }

      public BlockBehaviour.Properties emissiveRendering(BlockBehaviour.StatePredicate $$0) {
         this.emissiveRendering = $$0;
         return this;
      }

      public BlockBehaviour.Properties requiresCorrectToolForDrops() {
         this.requiresCorrectToolForDrops = true;
         return this;
      }

      public BlockBehaviour.Properties destroyTime(float $$0) {
         this.destroyTime = $$0;
         return this;
      }

      public BlockBehaviour.Properties explosionResistance(float $$0) {
         this.explosionResistance = Math.max(0.0F, $$0);
         return this;
      }

      public BlockBehaviour.Properties offsetType(BlockBehaviour.OffsetType $$0) {
         this.offsetFunction = switch ($$0) {
            case NONE -> null;
            case XZ -> ($$0x, $$1) -> {
               Block $$2 = $$0x.getBlock();
               long $$3 = Mth.getSeed($$1.getX(), 0, $$1.getZ());
               float $$4 = $$2.getMaxHorizontalOffset();
               double $$5 = Mth.clamp(((float)($$3 & 15L) / 15.0F - 0.5) * 0.5, -$$4, $$4);
               double $$6 = Mth.clamp(((float)($$3 >> 8 & 15L) / 15.0F - 0.5) * 0.5, -$$4, $$4);
               return new Vec3($$5, 0.0, $$6);
            };
            case XYZ -> ($$0x, $$1) -> {
               Block $$2 = $$0x.getBlock();
               long $$3 = Mth.getSeed($$1.getX(), 0, $$1.getZ());
               double $$4 = ((float)($$3 >> 4 & 15L) / 15.0F - 1.0) * $$2.getMaxVerticalOffset();
               float $$5 = $$2.getMaxHorizontalOffset();
               double $$6 = Mth.clamp(((float)($$3 & 15L) / 15.0F - 0.5) * 0.5, -$$5, $$5);
               double $$7 = Mth.clamp(((float)($$3 >> 8 & 15L) / 15.0F - 0.5) * 0.5, -$$5, $$5);
               return new Vec3($$6, $$4, $$7);
            };
         };
         return this;
      }

      public BlockBehaviour.Properties noTerrainParticles() {
         this.spawnTerrainParticles = false;
         return this;
      }

      public BlockBehaviour.Properties requiredFeatures(FeatureFlag... $$0) {
         this.requiredFeatures = FeatureFlags.REGISTRY.subset($$0);
         return this;
      }

      public BlockBehaviour.Properties instrument(NoteBlockInstrument $$0) {
         this.instrument = $$0;
         return this;
      }

      public BlockBehaviour.Properties replaceable() {
         this.replaceable = true;
         return this;
      }

      public BlockBehaviour.Properties setId(ResourceKey<Block> $$0) {
         this.id = $$0;
         return this;
      }

      public BlockBehaviour.Properties overrideDescription(String $$0) {
         this.descriptionId = DependantName.fixed($$0);
         return this;
      }

      protected String effectiveDescriptionId() {
         return (String)this.descriptionId.get(Objects.requireNonNull(this.id, "Block id not set"));
      }
   }

   @FunctionalInterface
   public interface StateArgumentPredicate<A> {
      boolean test(BlockState var1, net.minecraft.world.level.BlockGetter var2, BlockPos var3, A var4);
   }

   @FunctionalInterface
   public interface StatePredicate {
      boolean test(BlockState var1, net.minecraft.world.level.BlockGetter var2, BlockPos var3);
   }
}
