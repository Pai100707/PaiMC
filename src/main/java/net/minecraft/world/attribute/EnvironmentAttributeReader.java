package net.minecraft.world.attribute;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface EnvironmentAttributeReader {
   net.minecraft.world.attribute.EnvironmentAttributeReader EMPTY = new net.minecraft.world.attribute.EnvironmentAttributeReader() {
      @Override
      public <Value> Value getDimensionValue(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0) {
         return $$0.defaultValue();
      }

      @Override
      public <Value> Value getValue(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, Vec3 $$1, net.minecraft.world.attribute.SpatialAttributeInterpolator $$2
      ) {
         return $$0.defaultValue();
      }
   };

   <Value> Value getDimensionValue(net.minecraft.world.attribute.EnvironmentAttribute<Value> var1);

   default <Value> Value getValue(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, BlockPos $$1) {
      return this.getValue($$0, Vec3.atCenterOf($$1));
   }

   default <Value> Value getValue(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, Vec3 $$1) {
      return this.getValue($$0, $$1, null);
   }

   <Value> Value getValue(
      net.minecraft.world.attribute.EnvironmentAttribute<Value> var1, Vec3 var2, net.minecraft.world.attribute.SpatialAttributeInterpolator var3
   );
}
