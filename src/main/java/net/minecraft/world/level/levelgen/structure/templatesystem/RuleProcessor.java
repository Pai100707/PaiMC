package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class RuleProcessor extends StructureProcessor {
   public static final MapCodec<RuleProcessor> CODEC = ProcessorRule.CODEC.listOf().fieldOf("rules").xmap(RuleProcessor::new, $$0 -> $$0.rules);
   private final ImmutableList<ProcessorRule> rules;

   public RuleProcessor(List<? extends ProcessorRule> $$0) {
      this.rules = ImmutableList.copyOf($$0);
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
      RandomSource $$6 = RandomSource.create(Mth.getSeed($$4.pos()));
      BlockState $$7 = $$0.getBlockState($$4.pos());
      UnmodifiableIterator var9 = this.rules.iterator();

      while (var9.hasNext()) {
         ProcessorRule $$8 = (ProcessorRule)var9.next();
         if ($$8.test($$4.state(), $$7, $$3.pos(), $$4.pos(), $$2, $$6)) {
            return new StructureTemplate.StructureBlockInfo($$4.pos(), $$8.getOutputState(), $$8.getOutputTag($$6, $$4.nbt()));
         }
      }

      return $$4;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.RULE;
   }
}
