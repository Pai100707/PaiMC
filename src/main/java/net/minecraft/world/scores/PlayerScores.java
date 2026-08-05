package net.minecraft.world.scores;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

class PlayerScores {
   private final Reference2ObjectOpenHashMap<net.minecraft.world.scores.Objective, net.minecraft.world.scores.Score> scores = new Reference2ObjectOpenHashMap(
      16, 0.5F
   );

   
   public net.minecraft.world.scores.Score get(net.minecraft.world.scores.Objective $$0) {
      return (net.minecraft.world.scores.Score)this.scores.get($$0);
   }

   public net.minecraft.world.scores.Score getOrCreate(net.minecraft.world.scores.Objective $$0, Consumer<net.minecraft.world.scores.Score> $$1) {
      return (net.minecraft.world.scores.Score)this.scores.computeIfAbsent($$0, $$1x -> {
         net.minecraft.world.scores.Score $$2 = new net.minecraft.world.scores.Score();
         $$1.accept($$2);
         return $$2;
      });
   }

   public boolean remove(net.minecraft.world.scores.Objective $$0) {
      return this.scores.remove($$0) != null;
   }

   public boolean hasScores() {
      return !this.scores.isEmpty();
   }

   public Object2IntMap<net.minecraft.world.scores.Objective> listScores() {
      Object2IntMap<net.minecraft.world.scores.Objective> $$0 = new Object2IntOpenHashMap();
      this.scores.forEach(($$1, $$2) -> $$0.put($$1, $$2.value()));
      return $$0;
   }

   void setScore(net.minecraft.world.scores.Objective $$0, net.minecraft.world.scores.Score $$1) {
      this.scores.put($$0, $$1);
   }

   Map<net.minecraft.world.scores.Objective, net.minecraft.world.scores.Score> listRawScores() {
      return Collections.unmodifiableMap(this.scores);
   }
}
