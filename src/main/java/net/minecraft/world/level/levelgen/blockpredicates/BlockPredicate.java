package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface BlockPredicate extends BiPredicate<net.minecraft.world.level.WorldGenLevel, BlockPos> {
   Codec<BlockPredicate> CODEC = BuiltInRegistries.BLOCK_PREDICATE_TYPE.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
   BlockPredicate ONLY_IN_AIR_PREDICATE = matchesBlocks(Blocks.AIR);
   BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = matchesBlocks(Blocks.AIR, Blocks.WATER);

   BlockPredicateType<?> type();

   static BlockPredicate allOf(List<BlockPredicate> $$0) {
      return new AllOfPredicate($$0);
   }

   static BlockPredicate allOf(BlockPredicate... $$0) {
      return allOf(List.of($$0));
   }

   static BlockPredicate allOf(BlockPredicate $$0, BlockPredicate $$1) {
      return allOf(List.of($$0, $$1));
   }

   static BlockPredicate anyOf(List<BlockPredicate> $$0) {
      return new AnyOfPredicate($$0);
   }

   static BlockPredicate anyOf(BlockPredicate... $$0) {
      return anyOf(List.of($$0));
   }

   static BlockPredicate anyOf(BlockPredicate $$0, BlockPredicate $$1) {
      return anyOf(List.of($$0, $$1));
   }

   static BlockPredicate matchesBlocks(Vec3i $$0, List<Block> $$1) {
      return new MatchingBlocksPredicate($$0, HolderSet.direct(Block::builtInRegistryHolder, $$1));
   }

   static BlockPredicate matchesBlocks(List<Block> $$0) {
      return matchesBlocks(Vec3i.ZERO, $$0);
   }

   static BlockPredicate matchesBlocks(Vec3i $$0, Block... $$1) {
      return matchesBlocks($$0, List.of($$1));
   }

   static BlockPredicate matchesBlocks(Block... $$0) {
      return matchesBlocks(Vec3i.ZERO, $$0);
   }

   static BlockPredicate matchesTag(Vec3i $$0, TagKey<Block> $$1) {
      return new MatchingBlockTagPredicate($$0, $$1);
   }

   static BlockPredicate matchesTag(TagKey<Block> $$0) {
      return matchesTag(Vec3i.ZERO, $$0);
   }

   static BlockPredicate matchesFluids(Vec3i $$0, List<Fluid> $$1) {
      return new MatchingFluidsPredicate($$0, HolderSet.direct(Fluid::builtInRegistryHolder, $$1));
   }

   static BlockPredicate matchesFluids(Vec3i $$0, Fluid... $$1) {
      return matchesFluids($$0, List.of($$1));
   }

   static BlockPredicate matchesFluids(Fluid... $$0) {
      return matchesFluids(Vec3i.ZERO, $$0);
   }

   static BlockPredicate not(BlockPredicate $$0) {
      return new NotPredicate($$0);
   }

   static BlockPredicate replaceable(Vec3i $$0) {
      return new ReplaceablePredicate($$0);
   }

   static BlockPredicate replaceable() {
      return replaceable(Vec3i.ZERO);
   }

   static BlockPredicate wouldSurvive(BlockState $$0, Vec3i $$1) {
      return new WouldSurvivePredicate($$1, $$0);
   }

   static BlockPredicate hasSturdyFace(Vec3i $$0, Direction $$1) {
      return new HasSturdyFacePredicate($$0, $$1);
   }

   static BlockPredicate hasSturdyFace(Direction $$0) {
      return hasSturdyFace(Vec3i.ZERO, $$0);
   }

   static BlockPredicate solid(Vec3i $$0) {
      return new SolidPredicate($$0);
   }

   static BlockPredicate solid() {
      return solid(Vec3i.ZERO);
   }

   static BlockPredicate noFluid() {
      return noFluid(Vec3i.ZERO);
   }

   static BlockPredicate noFluid(Vec3i $$0) {
      return matchesFluids($$0, Fluids.EMPTY);
   }

   static BlockPredicate insideWorld(Vec3i $$0) {
      return new InsideWorldBoundsPredicate($$0);
   }

   static BlockPredicate alwaysTrue() {
      return TrueBlockPredicate.INSTANCE;
   }

   static BlockPredicate unobstructed(Vec3i $$0) {
      return new UnobstructedPredicate($$0);
   }

   static BlockPredicate unobstructed() {
      return unobstructed(Vec3i.ZERO);
   }
}
