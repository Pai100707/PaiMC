/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.chat.contents.objects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.resources.Identifier;

public record AtlasSprite(Identifier atlas, Identifier sprite) implements ObjectInfo
{
    public static final Identifier DEFAULT_ATLAS = AtlasIds.BLOCKS;
    public static final MapCodec<AtlasSprite> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Identifier.CODEC.optionalFieldOf("atlas", (Object)DEFAULT_ATLAS).forGetter(AtlasSprite::atlas), (App)Identifier.CODEC.fieldOf("sprite").forGetter(AtlasSprite::sprite)).apply((Applicative)$$0, AtlasSprite::new));

    public MapCodec<AtlasSprite> codec() {
        return MAP_CODEC;
    }

    @Override
    public FontDescription fontDescription() {
        return new FontDescription.AtlasSprite(this.atlas, this.sprite);
    }

    private static String toShortName(Identifier $$0) {
        return $$0.getNamespace().equals("minecraft") ? $$0.getPath() : $$0.toString();
    }

    @Override
    public String description() {
        String $$0 = AtlasSprite.toShortName(this.sprite);
        if (this.atlas.equals(DEFAULT_ATLAS)) {
            return "[" + $$0 + "]";
        }
        return "[" + $$0 + "@" + AtlasSprite.toShortName(this.atlas) + "]";
    }
}

