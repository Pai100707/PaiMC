/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public interface ComponentContents {
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
        return Optional.empty();
    }

    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
        return Optional.empty();
    }

    default public MutableComponent resolve(@Nullable CommandSourceStack $$0, @Nullable Entity $$1, int $$2) throws CommandSyntaxException {
        return MutableComponent.create(this);
    }

    public MapCodec<? extends ComponentContents> codec();
}

