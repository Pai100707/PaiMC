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
package net.minecraft.server.jsonrpc.api;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import org.jspecify.annotations.Nullable;

public record MethodInfo<Params, Result>(String description, Optional<ParamInfo<Params>> params, Optional<ResultInfo<Result>> result) {
    public MethodInfo(String $$0, @Nullable ParamInfo<Params> $$1, @Nullable ResultInfo<Result> $$2) {
        this($$0, Optional.ofNullable($$1), Optional.ofNullable($$2));
    }

    private static <Params> Optional<ParamInfo<Params>> toOptional(List<ParamInfo<Params>> $$0) {
        return $$0.isEmpty() ? Optional.empty() : Optional.of($$0.getFirst());
    }

    private static <Params> List<ParamInfo<Params>> toList(Optional<ParamInfo<Params>> $$0) {
        if ($$0.isPresent()) {
            return List.of($$0.get());
        }
        return List.of();
    }

    private static <Params> Codec<Optional<ParamInfo<Params>>> paramsTypedCodec() {
        return ParamInfo.typedCodec().codec().listOf().xmap(MethodInfo::toOptional, MethodInfo::toList);
    }

    static <Params, Result> MapCodec<MethodInfo<Params, Result>> typedCodec() {
        return RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("description").forGetter(MethodInfo::description), (App)MethodInfo.paramsTypedCodec().fieldOf("params").forGetter(MethodInfo::params), (App)ResultInfo.typedCodec().optionalFieldOf("result").forGetter(MethodInfo::result)).apply((Applicative)$$0, MethodInfo::new));
    }

    public Named<Params, Result> named(Identifier $$0) {
        return new Named($$0, this);
    }

    public record Named<Params, Result>(Identifier name, MethodInfo<Params, Result> contents) {
        public static final Codec<Named<?, ?>> CODEC = Named.typedCodec();

        public static <Params, Result> Codec<Named<Params, Result>> typedCodec() {
            return RecordCodecBuilder.create($$0 -> $$0.group((App)Identifier.CODEC.fieldOf("name").forGetter(Named::name), (App)MethodInfo.typedCodec().forGetter(Named::contents)).apply((Applicative)$$0, Named::new));
        }
    }
}

