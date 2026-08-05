/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;

public abstract class TestFunctionLoader {
    private static final List<TestFunctionLoader> loaders = new ArrayList<TestFunctionLoader>();

    public static void registerLoader(TestFunctionLoader $$0) {
        loaders.add($$0);
    }

    public static void runLoaders(Registry<Consumer<GameTestHelper>> $$0) {
        for (TestFunctionLoader $$12 : loaders) {
            $$12.load(($$1, $$2) -> Registry.register($$0, $$1, $$2));
        }
    }

    public abstract void load(BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> var1);
}

