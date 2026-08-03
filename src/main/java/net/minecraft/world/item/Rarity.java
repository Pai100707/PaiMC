package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum Rarity implements StringRepresentable {
   COMMON(0, "common", ChatFormatting.WHITE),
   UNCOMMON(1, "uncommon", ChatFormatting.YELLOW),
   RARE(2, "rare", ChatFormatting.AQUA),
   EPIC(3, "epic", ChatFormatting.LIGHT_PURPLE);

   public static final Codec<net.minecraft.world.item.Rarity> CODEC = StringRepresentable.fromValues(net.minecraft.world.item.Rarity::values);
   public static final IntFunction<net.minecraft.world.item.Rarity> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
   public static final StreamCodec<ByteBuf, net.minecraft.world.item.Rarity> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
   private final int id;
   private final String name;
   private final ChatFormatting color;

   private Rarity(final int $$0, final String $$1, final ChatFormatting $$2) {
      this.id = $$0;
      this.name = $$1;
      this.color = $$2;
   }

   public ChatFormatting color() {
      return this.color;
   }

   public String getSerializedName() {
      return this.name;
   }
}
