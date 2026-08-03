package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentInfo implements ArgumentTypeInfo<IntegerArgumentType, IntegerArgumentInfo.Template> {
   public void serializeToNetwork(IntegerArgumentInfo.Template $$0, FriendlyByteBuf $$1) {
      boolean $$2 = $$0.min != Integer.MIN_VALUE;
      boolean $$3 = $$0.max != Integer.MAX_VALUE;
      $$1.writeByte(ArgumentUtils.createNumberFlags($$2, $$3));
      if ($$2) {
         $$1.writeInt($$0.min);
      }

      if ($$3) {
         $$1.writeInt($$0.max);
      }
   }

   public IntegerArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
      byte $$1 = $$0.readByte();
      int $$2 = ArgumentUtils.numberHasMin($$1) ? $$0.readInt() : Integer.MIN_VALUE;
      int $$3 = ArgumentUtils.numberHasMax($$1) ? $$0.readInt() : Integer.MAX_VALUE;
      return new IntegerArgumentInfo.Template($$2, $$3);
   }

   public void serializeToJson(IntegerArgumentInfo.Template $$0, JsonObject $$1) {
      if ($$0.min != Integer.MIN_VALUE) {
         $$1.addProperty("min", $$0.min);
      }

      if ($$0.max != Integer.MAX_VALUE) {
         $$1.addProperty("max", $$0.max);
      }
   }

   public IntegerArgumentInfo.Template unpack(IntegerArgumentType $$0) {
      return new IntegerArgumentInfo.Template($$0.getMinimum(), $$0.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<IntegerArgumentType> {
      final int min;
      final int max;

      Template(final int $$1, final int $$2) {
         this.min = $$1;
         this.max = $$2;
      }

      public IntegerArgumentType instantiate(net.minecraft.commands.CommandBuildContext $$0) {
         return IntegerArgumentType.integer(this.min, this.max);
      }

      @Override
      public ArgumentTypeInfo<IntegerArgumentType, ?> type() {
         return IntegerArgumentInfo.this;
      }
   }
}
