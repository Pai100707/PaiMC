/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public record SelectorContents(SelectorPattern selector, Optional<Component> separator) implements ComponentContents
{
    public static final MapCodec<SelectorContents> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)SelectorPattern.CODEC.fieldOf("selector").forGetter(SelectorContents::selector), (App)ComponentSerialization.CODEC.optionalFieldOf("separator").forGetter(SelectorContents::separator)).apply((Applicative)$$0, SelectorContents::new));

    public MapCodec<SelectorContents> codec() {
        return MAP_CODEC;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack $$0, @Nullable Entity $$1, int $$2) throws CommandSyntaxException {
        if ($$0 == null) {
            return Component.empty();
        }
        Optional<MutableComponent> $$3 = ComponentUtils.updateForEntity($$0, this.separator, $$1, $$2);
        return ComponentUtils.formatList(this.selector.resolved().findEntities($$0), $$3, Entity::getDisplayName);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
        return $$0.accept($$1, this.selector.pattern());
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
        return $$0.accept(this.selector.pattern());
    }

    @Override
    public String toString() {
        return "pattern{" + String.valueOf(this.selector) + "}";
    }
}

