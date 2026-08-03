package net.minecraft.world.entity.vehicle.boat;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class Raft extends AbstractBoat {
   public Raft(net.minecraft.world.entity.EntityType<? extends Raft> $$0, Level $$1, Supplier<Item> $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected double rideHeight(net.minecraft.world.entity.EntityDimensions $$0) {
      return $$0.height() * 0.8888889F;
   }
}
