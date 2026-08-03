package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlackstoneReplaceProcessor extends StructureProcessor {
   public static final MapCodec<BlackstoneReplaceProcessor> CODEC = MapCodec.unit(() -> BlackstoneReplaceProcessor.INSTANCE);
   public static final BlackstoneReplaceProcessor INSTANCE = new BlackstoneReplaceProcessor();
   private final Map<Block, Block> replacements = (Map<Block, Block>)Util.make(Maps.newHashMap(), $$0 -> {
      $$0.put(Blocks.COBBLESTONE, Blocks.BLACKSTONE);
      $$0.put(Blocks.MOSSY_COBBLESTONE, Blocks.BLACKSTONE);
      $$0.put(Blocks.STONE, Blocks.POLISHED_BLACKSTONE);
      $$0.put(Blocks.STONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS);
      $$0.put(Blocks.MOSSY_STONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS);
      $$0.put(Blocks.COBBLESTONE_STAIRS, Blocks.BLACKSTONE_STAIRS);
      $$0.put(Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.BLACKSTONE_STAIRS);
      $$0.put(Blocks.STONE_STAIRS, Blocks.POLISHED_BLACKSTONE_STAIRS);
      $$0.put(Blocks.STONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
      $$0.put(Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
      $$0.put(Blocks.COBBLESTONE_SLAB, Blocks.BLACKSTONE_SLAB);
      $$0.put(Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.BLACKSTONE_SLAB);
      $$0.put(Blocks.SMOOTH_STONE_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB);
      $$0.put(Blocks.STONE_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB);
      $$0.put(Blocks.STONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
      $$0.put(Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
      $$0.put(Blocks.STONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
      $$0.put(Blocks.MOSSY_STONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
      $$0.put(Blocks.COBBLESTONE_WALL, Blocks.BLACKSTONE_WALL);
      $$0.put(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.BLACKSTONE_WALL);
      $$0.put(Blocks.CHISELED_STONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE);
      $$0.put(Blocks.CRACKED_STONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
      $$0.put(Blocks.IRON_BARS, Blocks.IRON_CHAIN);
   });

   private BlackstoneReplaceProcessor() {
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
      Block $$6 = this.replacements.get($$4.state().getBlock());
      if ($$6 == null) {
         return $$4;
      } else {
         BlockState $$7 = $$4.state();
         BlockState $$8 = $$6.defaultBlockState();
         if ($$7.hasProperty(StairBlock.FACING)) {
            $$8 = $$8.setValue(StairBlock.FACING, (Direction)$$7.getValue(StairBlock.FACING));
         }

         if ($$7.hasProperty(StairBlock.HALF)) {
            $$8 = $$8.setValue(StairBlock.HALF, $$7.getValue(StairBlock.HALF));
         }

         if ($$7.hasProperty(SlabBlock.TYPE)) {
            $$8 = $$8.setValue(SlabBlock.TYPE, $$7.getValue(SlabBlock.TYPE));
         }

         return new StructureTemplate.StructureBlockInfo($$4.pos(), $$8, $$4.nbt());
      }
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLACKSTONE_REPLACE;
   }
}
