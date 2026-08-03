package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;

public interface FontDescription {
   Codec<FontDescription> CODEC = Identifier.CODEC
      .flatComapMap(
         FontDescription.Resource::new,
         $$0 -> $$0 instanceof FontDescription.Resource $$1
            ? DataResult.success($$1.id())
            : DataResult.error(() -> "Unsupported font description type: " + $$0)
      );
   FontDescription.Resource DEFAULT = new FontDescription.Resource(Identifier.withDefaultNamespace("default"));

   public record AtlasSprite(Identifier atlasId, Identifier spriteId) implements FontDescription {
   }

   public record PlayerSprite(ResolvableProfile profile, boolean hat) implements FontDescription {
   }

   public record Resource(Identifier id) implements FontDescription {
   }
}
