package net.minecraft.stats;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.player.Player;

public class StatsCounter {
   protected final Object2IntMap<net.minecraft.stats.Stat<?>> stats = Object2IntMaps.synchronize(new Object2IntOpenHashMap());

   public StatsCounter() {
      this.stats.defaultReturnValue(0);
   }

   public void increment(Player $$0, net.minecraft.stats.Stat<?> $$1, int $$2) {
      int $$3 = (int)Math.min((long)this.getValue($$1) + $$2, 2147483647L);
      this.setValue($$0, $$1, $$3);
   }

   public void setValue(Player $$0, net.minecraft.stats.Stat<?> $$1, int $$2) {
      this.stats.put($$1, $$2);
   }

   public <T> int getValue(net.minecraft.stats.StatType<T> $$0, T $$1) {
      return $$0.contains($$1) ? this.getValue($$0.get($$1)) : 0;
   }

   public int getValue(net.minecraft.stats.Stat<?> $$0) {
      return this.stats.getInt($$0);
   }
}
