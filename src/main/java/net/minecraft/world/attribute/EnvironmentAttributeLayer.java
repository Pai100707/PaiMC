package net.minecraft.world.attribute;

import net.minecraft.world.phys.Vec3;

public sealed interface EnvironmentAttributeLayer<Value>
   permits net.minecraft.world.attribute.EnvironmentAttributeLayer.Constant,
   net.minecraft.world.attribute.EnvironmentAttributeLayer.TimeBased,
   net.minecraft.world.attribute.EnvironmentAttributeLayer.Positional {
   @FunctionalInterface
   public non-sealed interface Constant<Value> extends net.minecraft.world.attribute.EnvironmentAttributeLayer<Value> {
      Value applyConstant(Value var1);
   }

   @FunctionalInterface
   public non-sealed interface Positional<Value> extends net.minecraft.world.attribute.EnvironmentAttributeLayer<Value> {
      Value applyPositional(Value var1, Vec3 var2, net.minecraft.world.attribute.SpatialAttributeInterpolator var3);
   }

   @FunctionalInterface
   public non-sealed interface TimeBased<Value> extends net.minecraft.world.attribute.EnvironmentAttributeLayer<Value> {
      Value applyTimeBased(Value var1, int var2);
   }
}
