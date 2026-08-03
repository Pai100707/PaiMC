package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
   private final HolderLookup<Block> blocks;

   public BlockPredicateArgument(net.minecraft.commands.CommandBuildContext $$0) {
      this.blocks = $$0.lookupOrThrow(Registries.BLOCK);
   }

   public static BlockPredicateArgument blockPredicate(net.minecraft.commands.CommandBuildContext $$0) {
      return new BlockPredicateArgument($$0);
   }

   public BlockPredicateArgument.Result parse(StringReader $$0) throws CommandSyntaxException {
      return parse(this.blocks, $$0);
   }

   public static BlockPredicateArgument.Result parse(HolderLookup<Block> $$0, StringReader $$1) throws CommandSyntaxException {
      return (BlockPredicateArgument.Result)BlockStateParser.parseForTesting($$0, $$1, true)
         .map(
            $$0x -> new BlockPredicateArgument.BlockPredicate($$0x.blockState(), $$0x.properties().keySet(), $$0x.nbt()),
            $$0x -> new BlockPredicateArgument.TagPredicate($$0x.tag(), $$0x.vagueProperties(), $$0x.nbt())
         );
   }

   public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return (Predicate<BlockInWorld>)$$0.getArgument($$1, BlockPredicateArgument.Result.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return BlockStateParser.fillSuggestions(this.blocks, $$1, true, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static class BlockPredicate implements BlockPredicateArgument.Result {
      private final BlockState state;
      private final Set<Property<?>> properties;
      @Nullable
      private final CompoundTag nbt;

      public BlockPredicate(BlockState $$0, Set<Property<?>> $$1, @Nullable CompoundTag $$2) {
         this.state = $$0;
         this.properties = $$1;
         this.nbt = $$2;
      }

      public boolean test(BlockInWorld $$0) {
         BlockState $$1 = $$0.getState();
         if (!$$1.is(this.state.getBlock())) {
            return false;
         } else {
            for (Property<?> $$2 : this.properties) {
               if ($$1.getValue($$2) != this.state.getValue($$2)) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity $$3 = $$0.getEntity();
               return $$3 != null && NbtUtils.compareNbt(this.nbt, $$3.saveWithFullMetadata($$0.getLevel().registryAccess()), true);
            }
         }
      }

      @Override
      public boolean requiresNbt() {
         return this.nbt != null;
      }
   }

   public interface Result extends Predicate<BlockInWorld> {
      boolean requiresNbt();
   }

   static class TagPredicate implements BlockPredicateArgument.Result {
      private final HolderSet<Block> tag;
      @Nullable
      private final CompoundTag nbt;
      private final Map<String, String> vagueProperties;

      TagPredicate(HolderSet<Block> $$0, Map<String, String> $$1, @Nullable CompoundTag $$2) {
         this.tag = $$0;
         this.vagueProperties = $$1;
         this.nbt = $$2;
      }

      public boolean test(BlockInWorld $$0) {
         BlockState $$1 = $$0.getState();
         if (!$$1.is(this.tag)) {
            return false;
         } else {
            for (Entry<String, String> $$2 : this.vagueProperties.entrySet()) {
               Property<?> $$3 = $$1.getBlock().getStateDefinition().getProperty($$2.getKey());
               if ($$3 == null) {
                  return false;
               }

               Comparable<?> $$4 = (Comparable<?>)$$3.getValue($$2.getValue()).orElse(null);
               if ($$4 == null) {
                  return false;
               }

               if ($$1.getValue($$3) != $$4) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity $$5 = $$0.getEntity();
               return $$5 != null && NbtUtils.compareNbt(this.nbt, $$5.saveWithFullMetadata($$0.getLevel().registryAccess()), true);
            }
         }
      }

      @Override
      public boolean requiresNbt() {
         return this.nbt != null;
      }
   }
}
