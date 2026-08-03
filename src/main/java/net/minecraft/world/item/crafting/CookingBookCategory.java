package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum CookingBookCategory implements StringRepresentable {
   FOOD(0, "food"),
   BLOCKS(1, "blocks"),
   MISC(2, "misc");

   private static final IntFunction<CookingBookCategory> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
   public static final Codec<CookingBookCategory> CODEC = StringRepresentable.fromEnum(CookingBookCategory::values);
   public static final StreamCodec<ByteBuf, CookingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
   private final int id;
   private final String name;

   private CookingBookCategory(final int $$0, final String $$1) {
      this.id = $$0;
      this.name = $$1;
   }

   public String getSerializedName() {
      return this.name;
   }
}
