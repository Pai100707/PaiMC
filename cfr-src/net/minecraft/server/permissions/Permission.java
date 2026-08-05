/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.permissions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;

public interface Permission {
    public static final Codec<Permission> FULL_CODEC = BuiltInRegistries.PERMISSION_TYPE.byNameCodec().dispatch(Permission::codec, $$0 -> $$0);
    public static final Codec<Permission> CODEC = Codec.either(FULL_CODEC, Identifier.CODEC).xmap($$02 -> (Permission)$$02.map($$0 -> $$0, Atom::create), $$0 -> {
        Either either;
        if ($$0 instanceof Atom) {
            Atom $$1 = (Atom)$$0;
            either = Either.right((Object)$$1.id());
        } else {
            either = Either.left((Object)$$0);
        }
        return either;
    });

    public MapCodec<? extends Permission> codec();

    public record Atom(Identifier id) implements Permission
    {
        public static final MapCodec<Atom> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Identifier.CODEC.fieldOf("id").forGetter(Atom::id)).apply((Applicative)$$0, Atom::new));

        public MapCodec<Atom> codec() {
            return MAP_CODEC;
        }

        public static Atom create(String $$0) {
            return Atom.create(Identifier.withDefaultNamespace($$0));
        }

        public static Atom create(Identifier $$0) {
            return new Atom($$0);
        }
    }

    public record HasCommandLevel(PermissionLevel level) implements Permission
    {
        public static final MapCodec<HasCommandLevel> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)PermissionLevel.CODEC.fieldOf("level").forGetter(HasCommandLevel::level)).apply((Applicative)$$0, HasCommandLevel::new));

        public MapCodec<HasCommandLevel> codec() {
            return MAP_CODEC;
        }
    }
}

