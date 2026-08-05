package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;

public class BlockRotProcessor extends StructureProcessor {
   public static final MapCodec<BlockRotProcessor> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("rottable_blocks").forGetter($$0x -> $$0x.rottableBlocks),
            Codec.floatRange(0.0F, 1.0F).fieldOf("integrity").forGetter($$0x -> $$0x.integrity)
         )
         .apply($$0, BlockRotProcessor::new)
   );
   private final Optional<HolderSet<Block>> rottableBlocks;
   private final float integrity;

   public BlockRotProcessor(HolderSet<Block> $$0, float $$1) {
      this(Optional.of($$0), $$1);
   }

   public BlockRotProcessor(float $$0) {
      this(Optional.empty(), $$0);
   }

   private BlockRotProcessor(Optional<HolderSet<Block>> $$0, float $$1) {
      this.integrity = $$1;
      this.rottableBlocks = $$0;
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
      return (!this.rottableBlocks.isPresent() || $$3.state().is(this.rottableBlocks.get())) && !($$6.nextFloat() <= this.integrity) ? null : $$4;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_ROT;
   }
}
