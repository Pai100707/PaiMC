package net.minecraft.world.inventory;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum ClickType {
   PICKUP(0),
   QUICK_MOVE(1),
   SWAP(2),
   CLONE(3),
   THROW(4),
   QUICK_CRAFT(5),
   PICKUP_ALL(6);

   private static final IntFunction<net.minecraft.world.inventory.ClickType> BY_ID = ByIdMap.continuous(
      net.minecraft.world.inventory.ClickType::id, values(), OutOfBoundsStrategy.ZERO
   );
   public static final StreamCodec<ByteBuf, net.minecraft.world.inventory.ClickType> STREAM_CODEC = ByteBufCodecs.idMapper(
      BY_ID, net.minecraft.world.inventory.ClickType::id
   );
   private final int id;

   private ClickType(final int $$0) {
      this.id = $$0;
   }

   public int id() {
      return this.id;
   }
}
