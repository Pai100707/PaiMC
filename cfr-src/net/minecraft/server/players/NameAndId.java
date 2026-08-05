/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.yggdrasil.response.NameAndId
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import org.jspecify.annotations.Nullable;

public record NameAndId(UUID id, String name) {
    public static final Codec<NameAndId> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(NameAndId::id), (App)Codec.STRING.fieldOf("name").forGetter(NameAndId::name)).apply((Applicative)$$0, NameAndId::new));

    public NameAndId(GameProfile $$0) {
        this($$0.id(), $$0.name());
    }

    public NameAndId(com.mojang.authlib.yggdrasil.response.NameAndId $$0) {
        this($$0.id(), $$0.name());
    }

    /*
     * WARNING - void declaration
     */
    public static @Nullable NameAndId fromJson(JsonObject $$0) {
        void $$4;
        if (!$$0.has("uuid") || !$$0.has("name")) {
            return null;
        }
        String $$1 = $$0.get("uuid").getAsString();
        try {
            UUID $$2 = UUID.fromString($$1);
        }
        catch (Throwable $$3) {
            return null;
        }
        return new NameAndId((UUID)$$4, $$0.get("name").getAsString());
    }

    public void appendTo(JsonObject $$0) {
        $$0.addProperty("uuid", this.id().toString());
        $$0.addProperty("name", this.name());
    }

    public static NameAndId createOffline(String $$0) {
        UUID $$1 = UUIDUtil.createOfflinePlayerUUID($$0);
        return new NameAndId($$1, $$0);
    }
}

