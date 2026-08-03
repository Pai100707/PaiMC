package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record Tool(List<Tool.Rule> rules, float defaultMiningSpeed, int damagePerBlock, boolean canDestroyBlocksInCreative) {
   public static final Codec<Tool> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Tool.Rule.CODEC.listOf().fieldOf("rules").forGetter(Tool::rules),
            Codec.FLOAT.optionalFieldOf("default_mining_speed", 1.0F).forGetter(Tool::defaultMiningSpeed),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("damage_per_block", 1).forGetter(Tool::damagePerBlock),
            Codec.BOOL.optionalFieldOf("can_destroy_blocks_in_creative", true).forGetter(Tool::canDestroyBlocksInCreative)
         )
         .apply($$0, Tool::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, Tool> STREAM_CODEC = StreamCodec.composite(
      Tool.Rule.STREAM_CODEC.apply(ByteBufCodecs.list()),
      Tool::rules,
      ByteBufCodecs.FLOAT,
      Tool::defaultMiningSpeed,
      ByteBufCodecs.VAR_INT,
      Tool::damagePerBlock,
      ByteBufCodecs.BOOL,
      Tool::canDestroyBlocksInCreative,
      Tool::new
   );

   public float getMiningSpeed(BlockState $$0) {
      for (Tool.Rule $$1 : this.rules) {
         if ($$1.speed.isPresent() && $$0.is($$1.blocks)) {
            return $$1.speed.get();
         }
      }

      return this.defaultMiningSpeed;
   }

   public boolean isCorrectForDrops(BlockState $$0) {
      for (Tool.Rule $$1 : this.rules) {
         if ($$1.correctForDrops.isPresent() && $$0.is($$1.blocks)) {
            return $$1.correctForDrops.get();
         }
      }

      return false;
   }

   public record Rule(HolderSet<Block> blocks, Optional<Float> speed, Optional<Boolean> correctForDrops) {
      public static final Codec<Tool.Rule> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(Tool.Rule::blocks),
               ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("speed").forGetter(Tool.Rule::speed),
               Codec.BOOL.optionalFieldOf("correct_for_drops").forGetter(Tool.Rule::correctForDrops)
            )
            .apply($$0, Tool.Rule::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, Tool.Rule> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.holderSet(Registries.BLOCK),
         Tool.Rule::blocks,
         ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional),
         Tool.Rule::speed,
         ByteBufCodecs.BOOL.apply(ByteBufCodecs::optional),
         Tool.Rule::correctForDrops,
         Tool.Rule::new
      );

      public static Tool.Rule minesAndDrops(HolderSet<Block> $$0, float $$1) {
         return new Tool.Rule($$0, Optional.of($$1), Optional.of(true));
      }

      public static Tool.Rule deniesDrops(HolderSet<Block> $$0) {
         return new Tool.Rule($$0, Optional.empty(), Optional.of(false));
      }

      public static Tool.Rule overrideSpeed(HolderSet<Block> $$0, float $$1) {
         return new Tool.Rule($$0, Optional.of($$1), Optional.empty());
      }
   }
}
