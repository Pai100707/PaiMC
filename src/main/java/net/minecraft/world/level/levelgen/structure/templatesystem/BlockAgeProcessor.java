package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public class BlockAgeProcessor extends StructureProcessor {
   public static final MapCodec<BlockAgeProcessor> CODEC = Codec.FLOAT.fieldOf("mossiness").xmap(BlockAgeProcessor::new, $$0 -> $$0.mossiness);
   private static final float PROBABILITY_OF_REPLACING_FULL_BLOCK = 0.5F;
   private static final float PROBABILITY_OF_REPLACING_STAIRS = 0.5F;
   private static final float PROBABILITY_OF_REPLACING_OBSIDIAN = 0.15F;
   private static final BlockState[] NON_MOSSY_REPLACEMENTS = new BlockState[]{
      Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()
   };
   private final float mossiness;

   public BlockAgeProcessor(float $$0) {
      this.mossiness = $$0;
   }

   
   @Override
   public StructureTemplate.StructureBlockInfo processBlock(
      net.minecraft.world.level.LevelReader $$0,
      BlockPos $$1,
      BlockPos $$2,
      StructureTemplate.StructureBlockInfo $$3,
      StructureTemplate.StructureBlockInfo $$4,
      StructurePlaceSettings $$5
   ) {
      RandomSource $$6 = $$5.getRandom($$4.pos());
      BlockState $$7 = $$4.state();
      BlockPos $$8 = $$4.pos();
      BlockState $$9 = null;
      if ($$7.is(Blocks.STONE_BRICKS) || $$7.is(Blocks.STONE) || $$7.is(Blocks.CHISELED_STONE_BRICKS)) {
         $$9 = this.maybeReplaceFullStoneBlock($$6);
      } else if ($$7.is(BlockTags.STAIRS)) {
         $$9 = this.maybeReplaceStairs($$7, $$6);
      } else if ($$7.is(BlockTags.SLABS)) {
         $$9 = this.maybeReplaceSlab($$7, $$6);
      } else if ($$7.is(BlockTags.WALLS)) {
         $$9 = this.maybeReplaceWall($$7, $$6);
      } else if ($$7.is(Blocks.OBSIDIAN)) {
         $$9 = this.maybeReplaceObsidian($$6);
      }

      return $$9 != null ? new StructureTemplate.StructureBlockInfo($$8, $$9, $$4.nbt()) : $$4;
   }

   
   private BlockState maybeReplaceFullStoneBlock(RandomSource $$0) {
      if ($$0.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] $$1 = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs($$0, Blocks.STONE_BRICK_STAIRS)};
         BlockState[] $$2 = new BlockState[]{Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs($$0, Blocks.MOSSY_STONE_BRICK_STAIRS)};
         return this.getRandomBlock($$0, $$1, $$2);
      }
   }

   
   private BlockState maybeReplaceStairs(BlockState $$0, RandomSource $$1) {
      if ($$1.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] $$2 = new BlockState[]{Blocks.MOSSY_STONE_BRICK_STAIRS.withPropertiesOf($$0), Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()};
         return this.getRandomBlock($$1, NON_MOSSY_REPLACEMENTS, $$2);
      }
   }

   
   private BlockState maybeReplaceSlab(BlockState $$0, RandomSource $$1) {
      return $$1.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_SLAB.withPropertiesOf($$0) : null;
   }

   
   private BlockState maybeReplaceWall(BlockState $$0, RandomSource $$1) {
      return $$1.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_WALL.withPropertiesOf($$0) : null;
   }

   
   private BlockState maybeReplaceObsidian(RandomSource $$0) {
      return $$0.nextFloat() < 0.15F ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : null;
   }

   private static BlockState getRandomFacingStairs(RandomSource $$0, Block $$1) {
      return $$1.defaultBlockState()
         .setValue(StairBlock.FACING, Plane.HORIZONTAL.getRandomDirection($$0))
         .setValue(StairBlock.HALF, (Half)Util.getRandom(Half.values(), $$0));
   }

   private BlockState getRandomBlock(RandomSource $$0, BlockState[] $$1, BlockState[] $$2) {
      return $$0.nextFloat() < this.mossiness ? getRandomBlock($$0, $$2) : getRandomBlock($$0, $$1);
   }

   private static BlockState getRandomBlock(RandomSource $$0, BlockState[] $$1) {
      return $$1[$$0.nextInt($$1.length)];
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_AGE;
   }
}
