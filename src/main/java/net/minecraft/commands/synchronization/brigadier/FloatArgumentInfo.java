package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentInfo implements ArgumentTypeInfo<FloatArgumentType, FloatArgumentInfo.Template> {
   public void serializeToNetwork(FloatArgumentInfo.Template $$0, FriendlyByteBuf $$1) {
      boolean $$2 = $$0.min != -Float.MAX_VALUE;
      boolean $$3 = $$0.max != Float.MAX_VALUE;
      $$1.writeByte(ArgumentUtils.createNumberFlags($$2, $$3));
      if ($$2) {
         $$1.writeFloat($$0.min);
      }

      if ($$3) {
         $$1.writeFloat($$0.max);
      }
   }

   public FloatArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
      byte $$1 = $$0.readByte();
      float $$2 = ArgumentUtils.numberHasMin($$1) ? $$0.readFloat() : -Float.MAX_VALUE;
      float $$3 = ArgumentUtils.numberHasMax($$1) ? $$0.readFloat() : Float.MAX_VALUE;
      return new FloatArgumentInfo.Template($$2, $$3);
   }

   public void serializeToJson(FloatArgumentInfo.Template $$0, JsonObject $$1) {
      if ($$0.min != -Float.MAX_VALUE) {
         $$1.addProperty("min", $$0.min);
      }

      if ($$0.max != Float.MAX_VALUE) {
         $$1.addProperty("max", $$0.max);
      }
   }

   public FloatArgumentInfo.Template unpack(FloatArgumentType $$0) {
      return new FloatArgumentInfo.Template($$0.getMinimum(), $$0.getMaximum());
   }

   public final class Template implements ArgumentTypeInfo.Template<FloatArgumentType> {
      final float min;
      final float max;

      Template(final float $$1, final float $$2) {
         this.min = $$1;
         this.max = $$2;
      }

      public FloatArgumentType instantiate(net.minecraft.commands.CommandBuildContext $$0) {
         return FloatArgumentType.floatArg(this.min, this.max);
      }

      @Override
      public ArgumentTypeInfo<FloatArgumentType, ?> type() {
         return FloatArgumentInfo.this;
      }
   }
}
