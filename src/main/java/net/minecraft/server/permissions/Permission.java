package net.minecraft.server.permissions;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public interface Permission {
   Codec<Permission> FULL_CODEC = BuiltInRegistries.PERMISSION_TYPE.byNameCodec().dispatch(Permission::codec, $$0 -> $$0);
   Codec<Permission> CODEC = Codec.either(FULL_CODEC, Identifier.CODEC)
      .xmap(
         $$0 -> (Permission)$$0.map($$0x -> $$0x, Permission.Atom::create),
         $$0 -> $$0 instanceof Permission.Atom $$1 ? Either.right($$1.id()) : Either.left($$0)
      );

   MapCodec<? extends Permission> codec();

   public record Atom(Identifier id) implements Permission {
      public static final MapCodec<Permission.Atom> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(Identifier.CODEC.fieldOf("id").forGetter(Permission.Atom::id)).apply($$0, Permission.Atom::new)
      );

      @Override
      public MapCodec<Permission.Atom> codec() {
         return MAP_CODEC;
      }

      public static Permission.Atom create(String $$0) {
         return create(Identifier.withDefaultNamespace($$0));
      }

      public static Permission.Atom create(Identifier $$0) {
         return new Permission.Atom($$0);
      }
   }

   public record HasCommandLevel(PermissionLevel level) implements Permission {
      public static final MapCodec<Permission.HasCommandLevel> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(PermissionLevel.CODEC.fieldOf("level").forGetter(Permission.HasCommandLevel::level)).apply($$0, Permission.HasCommandLevel::new)
      );

      @Override
      public MapCodec<Permission.HasCommandLevel> codec() {
         return MAP_CODEC;
      }
   }
}
