package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ClipBlockStateContext {
   private final Vec3 from;
   private final Vec3 to;
   private final Predicate<BlockState> block;

   public ClipBlockStateContext(Vec3 $$0, Vec3 $$1, Predicate<BlockState> $$2) {
      this.from = $$0;
      this.to = $$1;
      this.block = $$2;
   }

   public Vec3 getTo() {
      return this.to;
   }

   public Vec3 getFrom() {
      return this.from;
   }

   public Predicate<BlockState> isTargetBlock() {
      return this.block;
   }
}
