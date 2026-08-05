/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.chat.contents;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.ObjectInfos;

public record ObjectContents(ObjectInfo contents) implements ComponentContents
{
    private static final String PLACEHOLDER = Character.toString('\ufffc');
    public static final MapCodec<ObjectContents> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ObjectInfos.CODEC.forGetter(ObjectContents::contents)).apply((Applicative)$$0, ObjectContents::new));

    public MapCodec<ObjectContents> codec() {
        return MAP_CODEC;
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
        return $$0.accept(this.contents.description());
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
        return $$0.accept($$1.withFont(this.contents.fontDescription()), PLACEHOLDER);
    }
}

