package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public interface ClientAsset {
   Identifier id();

   public record DownloadedTexture(Identifier texturePath, String url) implements net.minecraft.core.ClientAsset.Texture {
      @Override
      public Identifier id() {
         return this.texturePath;
      }
   }

   public record ResourceTexture(Identifier id, Identifier texturePath) implements net.minecraft.core.ClientAsset.Texture {
      public static final Codec<net.minecraft.core.ClientAsset.ResourceTexture> CODEC = Identifier.CODEC
         .xmap(net.minecraft.core.ClientAsset.ResourceTexture::new, net.minecraft.core.ClientAsset.ResourceTexture::id);
      public static final MapCodec<net.minecraft.core.ClientAsset.ResourceTexture> DEFAULT_FIELD_CODEC = CODEC.fieldOf("asset_id");
      public static final StreamCodec<ByteBuf, net.minecraft.core.ClientAsset.ResourceTexture> STREAM_CODEC = Identifier.STREAM_CODEC
         .map(net.minecraft.core.ClientAsset.ResourceTexture::new, net.minecraft.core.ClientAsset.ResourceTexture::id);

      public ResourceTexture(Identifier $$0) {
         this($$0, $$0.withPath($$0x -> "textures/" + $$0x + ".png"));
      }
   }

   public interface Texture extends net.minecraft.core.ClientAsset {
      Identifier texturePath();
   }
}
