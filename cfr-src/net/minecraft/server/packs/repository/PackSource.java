/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.repository;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface PackSource {
    public static final UnaryOperator<Component> NO_DECORATION = UnaryOperator.identity();
    public static final PackSource DEFAULT = PackSource.create(NO_DECORATION, true);
    public static final PackSource BUILT_IN = PackSource.create(PackSource.decorateWithSource("pack.source.builtin"), true);
    public static final PackSource FEATURE = PackSource.create(PackSource.decorateWithSource("pack.source.feature"), false);
    public static final PackSource WORLD = PackSource.create(PackSource.decorateWithSource("pack.source.world"), true);
    public static final PackSource SERVER = PackSource.create(PackSource.decorateWithSource("pack.source.server"), true);

    public Component decorate(Component var1);

    public boolean shouldAddAutomatically();

    public static PackSource create(final UnaryOperator<Component> $$0, final boolean $$1) {
        return new PackSource(){

            @Override
            public Component decorate(Component $$02) {
                return (Component)$$0.apply($$02);
            }

            @Override
            public boolean shouldAddAutomatically() {
                return $$1;
            }
        };
    }

    private static UnaryOperator<Component> decorateWithSource(String $$0) {
        MutableComponent $$12 = Component.translatable($$0);
        return $$1 -> Component.translatable("pack.nameAndSource", $$1, $$12).withStyle(ChatFormatting.GRAY);
    }
}

