package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentInfo implements ArgumentTypeInfo<DoubleArgumentType, DoubleArgumentInfo.Template> {
   public void serializeToNetwork(DoubleArgumentInfo.Template $$0, FriendlyByteBuf $$1) {
      boolean $$2 = $$0.min != -Double.MAX_VALUE;
      boolean $$3 = $$0.max != Double.MAX_VALUE;
      $$1.writeByte(ArgumentUtils.createNumberFlags($$2, $$3));
      if ($$2) {
         $$1.writeDouble($$0.min);
      }

      if ($$3) {
         $$1.writeDouble($$0.max);
      }
   }

   public DoubleArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
      byte $$1 = $$0.readByte();
      double $$2 = ArgumentUtils.numberHasMin($$1) ? $$0.readDouble() : -Double.MAX_VALUE;
      double $$3 = ArgumentUtils.numberHasMax($$1) ? $$0.readDouble() : Double.MAX_VALUE;
      return new DoubleArgumentInfo.Template($$2, $$3);
   }

   public void serializeToJson(DoubleArgumentInfo.Template $$0, JsonObject $$1) {
      if ($$0.min != -Double.MAX_VALUE) {
         $$1.addProperty("min", $$0.min);
      }

      if ($$0.max != Double.MAX_VALUE) {
         $$1.addProperty("max", $$0.max);
      }
   }

   public DoubleArgumentInfo.Template unpack(DoubleArgumentType $$0) {
      return new DoubleArgumentInfo.Template($$0.getMinimum(), $$0.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<DoubleArgumentType> {
      final double min;
      final double max;

      Template(final double $$1, final double $$2) {
         this.min = $$1;
         this.max = $$2;
      }

      public DoubleArgumentType instantiate(net.minecraft.commands.CommandBuildContext $$0) {
         return DoubleArgumentType.doubleArg(this.min, this.max);
      }

      @Override
      public ArgumentTypeInfo<DoubleArgumentType, ?> type() {
         return DoubleArgumentInfo.this;
      }
   }
}
