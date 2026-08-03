package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerPotBlock extends Block {
   public static final MapCodec<FlowerPotBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("potted").forGetter($$0x -> $$0x.potted), propertiesCodec())
         .apply($$0, FlowerPotBlock::new)
   );
   private static final Map<Block, Block> POTTED_BY_CONTENT = Maps.newHashMap();
   private static final VoxelShape SHAPE = Block.column(6.0, 0.0, 6.0);
   private final Block potted;

   @Override
   public MapCodec<FlowerPotBlock> codec() {
      return CODEC;
   }

   public FlowerPotBlock(Block $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.potted = $$0;
      POTTED_BY_CONTENT.put($$0, this);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      BlockState $$8 = ($$0.getItem() instanceof BlockItem $$7 ? POTTED_BY_CONTENT.getOrDefault($$7.getBlock(), Blocks.AIR) : Blocks.AIR).defaultBlockState();
      if ($$8.isAir()) {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      } else if (!this.isEmpty()) {
         return InteractionResult.CONSUME;
      } else {
         $$2.setBlock($$3, $$8, 3);
         $$2.gameEvent($$4, GameEvent.BLOCK_CHANGE, $$3);
         $$4.awardStat(Stats.POT_FLOWER);
         $$0.consume(1, $$4);
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (this.isEmpty()) {
         return InteractionResult.CONSUME;
      } else {
         ItemStack $$5 = new ItemStack(this.potted);
         if (!$$3.addItem($$5)) {
            $$3.drop($$5, false);
         }

         $$1.setBlock($$2, Blocks.FLOWER_POT.defaultBlockState(), 3);
         $$1.gameEvent($$3, GameEvent.BLOCK_CHANGE, $$2);
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return this.isEmpty() ? super.getCloneItemStack($$0, $$1, $$2, $$3) : new ItemStack(this.potted);
   }

   private boolean isEmpty() {
      return this.potted == Blocks.AIR;
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
      return $$4 == Direction.DOWN && !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   public Block getPotted() {
      return this.potted;
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return $$0.is(Blocks.POTTED_OPEN_EYEBLOSSOM) || $$0.is(Blocks.POTTED_CLOSED_EYEBLOSSOM);
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (this.isRandomlyTicking($$0)) {
         boolean $$4 = this.potted == Blocks.OPEN_EYEBLOSSOM;
         boolean $$5 = ((TriState)$$1.environmentAttributes().getValue(EnvironmentAttributes.EYEBLOSSOM_OPEN, $$2)).toBoolean($$4);
         if ($$4 != $$5) {
            $$1.setBlock($$2, this.opposite($$0), 3);
            EyeblossomBlock.Type $$6 = EyeblossomBlock.Type.fromBoolean($$4).transform();
            $$6.spawnTransformParticle($$1, $$2, $$3);
            $$1.playSound(null, $$2, $$6.longSwitchSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
         }
      }

      super.randomTick($$0, $$1, $$2, $$3);
   }

   public BlockState opposite(BlockState $$0) {
      if ($$0.is(Blocks.POTTED_OPEN_EYEBLOSSOM)) {
         return Blocks.POTTED_CLOSED_EYEBLOSSOM.defaultBlockState();
      } else {
         return $$0.is(Blocks.POTTED_CLOSED_EYEBLOSSOM) ? Blocks.POTTED_OPEN_EYEBLOSSOM.defaultBlockState() : $$0;
      }
   }
}
