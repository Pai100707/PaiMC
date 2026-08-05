package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PlayerModelType implements StringRepresentable {
   SLIM("slim", "slim"),
   WIDE("wide", "default");

   public static final Codec<PlayerModelType> CODEC = StringRepresentable.fromEnum(PlayerModelType::values);
   private static final Function<String, PlayerModelType> NAME_LOOKUP = StringRepresentable.createNameLookup(values(), $$0 -> $$0.legacyServicesId);
   public static final StreamCodec<ByteBuf, PlayerModelType> STREAM_CODEC = ByteBufCodecs.BOOL.map($$0 -> $$0 ? SLIM : WIDE, $$0 -> $$0 == SLIM);
   private final String id;
   private final String legacyServicesId;

   private PlayerModelType(final String $$0, final String $$1) {
      this.id = $$0;
      this.legacyServicesId = $$1;
   }

   public static PlayerModelType byLegacyServicesName(String $$0) {
      return Objects.requireNonNullElse(NAME_LOOKUP.apply($$0), WIDE);
   }

   public String getSerializedName() {
      return this.id;
   }
}
