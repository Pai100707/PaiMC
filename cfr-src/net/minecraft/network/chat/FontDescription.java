/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;

public interface FontDescription {
    public static final Codec<FontDescription> CODEC = Identifier.CODEC.flatComapMap(Resource::new, $$0 -> {
        if ($$0 instanceof Resource) {
            Resource $$1 = (Resource)$$0;
            return DataResult.success((Object)$$1.id());
        }
        return DataResult.error(() -> "Unsupported font description type: " + String.valueOf($$0));
    });
    public static final Resource DEFAULT = new Resource(Identifier.withDefaultNamespace("default"));

    public record Resource(Identifier id) implements FontDescription
    {
    }

    public record PlayerSprite(ResolvableProfile profile, boolean hat) implements FontDescription
    {
    }

    public record AtlasSprite(Identifier atlasId, Identifier spriteId) implements FontDescription
    {
    }
}

