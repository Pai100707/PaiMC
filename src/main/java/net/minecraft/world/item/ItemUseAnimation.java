package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum ItemUseAnimation implements StringRepresentable {
   NONE(0, "none"),
   EAT(1, "eat", true),
   DRINK(2, "drink", true),
   BLOCK(3, "block"),
   BOW(4, "bow"),
   TRIDENT(5, "trident"),
   CROSSBOW(6, "crossbow"),
   SPYGLASS(7, "spyglass"),
   TOOT_HORN(8, "toot_horn"),
   BRUSH(9, "brush"),
   BUNDLE(10, "bundle"),
   SPEAR(11, "spear", true);

   private static final IntFunction<net.minecraft.world.item.ItemUseAnimation> BY_ID = ByIdMap.continuous(
      net.minecraft.world.item.ItemUseAnimation::getId, values(), OutOfBoundsStrategy.ZERO
   );
   public static final Codec<net.minecraft.world.item.ItemUseAnimation> CODEC = StringRepresentable.fromEnum(net.minecraft.world.item.ItemUseAnimation::values);
   public static final StreamCodec<ByteBuf, net.minecraft.world.item.ItemUseAnimation> STREAM_CODEC = ByteBufCodecs.idMapper(
      BY_ID, net.minecraft.world.item.ItemUseAnimation::getId
   );
   private final int id;
   private final String name;
   private final boolean customArmTransform;

   private ItemUseAnimation(final int $$0, final String $$1) {
      this($$0, $$1, false);
   }

   private ItemUseAnimation(final int $$0, final String $$1, final boolean $$2) {
      this.id = $$0;
      this.name = $$1;
      this.customArmTransform = $$2;
   }

   public int getId() {
      return this.id;
   }

   public String getSerializedName() {
      return this.name;
   }

   public boolean hasCustomArmTransform() {
      return this.customArmTransform;
   }
}
