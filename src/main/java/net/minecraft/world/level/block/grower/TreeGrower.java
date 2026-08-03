package net.minecraft.world.level.block.grower;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jspecify.annotations.Nullable;

public final class TreeGrower {
   private static final Map<String, TreeGrower> GROWERS = new Object2ObjectArrayMap();
   public static final Codec<TreeGrower> CODEC = Codec.stringResolver($$0 -> $$0.name, GROWERS::get);
   public static final TreeGrower OAK = new TreeGrower(
      "oak",
      0.1F,
      Optional.empty(),
      Optional.empty(),
      Optional.of(TreeFeatures.OAK),
      Optional.of(TreeFeatures.FANCY_OAK),
      Optional.of(TreeFeatures.OAK_BEES_005),
      Optional.of(TreeFeatures.FANCY_OAK_BEES_005)
   );
   public static final TreeGrower SPRUCE = new TreeGrower(
      "spruce",
      0.5F,
      Optional.of(TreeFeatures.MEGA_SPRUCE),
      Optional.of(TreeFeatures.MEGA_PINE),
      Optional.of(TreeFeatures.SPRUCE),
      Optional.empty(),
      Optional.empty(),
      Optional.empty()
   );
   public static final TreeGrower MANGROVE = new TreeGrower(
      "mangrove",
      0.85F,
      Optional.empty(),
      Optional.empty(),
      Optional.of(TreeFeatures.MANGROVE),
      Optional.of(TreeFeatures.TALL_MANGROVE),
      Optional.empty(),
      Optional.empty()
   );
   public static final TreeGrower AZALEA = new TreeGrower("azalea", Optional.empty(), Optional.of(TreeFeatures.AZALEA_TREE), Optional.empty());
   public static final TreeGrower BIRCH = new TreeGrower("birch", Optional.empty(), Optional.of(TreeFeatures.BIRCH), Optional.of(TreeFeatures.BIRCH_BEES_005));
   public static final TreeGrower JUNGLE = new TreeGrower(
      "jungle", Optional.of(TreeFeatures.MEGA_JUNGLE_TREE), Optional.of(TreeFeatures.JUNGLE_TREE_NO_VINE), Optional.empty()
   );
   public static final TreeGrower ACACIA = new TreeGrower("acacia", Optional.empty(), Optional.of(TreeFeatures.ACACIA), Optional.empty());
   public static final TreeGrower CHERRY = new TreeGrower(
      "cherry", Optional.empty(), Optional.of(TreeFeatures.CHERRY), Optional.of(TreeFeatures.CHERRY_BEES_005)
   );
   public static final TreeGrower DARK_OAK = new TreeGrower("dark_oak", Optional.of(TreeFeatures.DARK_OAK), Optional.empty(), Optional.empty());
   public static final TreeGrower PALE_OAK = new TreeGrower("pale_oak", Optional.of(TreeFeatures.PALE_OAK_BONEMEAL), Optional.empty(), Optional.empty());
   private final String name;
   private final float secondaryChance;
   private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> megaTree;
   private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryMegaTree;
   private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> tree;
   private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryTree;
   private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> flowers;
   private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryFlowers;

   public TreeGrower(
      String $$0,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$1,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$2,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$3
   ) {
      this($$0, 0.0F, $$1, Optional.empty(), $$2, Optional.empty(), $$3, Optional.empty());
   }

   public TreeGrower(
      String $$0,
      float $$1,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$2,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$3,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$4,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$5,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$6,
      Optional<ResourceKey<ConfiguredFeature<?, ?>>> $$7
   ) {
      this.name = $$0;
      this.secondaryChance = $$1;
      this.megaTree = $$2;
      this.secondaryMegaTree = $$3;
      this.tree = $$4;
      this.secondaryTree = $$5;
      this.flowers = $$6;
      this.secondaryFlowers = $$7;
      GROWERS.put($$0, this);
   }

   @Nullable
   private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource $$0, boolean $$1) {
      if ($$0.nextFloat() < this.secondaryChance) {
         if ($$1 && this.secondaryFlowers.isPresent()) {
            return this.secondaryFlowers.get();
         }

         if (this.secondaryTree.isPresent()) {
            return this.secondaryTree.get();
         }
      }

      return $$1 && this.flowers.isPresent() ? this.flowers.get() : this.tree.orElse(null);
   }

   @Nullable
   private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource $$0) {
      return this.secondaryMegaTree.isPresent() && $$0.nextFloat() < this.secondaryChance ? this.secondaryMegaTree.get() : this.megaTree.orElse(null);
   }

   public boolean growTree(ServerLevel $$0, ChunkGenerator $$1, BlockPos $$2, BlockState $$3, RandomSource $$4) {
      ResourceKey<ConfiguredFeature<?, ?>> $$5 = this.getConfiguredMegaFeature($$4);
      if ($$5 != null) {
         Holder<ConfiguredFeature<?, ?>> $$6 = (Holder<ConfiguredFeature<?, ?>>)$$0.registryAccess()
            .lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .get($$5)
            .orElse(null);
         if ($$6 != null) {
            for (int $$7 = 0; $$7 >= -1; $$7--) {
               for (int $$8 = 0; $$8 >= -1; $$8--) {
                  if (isTwoByTwoSapling($$3, $$0, $$2, $$7, $$8)) {
                     ConfiguredFeature<?, ?> $$9 = (ConfiguredFeature<?, ?>)$$6.value();
                     BlockState $$10 = Blocks.AIR.defaultBlockState();
                     $$0.setBlock($$2.offset($$7, 0, $$8), $$10, 260);
                     $$0.setBlock($$2.offset($$7 + 1, 0, $$8), $$10, 260);
                     $$0.setBlock($$2.offset($$7, 0, $$8 + 1), $$10, 260);
                     $$0.setBlock($$2.offset($$7 + 1, 0, $$8 + 1), $$10, 260);
                     if ($$9.place($$0, $$1, $$4, $$2.offset($$7, 0, $$8))) {
                        return true;
                     }

                     $$0.setBlock($$2.offset($$7, 0, $$8), $$3, 260);
                     $$0.setBlock($$2.offset($$7 + 1, 0, $$8), $$3, 260);
                     $$0.setBlock($$2.offset($$7, 0, $$8 + 1), $$3, 260);
                     $$0.setBlock($$2.offset($$7 + 1, 0, $$8 + 1), $$3, 260);
                     return false;
                  }
               }
            }
         }
      }

      ResourceKey<ConfiguredFeature<?, ?>> $$11 = this.getConfiguredFeature($$4, this.hasFlowers($$0, $$2));
      if ($$11 == null) {
         return false;
      } else {
         Holder<ConfiguredFeature<?, ?>> $$12 = (Holder<ConfiguredFeature<?, ?>>)$$0.registryAccess()
            .lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .get($$11)
            .orElse(null);
         if ($$12 == null) {
            return false;
         } else {
            ConfiguredFeature<?, ?> $$13 = (ConfiguredFeature<?, ?>)$$12.value();
            BlockState $$14 = $$0.getFluidState($$2).createLegacyBlock();
            $$0.setBlock($$2, $$14, 260);
            if ($$13.place($$0, $$1, $$4, $$2)) {
               if ($$0.getBlockState($$2) == $$14) {
                  $$0.sendBlockUpdated($$2, $$3, $$14, 2);
               }

               return true;
            } else {
               $$0.setBlock($$2, $$3, 260);
               return false;
            }
         }
      }
   }

   private static boolean isTwoByTwoSapling(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, int $$3, int $$4) {
      Block $$5 = $$0.getBlock();
      return $$1.getBlockState($$2.offset($$3, 0, $$4)).is($$5)
         && $$1.getBlockState($$2.offset($$3 + 1, 0, $$4)).is($$5)
         && $$1.getBlockState($$2.offset($$3, 0, $$4 + 1)).is($$5)
         && $$1.getBlockState($$2.offset($$3 + 1, 0, $$4 + 1)).is($$5);
   }

   private boolean hasFlowers(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      for (BlockPos $$2 : MutableBlockPos.betweenClosed($$1.below().north(2).west(2), $$1.above().south(2).east(2))) {
         if ($$0.getBlockState($$2).is(BlockTags.FLOWERS)) {
            return true;
         }
      }

      return false;
   }
}
