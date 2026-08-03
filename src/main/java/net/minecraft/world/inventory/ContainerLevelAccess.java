package net.minecraft.world.inventory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ContainerLevelAccess {
   net.minecraft.world.inventory.ContainerLevelAccess NULL = new net.minecraft.world.inventory.ContainerLevelAccess() {
      @Override
      public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> $$0) {
         return Optional.empty();
      }
   };

   static net.minecraft.world.inventory.ContainerLevelAccess create(final Level $$0, final BlockPos $$1) {
      return new net.minecraft.world.inventory.ContainerLevelAccess() {
         @Override
         public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> $$0x) {
            return Optional.of($$0.apply($$0, $$1));
         }
      };
   }

   <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> var1);

   default <T> T evaluate(BiFunction<Level, BlockPos, T> $$0, T $$1) {
      return this.evaluate($$0).orElse($$1);
   }

   default void execute(BiConsumer<Level, BlockPos> $$0) {
      this.evaluate(($$1, $$2) -> {
         $$0.accept($$1, $$2);
         return Optional.empty();
      });
   }
}
