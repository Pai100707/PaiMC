/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.player;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jspecify.annotations.Nullable;

public record PlayerSkin(ClientAsset.Texture body, @Nullable ClientAsset.Texture cape, @Nullable ClientAsset.Texture elytra, PlayerModelType model, boolean secure) {
    public static PlayerSkin insecure(ClientAsset.Texture $$0, @Nullable ClientAsset.Texture $$1, @Nullable ClientAsset.Texture $$2, PlayerModelType $$3) {
        return new PlayerSkin($$0, $$1, $$2, $$3, false);
    }

    public PlayerSkin with(Patch $$0) {
        if ($$0.equals(Patch.EMPTY)) {
            return this;
        }
        return PlayerSkin.insecure((ClientAsset.Texture)DataFixUtils.orElse($$0.body, (Object)this.body), (ClientAsset.Texture)DataFixUtils.orElse($$0.cape, (Object)this.cape), (ClientAsset.Texture)DataFixUtils.orElse($$0.elytra, (Object)this.elytra), $$0.model.orElse(this.model));
    }

    public static final class Patch
    extends Record {
        final Optional<ClientAsset.ResourceTexture> body;
        final Optional<ClientAsset.ResourceTexture> cape;
        final Optional<ClientAsset.ResourceTexture> elytra;
        final Optional<PlayerModelType> model;
        public static final Patch EMPTY = new Patch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        public static final MapCodec<Patch> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ClientAsset.ResourceTexture.CODEC.optionalFieldOf("texture").forGetter(Patch::body), (App)ClientAsset.ResourceTexture.CODEC.optionalFieldOf("cape").forGetter(Patch::cape), (App)ClientAsset.ResourceTexture.CODEC.optionalFieldOf("elytra").forGetter(Patch::elytra), (App)PlayerModelType.CODEC.optionalFieldOf("model").forGetter(Patch::model)).apply((Applicative)$$0, Patch::create));
        public static final StreamCodec<ByteBuf, Patch> STREAM_CODEC = StreamCodec.composite(ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::body, ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::cape, ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::elytra, PlayerModelType.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::model, Patch::create);

        public Patch(Optional<ClientAsset.ResourceTexture> $$0, Optional<ClientAsset.ResourceTexture> $$1, Optional<ClientAsset.ResourceTexture> $$2, Optional<PlayerModelType> $$3) {
            this.body = $$0;
            this.cape = $$1;
            this.elytra = $$2;
            this.model = $$3;
        }

        public static Patch create(Optional<ClientAsset.ResourceTexture> $$0, Optional<ClientAsset.ResourceTexture> $$1, Optional<ClientAsset.ResourceTexture> $$2, Optional<PlayerModelType> $$3) {
            if ($$0.isEmpty() && $$1.isEmpty() && $$2.isEmpty() && $$3.isEmpty()) {
                return EMPTY;
            }
            return new Patch($$0, $$1, $$2, $$3);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Patch.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Patch.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Patch.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this, $$0);
        }

        public Optional<ClientAsset.ResourceTexture> body() {
            return this.body;
        }

        public Optional<ClientAsset.ResourceTexture> cape() {
            return this.cape;
        }

        public Optional<ClientAsset.ResourceTexture> elytra() {
            return this.elytra;
        }

        public Optional<PlayerModelType> model() {
            return this.model;
        }
    }
}

