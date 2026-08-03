package net.minecraft.world.entity.ai.sensing;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public class Sensing {
   private final net.minecraft.world.entity.Mob mob;
   private final IntSet seen = new IntOpenHashSet();
   private final IntSet unseen = new IntOpenHashSet();

   public Sensing(net.minecraft.world.entity.Mob $$0) {
      this.mob = $$0;
   }

   public void tick() {
      this.seen.clear();
      this.unseen.clear();
   }

   public boolean hasLineOfSight(net.minecraft.world.entity.Entity $$0) {
      int $$1 = $$0.getId();
      if (this.seen.contains($$1)) {
         return true;
      } else if (this.unseen.contains($$1)) {
         return false;
      } else {
         ProfilerFiller $$2 = Profiler.get();
         $$2.push("hasLineOfSight");
         boolean $$3 = this.mob.hasLineOfSight($$0);
         $$2.pop();
         if ($$3) {
            this.seen.add($$1);
         } else {
            this.unseen.add($$1);
         }

         return $$3;
      }
   }
}
