package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockPredicate(
   Optional<HolderSet<Block>> blocks, Optional<StatePropertiesPredicate> properties, Optional<NbtPredicate> nbt, DataComponentMatchers components
) {
   public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("blocks").forGetter(BlockPredicate::blocks),
            StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(BlockPredicate::properties),
            NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(BlockPredicate::nbt),
            DataComponentMatchers.CODEC.forGetter(BlockPredicate::components)
         )
         .apply($$0, BlockPredicate::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, BlockPredicate> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.BLOCK)),
      BlockPredicate::blocks,
      ByteBufCodecs.optional(StatePropertiesPredicate.STREAM_CODEC),
      BlockPredicate::properties,
      ByteBufCodecs.optional(NbtPredicate.STREAM_CODEC),
      BlockPredicate::nbt,
      DataComponentMatchers.STREAM_CODEC,
      BlockPredicate::components,
      BlockPredicate::new
   );

   public boolean matches(ServerLevel $$0, BlockPos $$1) {
      if (!$$0.isLoaded($$1)) {
         return false;
      } else if (!this.matchesState($$0.getBlockState($$1))) {
         return false;
      } else {
         if (this.nbt.isPresent() || !this.components.isEmpty()) {
            BlockEntity $$2 = $$0.getBlockEntity($$1);
            if (this.nbt.isPresent() && !matchesBlockEntity($$0, $$2, this.nbt.get())) {
               return false;
            }

            if (!this.components.isEmpty() && !matchesComponents($$2, this.components)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean matches(BlockInWorld $$0) {
      return !this.matchesState($$0.getState()) ? false : !this.nbt.isPresent() || matchesBlockEntity($$0.getLevel(), $$0.getEntity(), this.nbt.get());
   }

   private boolean matchesState(BlockState $$0) {
      return this.blocks.isPresent() && !$$0.is(this.blocks.get()) ? false : !this.properties.isPresent() || this.properties.get().matches($$0);
   }

   private static boolean matchesBlockEntity(LevelReader $$0, BlockEntity $$1, NbtPredicate $$2) {
      return $$1 != null && $$2.matches($$1.saveWithFullMetadata($$0.registryAccess()));
   }

   private static boolean matchesComponents(BlockEntity $$0, DataComponentMatchers $$1) {
      return $$0 != null && $$1.test((DataComponentGetter)$$0.collectComponents());
   }

   public boolean requiresNbt() {
      return this.nbt.isPresent();
   }

   public static class Builder {
      private Optional<HolderSet<Block>> blocks = Optional.empty();
      private Optional<StatePropertiesPredicate> properties = Optional.empty();
      private Optional<NbtPredicate> nbt = Optional.empty();
      private DataComponentMatchers components = DataComponentMatchers.ANY;

      private Builder() {
      }

      public static BlockPredicate.Builder block() {
         return new BlockPredicate.Builder();
      }

      public BlockPredicate.Builder of(HolderGetter<Block> $$0, Block... $$1) {
         return this.of($$0, Arrays.asList($$1));
      }

      public BlockPredicate.Builder of(HolderGetter<Block> $$0, Collection<Block> $$1) {
         this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, $$1));
         return this;
      }

      public BlockPredicate.Builder of(HolderGetter<Block> $$0, TagKey<Block> $$1) {
         this.blocks = Optional.of($$0.getOrThrow($$1));
         return this;
      }

      public BlockPredicate.Builder hasNbt(CompoundTag $$0) {
         this.nbt = Optional.of(new NbtPredicate($$0));
         return this;
      }

      public BlockPredicate.Builder setProperties(StatePropertiesPredicate.Builder $$0) {
         this.properties = $$0.build();
         return this;
      }

      public BlockPredicate.Builder components(DataComponentMatchers $$0) {
         this.components = $$0;
         return this;
      }

      public BlockPredicate build() {
         return new BlockPredicate(this.blocks, this.properties, this.nbt, this.components);
      }
   }
}
