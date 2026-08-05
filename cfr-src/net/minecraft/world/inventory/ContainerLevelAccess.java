/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ContainerLevelAccess {
    public static final ContainerLevelAccess NULL = new ContainerLevelAccess(){

        @Override
        public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> $$0) {
            return Optional.empty();
        }
    };

    public static ContainerLevelAccess create(final Level $$0, final BlockPos $$1) {
        return new ContainerLevelAccess(){

            @Override
            public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> $$02) {
                return Optional.of($$02.apply($$0, $$1));
            }
        };
    }

    public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> var1);

    default public <T> T evaluate(BiFunction<Level, BlockPos, T> $$0, T $$1) {
        return this.evaluate($$0).orElse($$1);
    }

    default public void execute(BiConsumer<Level, BlockPos> $$0) {
        this.evaluate(($$1, $$2) -> {
            $$0.accept((Level)$$1, (BlockPos)$$2);
            return Optional.empty();
        });
    }
}

