package net.minecraft.world.scores;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ScoreAccess {
   int get();

   void set(int var1);

   default int add(int $$0) {
      int $$1 = this.get() + $$0;
      this.set($$1);
      return $$1;
   }

   default int increment() {
      return this.add(1);
   }

   default void reset() {
      this.set(0);
   }

   boolean locked();

   void unlock();

   void lock();

   
   Component display();

   void display(Component var1);

   void numberFormatOverride(NumberFormat var1);
}
