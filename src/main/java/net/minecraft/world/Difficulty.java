package net.minecraft.world;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable.EnumCodec;

public enum Difficulty implements StringRepresentable {
   PEACEFUL(0, "peaceful"),
   EASY(1, "easy"),
   NORMAL(2, "normal"),
   HARD(3, "hard");

   public static final EnumCodec<Difficulty> CODEC = StringRepresentable.fromEnum(Difficulty::values);
   private static final IntFunction<Difficulty> BY_ID = ByIdMap.continuous(Difficulty::getId, values(), OutOfBoundsStrategy.WRAP);
   public static final StreamCodec<ByteBuf, Difficulty> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Difficulty::getId);
   private final int id;
   private final String key;

   private Difficulty(final int $$0, final String $$1) {
      this.id = $$0;
      this.key = $$1;
   }

   public int getId() {
      return this.id;
   }

   public Component getDisplayName() {
      return Component.translatable("options.difficulty." + this.key);
   }

   public Component getInfo() {
      return Component.translatable("options.difficulty." + this.key + ".info");
   }

   @Deprecated
   public static Difficulty byId(int $$0) {
      return BY_ID.apply($$0);
   }

   
   public static Difficulty byName(String $$0) {
      return (Difficulty)CODEC.byName($$0);
   }

   public String getKey() {
      return this.key;
   }

   public String getSerializedName() {
      return this.key;
   }
}
