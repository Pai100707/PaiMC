package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.jspecify.annotations.Nullable;

public class ProtectedBlockProcessor extends StructureProcessor {
   public final TagKey<Block> cannotReplace;
   public static final MapCodec<ProtectedBlockProcessor> CODEC = TagKey.hashedCodec(Registries.BLOCK)
      .xmap(ProtectedBlockProcessor::new, $$0 -> $$0.cannotReplace)
      .fieldOf("value");

   public ProtectedBlockProcessor(TagKey<Block> $$0) {
      this.cannotReplace = $$0;
   }

   @Nullable
   @Override
   public StructureTemplate.StructureBlockInfo processBlock(
      net.minecraft.world.level.LevelReader $$0,
      BlockPos $$1,
      BlockPos $$2,
      StructureTemplate.StructureBlockInfo $$3,
      StructureTemplate.StructureBlockInfo $$4,
      StructurePlaceSettings $$5
   ) {
      return Feature.isReplaceable(this.cannotReplace).test($$0.getBlockState($$4.pos())) ? $$4 : null;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.PROTECTED_BLOCKS;
   }
}
