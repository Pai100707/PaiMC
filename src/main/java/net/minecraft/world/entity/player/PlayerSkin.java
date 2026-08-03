package net.minecraft.world.entity.player;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.ClientAsset.Texture;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

public record PlayerSkin(Texture body, @Nullable Texture cape, @Nullable Texture elytra, PlayerModelType model, boolean secure) {
   public static PlayerSkin insecure(Texture $$0, @Nullable Texture $$1, @Nullable Texture $$2, PlayerModelType $$3) {
      return new PlayerSkin($$0, $$1, $$2, $$3, false);
   }

   public PlayerSkin with(PlayerSkin.Patch $$0) {
      return $$0.equals(PlayerSkin.Patch.EMPTY)
         ? this
         : insecure(
            (Texture)DataFixUtils.orElse($$0.body, this.body),
            (Texture)DataFixUtils.orElse($$0.cape, this.cape),
            (Texture)DataFixUtils.orElse($$0.elytra, this.elytra),
            $$0.model.orElse(this.model)
         );
   }

   public record Patch(Optional<ResourceTexture> body, Optional<ResourceTexture> cape, Optional<ResourceTexture> elytra, Optional<PlayerModelType> model) {
      public static final PlayerSkin.Patch EMPTY = new PlayerSkin.Patch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
      public static final MapCodec<PlayerSkin.Patch> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               ResourceTexture.CODEC.optionalFieldOf("texture").forGetter(PlayerSkin.Patch::body),
               ResourceTexture.CODEC.optionalFieldOf("cape").forGetter(PlayerSkin.Patch::cape),
               ResourceTexture.CODEC.optionalFieldOf("elytra").forGetter(PlayerSkin.Patch::elytra),
               PlayerModelType.CODEC.optionalFieldOf("model").forGetter(PlayerSkin.Patch::model)
            )
            .apply($$0, PlayerSkin.Patch::create)
      );
      public static final StreamCodec<ByteBuf, PlayerSkin.Patch> STREAM_CODEC = StreamCodec.composite(
         ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional),
         PlayerSkin.Patch::body,
         ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional),
         PlayerSkin.Patch::cape,
         ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional),
         PlayerSkin.Patch::elytra,
         PlayerModelType.STREAM_CODEC.apply(ByteBufCodecs::optional),
         PlayerSkin.Patch::model,
         PlayerSkin.Patch::create
      );

      public static PlayerSkin.Patch create(
         Optional<ResourceTexture> $$0, Optional<ResourceTexture> $$1, Optional<ResourceTexture> $$2, Optional<PlayerModelType> $$3
      ) {
         return $$0.isEmpty() && $$1.isEmpty() && $$2.isEmpty() && $$3.isEmpty() ? EMPTY : new PlayerSkin.Patch($$0, $$1, $$2, $$3);
      }
   }
}
