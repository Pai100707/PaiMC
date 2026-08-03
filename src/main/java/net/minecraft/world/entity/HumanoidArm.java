package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum HumanoidArm implements StringRepresentable {
   LEFT(0, "left", "options.mainHand.left"),
   RIGHT(1, "right", "options.mainHand.right");

   public static final Codec<net.minecraft.world.entity.HumanoidArm> CODEC = StringRepresentable.fromEnum(net.minecraft.world.entity.HumanoidArm::values);
   private static final IntFunction<net.minecraft.world.entity.HumanoidArm> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
   public static final StreamCodec<ByteBuf, net.minecraft.world.entity.HumanoidArm> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
   private final int id;
   private final String name;
   private final Component caption;

   private HumanoidArm(final int $$0, final String $$1, final String $$2) {
      this.id = $$0;
      this.name = $$1;
      this.caption = Component.translatable($$2);
   }

   public net.minecraft.world.entity.HumanoidArm getOpposite() {
      return switch (this) {
         case LEFT -> RIGHT;
         case RIGHT -> LEFT;
      };
   }

   public Component caption() {
      return this.caption;
   }

   public String getSerializedName() {
      return this.name;
   }
}
