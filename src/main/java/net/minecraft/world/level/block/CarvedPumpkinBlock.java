package net.minecraft.world.level.block;

import com.google.common.collect.BiMap;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

public class CarvedPumpkinBlock extends HorizontalDirectionalBlock {
   public static final MapCodec<CarvedPumpkinBlock> CODEC = simpleCodec(CarvedPumpkinBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   @Nullable
   private BlockPattern snowGolemBase;
   @Nullable
   private BlockPattern snowGolemFull;
   @Nullable
   private BlockPattern ironGolemBase;
   @Nullable
   private BlockPattern ironGolemFull;
   @Nullable
   private BlockPattern copperGolemBase;
   @Nullable
   private BlockPattern copperGolemFull;
   private static final Predicate<BlockState> PUMPKINS_PREDICATE = $$0 -> $$0.is(Blocks.CARVED_PUMPKIN) || $$0.is(Blocks.JACK_O_LANTERN);

   @Override
   public MapCodec<? extends CarvedPumpkinBlock> codec() {
      return CODEC;
   }

   protected CarvedPumpkinBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$3.is($$0.getBlock())) {
         this.trySpawnGolem($$1, $$2);
      }
   }

   public boolean canSpawnGolem(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      return this.getOrCreateSnowGolemBase().find($$0, $$1) != null
         || this.getOrCreateIronGolemBase().find($$0, $$1) != null
         || this.getOrCreateCopperGolemBase().find($$0, $$1) != null;
   }

   private void trySpawnGolem(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      BlockPattern.BlockPatternMatch $$2 = this.getOrCreateSnowGolemFull().find($$0, $$1);
      if ($$2 != null) {
         SnowGolem $$3 = (SnowGolem)EntityType.SNOW_GOLEM.create($$0, EntitySpawnReason.TRIGGERED);
         if ($$3 != null) {
            spawnGolemInWorld($$0, $$2, $$3, $$2.getBlock(0, 2, 0).getPos());
            return;
         }
      }

      BlockPattern.BlockPatternMatch $$4 = this.getOrCreateIronGolemFull().find($$0, $$1);
      if ($$4 != null) {
         IronGolem $$5 = (IronGolem)EntityType.IRON_GOLEM.create($$0, EntitySpawnReason.TRIGGERED);
         if ($$5 != null) {
            $$5.setPlayerCreated(true);
            spawnGolemInWorld($$0, $$4, $$5, $$4.getBlock(1, 2, 0).getPos());
            return;
         }
      }

      BlockPattern.BlockPatternMatch $$6 = this.getOrCreateCopperGolemFull().find($$0, $$1);
      if ($$6 != null) {
         CopperGolem $$7 = (CopperGolem)EntityType.COPPER_GOLEM.create($$0, EntitySpawnReason.TRIGGERED);
         if ($$7 != null) {
            spawnGolemInWorld($$0, $$6, $$7, $$6.getBlock(0, 0, 0).getPos());
            this.replaceCopperBlockWithChest($$0, $$6);
            $$7.spawn(this.getWeatherStateFromPattern($$6));
         }
      }
   }

   private WeatheringCopper.WeatherState getWeatherStateFromPattern(BlockPattern.BlockPatternMatch $$0) {
      BlockState $$1 = $$0.getBlock(0, 1, 0).getState();
      return $$1.getBlock() instanceof WeatheringCopper $$3
         ? $$3.getAge()
         : Optional.ofNullable((Block)((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).get($$1.getBlock()))
            .filter($$0x -> $$0x instanceof WeatheringCopper)
            .map($$0x -> (WeatheringCopper)$$0x)
            .orElse((WeatheringCopper)Blocks.COPPER_BLOCK)
            .getAge();
   }

   private static void spawnGolemInWorld(net.minecraft.world.level.Level $$0, BlockPattern.BlockPatternMatch $$1, Entity $$2, BlockPos $$3) {
      clearPatternBlocks($$0, $$1);
      $$2.snapTo($$3.getX() + 0.5, $$3.getY() + 0.05, $$3.getZ() + 0.5, 0.0F, 0.0F);
      $$0.addFreshEntity($$2);

      for (ServerPlayer $$4 : $$0.getEntitiesOfClass(ServerPlayer.class, $$2.getBoundingBox().inflate(5.0))) {
         CriteriaTriggers.SUMMONED_ENTITY.trigger($$4, $$2);
      }

      updatePatternBlocks($$0, $$1);
   }

   public static void clearPatternBlocks(net.minecraft.world.level.Level $$0, BlockPattern.BlockPatternMatch $$1) {
      for (int $$2 = 0; $$2 < $$1.getWidth(); $$2++) {
         for (int $$3 = 0; $$3 < $$1.getHeight(); $$3++) {
            BlockInWorld $$4 = $$1.getBlock($$2, $$3, 0);
            $$0.setBlock($$4.getPos(), Blocks.AIR.defaultBlockState(), 2);
            $$0.levelEvent(2001, $$4.getPos(), Block.getId($$4.getState()));
         }
      }
   }

   public static void updatePatternBlocks(net.minecraft.world.level.Level $$0, BlockPattern.BlockPatternMatch $$1) {
      for (int $$2 = 0; $$2 < $$1.getWidth(); $$2++) {
         for (int $$3 = 0; $$3 < $$1.getHeight(); $$3++) {
            BlockInWorld $$4 = $$1.getBlock($$2, $$3, 0);
            $$0.updateNeighborsAt($$4.getPos(), Blocks.AIR);
         }
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getOpposite());
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING);
   }

   private BlockPattern getOrCreateSnowGolemBase() {
      if (this.snowGolemBase == null) {
         this.snowGolemBase = BlockPatternBuilder.start()
            .aisle(" ", "#", "#")
            .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK)))
            .build();
      }

      return this.snowGolemBase;
   }

   private BlockPattern getOrCreateSnowGolemFull() {
      if (this.snowGolemFull == null) {
         this.snowGolemFull = BlockPatternBuilder.start()
            .aisle("^", "#", "#")
            .where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE))
            .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK)))
            .build();
      }

      return this.snowGolemFull;
   }

   private BlockPattern getOrCreateIronGolemBase() {
      if (this.ironGolemBase == null) {
         this.ironGolemBase = BlockPatternBuilder.start()
            .aisle("~ ~", "###", "~#~")
            .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK)))
            .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
            .build();
      }

      return this.ironGolemBase;
   }

   private BlockPattern getOrCreateIronGolemFull() {
      if (this.ironGolemFull == null) {
         this.ironGolemFull = BlockPatternBuilder.start()
            .aisle("~^~", "###", "~#~")
            .where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE))
            .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK)))
            .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
            .build();
      }

      return this.ironGolemFull;
   }

   private BlockPattern getOrCreateCopperGolemBase() {
      if (this.copperGolemBase == null) {
         this.copperGolemBase = BlockPatternBuilder.start().aisle(" ", "#").where('#', BlockInWorld.hasState($$0 -> $$0.is(BlockTags.COPPER))).build();
      }

      return this.copperGolemBase;
   }

   private BlockPattern getOrCreateCopperGolemFull() {
      if (this.copperGolemFull == null) {
         this.copperGolemFull = BlockPatternBuilder.start()
            .aisle("^", "#")
            .where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE))
            .where('#', BlockInWorld.hasState($$0 -> $$0.is(BlockTags.COPPER)))
            .build();
      }

      return this.copperGolemFull;
   }

   public void replaceCopperBlockWithChest(net.minecraft.world.level.Level $$0, BlockPattern.BlockPatternMatch $$1) {
      BlockInWorld $$2 = $$1.getBlock(0, 1, 0);
      BlockInWorld $$3 = $$1.getBlock(0, 0, 0);
      Direction $$4 = $$3.getState().getValue(FACING);
      BlockState $$5 = CopperChestBlock.getFromCopperBlock($$2.getState().getBlock(), $$4, $$0, $$2.getPos());
      $$0.setBlock($$2.getPos(), $$5, 2);
   }
}
