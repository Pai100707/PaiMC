package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentInfo implements ArgumentTypeInfo<LongArgumentType, LongArgumentInfo.Template> {
   public void serializeToNetwork(LongArgumentInfo.Template $$0, FriendlyByteBuf $$1) {
      boolean $$2 = $$0.min != Long.MIN_VALUE;
      boolean $$3 = $$0.max != Long.MAX_VALUE;
      $$1.writeByte(ArgumentUtils.createNumberFlags($$2, $$3));
      if ($$2) {
         $$1.writeLong($$0.min);
      }

      if ($$3) {
         $$1.writeLong($$0.max);
      }
   }

   public LongArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
      byte $$1 = $$0.readByte();
      long $$2 = ArgumentUtils.numberHasMin($$1) ? $$0.readLong() : Long.MIN_VALUE;
      long $$3 = ArgumentUtils.numberHasMax($$1) ? $$0.readLong() : Long.MAX_VALUE;
      return new LongArgumentInfo.Template($$2, $$3);
   }

   public void serializeToJson(LongArgumentInfo.Template $$0, JsonObject $$1) {
      if ($$0.min != Long.MIN_VALUE) {
         $$1.addProperty("min", $$0.min);
      }

      if ($$0.max != Long.MAX_VALUE) {
         $$1.addProperty("max", $$0.max);
      }
   }

   public LongArgumentInfo.Template unpack(LongArgumentType $$0) {
      return new LongArgumentInfo.Template($$0.getMinimum(), $$0.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<LongArgumentType> {
      final long min;
      final long max;

      Template(final long $$1, final long $$2) {
         this.min = $$1;
         this.max = $$2;
      }

      public LongArgumentType instantiate(net.minecraft.commands.CommandBuildContext $$0) {
         return LongArgumentType.longArg(this.min, this.max);
      }

      @Override
      public ArgumentTypeInfo<LongArgumentType, ?> type() {
         return LongArgumentInfo.this;
      }
   }
}
