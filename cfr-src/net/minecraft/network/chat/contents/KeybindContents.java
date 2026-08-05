/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat.contents;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindResolver;
import org.jspecify.annotations.Nullable;

public class KeybindContents
implements ComponentContents {
    public static final MapCodec<KeybindContents> MAP_CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)Codec.STRING.fieldOf("keybind").forGetter($$0 -> $$0.name)).apply((Applicative)$$02, KeybindContents::new));
    private final String name;
    private @Nullable Supplier<Component> nameResolver;

    public KeybindContents(String $$0) {
        this.name = $$0;
    }

    private Component getNestedComponent() {
        if (this.nameResolver == null) {
            this.nameResolver = KeybindResolver.keyResolver.apply(this.name);
        }
        return this.nameResolver.get();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
        return this.getNestedComponent().visit($$0);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
        return this.getNestedComponent().visit($$0, $$1);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof KeybindContents)) return false;
        KeybindContents $$1 = (KeybindContents)$$0;
        if (!this.name.equals($$1.name)) return false;
        return true;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString() {
        return "keybind{" + this.name + "}";
    }

    public String getName() {
        return this.name;
    }

    public MapCodec<KeybindContents> codec() {
        return MAP_CODEC;
    }
}

