package net.minecraft.world.level.block.state.properties;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum TestBlockMode implements StringRepresentable {
   START(0, "start"),
   LOG(1, "log"),
   FAIL(2, "fail"),
   ACCEPT(3, "accept");

   private static final IntFunction<TestBlockMode> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
   public static final Codec<TestBlockMode> CODEC = StringRepresentable.fromEnum(TestBlockMode::values);
   public static final StreamCodec<ByteBuf, TestBlockMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
   private final int id;
   private final String name;
   private final Component displayName;
   private final Component detailedMessage;

   private TestBlockMode(final int $$0, final String $$1) {
      this.id = $$0;
      this.name = $$1;
      this.displayName = Component.translatable("test_block.mode." + $$1);
      this.detailedMessage = Component.translatable("test_block.mode_info." + $$1);
   }

   public String getSerializedName() {
      return this.name;
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public Component getDetailedMessage() {
      return this.detailedMessage;
   }
}
