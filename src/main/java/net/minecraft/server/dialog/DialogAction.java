package net.minecraft.server.dialog;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable.EnumCodec;

public enum DialogAction implements StringRepresentable {
   CLOSE(0, "close"),
   NONE(1, "none"),
   WAIT_FOR_RESPONSE(2, "wait_for_response");

   public static final IntFunction<DialogAction> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
   public static final EnumCodec<DialogAction> CODEC = StringRepresentable.fromEnum(DialogAction::values);
   public static final StreamCodec<ByteBuf, DialogAction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
   private final int id;
   private final String name;

   private DialogAction(final int $$0, final String $$1) {
      this.id = $$0;
      this.name = $$1;
   }

   public String getSerializedName() {
      return this.name;
   }

   public boolean willUnpause() {
      return this == CLOSE || this == WAIT_FOR_RESPONSE;
   }
}
