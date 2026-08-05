/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

public enum ChatVisiblity {
    FULL(0, "options.chat.visibility.full"),
    SYSTEM(1, "options.chat.visibility.system"),
    HIDDEN(2, "options.chat.visibility.hidden");

    private static final IntFunction<ChatVisiblity> BY_ID;
    public static final Codec<ChatVisiblity> LEGACY_CODEC;
    private final int id;
    private final Component caption;

    private ChatVisiblity(int $$0, String $$1) {
        this.id = $$0;
        this.caption = Component.translatable($$1);
    }

    public Component caption() {
        return this.caption;
    }

    static {
        BY_ID = ByIdMap.continuous($$0 -> $$0.id, ChatVisiblity.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, $$0 -> $$0.id);
    }
}

