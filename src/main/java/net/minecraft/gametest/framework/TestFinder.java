package net.minecraft.gametest.framework;

import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;

public class TestFinder implements TestInstanceFinder, TestPosFinder {
   static final TestInstanceFinder NO_FUNCTIONS = Stream::empty;
   static final TestPosFinder NO_STRUCTURES = Stream::empty;
   private final TestInstanceFinder testInstanceFinder;
   private final TestPosFinder testPosFinder;
   private final CommandSourceStack source;

   @Override
   public Stream<BlockPos> findTestPos() {
      return this.testPosFinder.findTestPos();
   }

   public static TestFinder.Builder builder() {
      return new TestFinder.Builder();
   }

   TestFinder(CommandSourceStack $$0, TestInstanceFinder $$1, TestPosFinder $$2) {
      this.source = $$0;
      this.testInstanceFinder = $$1;
      this.testPosFinder = $$2;
   }

   public CommandSourceStack source() {
      return this.source;
   }

   @Override
   public Stream<Reference<GameTestInstance>> findTests() {
      return this.testInstanceFinder.findTests();
   }

   public static class Builder {
      private final UnaryOperator<Supplier<Stream<Reference<GameTestInstance>>>> testFinderWrapper;
      private final UnaryOperator<Supplier<Stream<BlockPos>>> structureBlockPosFinderWrapper;

      public Builder() {
         this.testFinderWrapper = $$0 -> $$0;
         this.structureBlockPosFinderWrapper = $$0 -> $$0;
      }

      private Builder(UnaryOperator<Supplier<Stream<Reference<GameTestInstance>>>> $$0, UnaryOperator<Supplier<Stream<BlockPos>>> $$1) {
         this.testFinderWrapper = $$0;
         this.structureBlockPosFinderWrapper = $$1;
      }

      public TestFinder.Builder createMultipleCopies(int $$0) {
         return new TestFinder.Builder(createCopies($$0), createCopies($$0));
      }

      private static <Q> UnaryOperator<Supplier<Stream<Q>>> createCopies(int $$0) {
         return $$1 -> {
            List<Q> $$2 = new LinkedList<>();
            List<Q> $$3 = ((Stream)$$1.get()).toList();

            for (int $$4 = 0; $$4 < $$0; $$4++) {
               $$2.addAll($$3);
            }

            return $$2::stream;
         };
      }

      private TestFinder build(CommandSourceStack $$0, TestInstanceFinder $$1, TestPosFinder $$2) {
         return new TestFinder($$0, this.testFinderWrapper.apply($$1::findTests)::get, this.structureBlockPosFinderWrapper.apply($$2::findTestPos)::get);
      }

      public TestFinder radius(CommandContext<CommandSourceStack> $$0, int $$1) {
         CommandSourceStack $$2 = (CommandSourceStack)$$0.getSource();
         BlockPos $$3 = BlockPos.containing($$2.getPosition());
         return this.build($$2, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findTestBlocks($$3, $$1, $$2.getLevel()));
      }

      public TestFinder nearest(CommandContext<CommandSourceStack> $$0) {
         CommandSourceStack $$1 = (CommandSourceStack)$$0.getSource();
         BlockPos $$2 = BlockPos.containing($$1.getPosition());
         return this.build($$1, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findNearestTest($$2, 15, $$1.getLevel()).stream());
      }

      public TestFinder allNearby(CommandContext<CommandSourceStack> $$0) {
         CommandSourceStack $$1 = (CommandSourceStack)$$0.getSource();
         BlockPos $$2 = BlockPos.containing($$1.getPosition());
         return this.build($$1, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findTestBlocks($$2, 250, $$1.getLevel()));
      }

      public TestFinder lookedAt(CommandContext<CommandSourceStack> $$0) {
         CommandSourceStack $$1 = (CommandSourceStack)$$0.getSource();
         return this.build(
            $$1,
            TestFinder.NO_FUNCTIONS,
            () -> StructureUtils.lookedAtTestPos(BlockPos.containing($$1.getPosition()), $$1.getPlayer().getCamera(), $$1.getLevel())
         );
      }

      public TestFinder failedTests(CommandContext<CommandSourceStack> $$0, boolean $$1) {
         return this.build(
            (CommandSourceStack)$$0.getSource(),
            () -> FailedTestTracker.getLastFailedTests().filter($$1x -> !$$1 || ((GameTestInstance)$$1x.value()).required()),
            TestFinder.NO_STRUCTURES
         );
      }

      public TestFinder byResourceSelection(CommandContext<CommandSourceStack> $$0, Collection<Reference<GameTestInstance>> $$1) {
         return this.build((CommandSourceStack)$$0.getSource(), $$1::stream, TestFinder.NO_STRUCTURES);
      }

      public TestFinder failedTests(CommandContext<CommandSourceStack> $$0) {
         return this.failedTests($$0, false);
      }
   }
}
