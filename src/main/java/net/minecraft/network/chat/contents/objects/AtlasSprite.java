package net.minecraft.network.chat.contents.objects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public record AtlasSprite(Identifier atlas, Identifier sprite) implements ObjectInfo {
   public static final Identifier DEFAULT_ATLAS = AtlasIds.BLOCKS;
   public static final MapCodec<AtlasSprite> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Identifier.CODEC.optionalFieldOf("atlas", DEFAULT_ATLAS).forGetter(AtlasSprite::atlas),
            Identifier.CODEC.fieldOf("sprite").forGetter(AtlasSprite::sprite)
         )
         .apply($$0, AtlasSprite::new)
   );

   @Override
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
      String $$0 = toShortName(this.sprite);
      return this.atlas.equals(DEFAULT_ATLAS) ? "[" + $$0 + "]" : "[" + $$0 + "@" + toShortName(this.atlas) + "]";
   }
}
