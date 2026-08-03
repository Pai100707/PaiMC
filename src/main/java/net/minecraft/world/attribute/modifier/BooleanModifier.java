package net.minecraft.world.attribute.modifier;

import com.mojang.serialization.Codec;

public enum BooleanModifier implements AttributeModifier<Boolean, Boolean> {
   AND,
   NAND,
   OR,
   NOR,
   XOR,
   XNOR;

   public Boolean apply(Boolean $$0, Boolean $$1) {
      return switch (this) {
         case AND -> $$1 && $$0;
         case NAND -> !$$1 || !$$0;
         case OR -> $$1 || $$0;
         case NOR -> !$$1 && !$$0;
         case XOR -> $$1 ^ $$0;
         case XNOR -> $$1 == $$0;
      };
   }

   @Override
   public Codec<Boolean> argumentCodec(net.minecraft.world.attribute.EnvironmentAttribute<Boolean> $$0) {
      return Codec.BOOL;
   }

   @Override
   public net.minecraft.world.attribute.LerpFunction<Boolean> argumentKeyframeLerp(net.minecraft.world.attribute.EnvironmentAttribute<Boolean> $$0) {
      return net.minecraft.world.attribute.LerpFunction.ofConstant();
   }
}
