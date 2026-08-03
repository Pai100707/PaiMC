package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public record RuleBasedBlockStateProvider(BlockStateProvider fallback, List<RuleBasedBlockStateProvider.Rule> rules) {
   public static final Codec<RuleBasedBlockStateProvider> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockStateProvider.CODEC.fieldOf("fallback").forGetter(RuleBasedBlockStateProvider::fallback),
            RuleBasedBlockStateProvider.Rule.CODEC.listOf().fieldOf("rules").forGetter(RuleBasedBlockStateProvider::rules)
         )
         .apply($$0, RuleBasedBlockStateProvider::new)
   );

   public static RuleBasedBlockStateProvider simple(BlockStateProvider $$0) {
      return new RuleBasedBlockStateProvider($$0, List.of());
   }

   public static RuleBasedBlockStateProvider simple(Block $$0) {
      return simple(BlockStateProvider.simple($$0));
   }

   public BlockState getState(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, BlockPos $$2) {
      for (RuleBasedBlockStateProvider.Rule $$3 : this.rules) {
         if ($$3.ifTrue().test($$0, $$2)) {
            return $$3.then().getState($$1, $$2);
         }
      }

      return this.fallback.getState($$1, $$2);
   }

   public record Rule(BlockPredicate ifTrue, BlockStateProvider then) {
      public static final Codec<RuleBasedBlockStateProvider.Rule> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               BlockPredicate.CODEC.fieldOf("if_true").forGetter(RuleBasedBlockStateProvider.Rule::ifTrue),
               BlockStateProvider.CODEC.fieldOf("then").forGetter(RuleBasedBlockStateProvider.Rule::then)
            )
            .apply($$0, RuleBasedBlockStateProvider.Rule::new)
      );
   }
}
