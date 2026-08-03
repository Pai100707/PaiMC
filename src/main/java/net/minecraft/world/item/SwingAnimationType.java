package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum SwingAnimationType implements StringRepresentable {
   NONE(0, "none"),
   WHACK(1, "whack"),
   STAB(2, "stab");

   private static final IntFunction<net.minecraft.world.item.SwingAnimationType> BY_ID = ByIdMap.continuous(
      net.minecraft.world.item.SwingAnimationType::getId, values(), OutOfBoundsStrategy.ZERO
   );
   public static final Codec<net.minecraft.world.item.SwingAnimationType> CODEC = StringRepresentable.fromEnum(
      net.minecraft.world.item.SwingAnimationType::values
   );
   public static final StreamCodec<ByteBuf, net.minecraft.world.item.SwingAnimationType> STREAM_CODEC = ByteBufCodecs.idMapper(
      BY_ID, net.minecraft.world.item.SwingAnimationType::getId
   );
   private final int id;
   private final String name;

   private SwingAnimationType(final int $$0, final String $$1) {
      this.id = $$0;
      this.name = $$1;
   }

   public int getId() {
      return this.id;
   }

   public String getSerializedName() {
      return this.name;
   }
}
