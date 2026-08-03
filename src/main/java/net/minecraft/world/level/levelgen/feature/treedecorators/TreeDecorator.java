package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class TreeDecorator {
   public static final Codec<TreeDecorator> CODEC = BuiltInRegistries.TREE_DECORATOR_TYPE.byNameCodec().dispatch(TreeDecorator::type, TreeDecoratorType::codec);

   protected abstract TreeDecoratorType<?> type();

   public abstract void place(TreeDecorator.Context var1);

   public static final class Context {
      private final net.minecraft.world.level.LevelSimulatedReader level;
      private final BiConsumer<BlockPos, BlockState> decorationSetter;
      private final RandomSource random;
      private final ObjectArrayList<BlockPos> logs;
      private final ObjectArrayList<BlockPos> leaves;
      private final ObjectArrayList<BlockPos> roots;

      public Context(
         net.minecraft.world.level.LevelSimulatedReader $$0,
         BiConsumer<BlockPos, BlockState> $$1,
         RandomSource $$2,
         Set<BlockPos> $$3,
         Set<BlockPos> $$4,
         Set<BlockPos> $$5
      ) {
         this.level = $$0;
         this.decorationSetter = $$1;
         this.random = $$2;
         this.roots = new ObjectArrayList($$5);
         this.logs = new ObjectArrayList($$3);
         this.leaves = new ObjectArrayList($$4);
         this.logs.sort(Comparator.comparingInt(Vec3i::getY));
         this.leaves.sort(Comparator.comparingInt(Vec3i::getY));
         this.roots.sort(Comparator.comparingInt(Vec3i::getY));
      }

      public void placeVine(BlockPos $$0, BooleanProperty $$1) {
         this.setBlock($$0, Blocks.VINE.defaultBlockState().setValue($$1, true));
      }

      public void setBlock(BlockPos $$0, BlockState $$1) {
         this.decorationSetter.accept($$0, $$1);
      }

      public boolean isAir(BlockPos $$0) {
         return this.level.isStateAtPosition($$0, BlockBehaviour.BlockStateBase::isAir);
      }

      public boolean checkBlock(BlockPos $$0, Predicate<BlockState> $$1) {
         return this.level.isStateAtPosition($$0, $$1);
      }

      public net.minecraft.world.level.LevelSimulatedReader level() {
         return this.level;
      }

      public RandomSource random() {
         return this.random;
      }

      public ObjectArrayList<BlockPos> logs() {
         return this.logs;
      }

      public ObjectArrayList<BlockPos> leaves() {
         return this.leaves;
      }

      public ObjectArrayList<BlockPos> roots() {
         return this.roots;
      }
   }
}
