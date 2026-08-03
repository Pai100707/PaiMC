package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.util.Util;

public interface InsideBlockEffectApplier {
   net.minecraft.world.entity.InsideBlockEffectApplier NOOP = new net.minecraft.world.entity.InsideBlockEffectApplier() {
      @Override
      public void apply(net.minecraft.world.entity.InsideBlockEffectType $$0) {
      }

      @Override
      public void runBefore(net.minecraft.world.entity.InsideBlockEffectType $$0, Consumer<net.minecraft.world.entity.Entity> $$1) {
      }

      @Override
      public void runAfter(net.minecraft.world.entity.InsideBlockEffectType $$0, Consumer<net.minecraft.world.entity.Entity> $$1) {
      }
   };

   void apply(net.minecraft.world.entity.InsideBlockEffectType var1);

   void runBefore(net.minecraft.world.entity.InsideBlockEffectType var1, Consumer<net.minecraft.world.entity.Entity> var2);

   void runAfter(net.minecraft.world.entity.InsideBlockEffectType var1, Consumer<net.minecraft.world.entity.Entity> var2);

   public static class StepBasedCollector implements net.minecraft.world.entity.InsideBlockEffectApplier {
      private static final net.minecraft.world.entity.InsideBlockEffectType[] APPLY_ORDER = net.minecraft.world.entity.InsideBlockEffectType.values();
      private static final int NO_STEP = -1;
      private final Set<net.minecraft.world.entity.InsideBlockEffectType> effectsInStep = EnumSet.noneOf(net.minecraft.world.entity.InsideBlockEffectType.class);
      private final Map<net.minecraft.world.entity.InsideBlockEffectType, List<Consumer<net.minecraft.world.entity.Entity>>> beforeEffectsInStep = Util.makeEnumMap(
         net.minecraft.world.entity.InsideBlockEffectType.class, $$0 -> new ArrayList()
      );
      private final Map<net.minecraft.world.entity.InsideBlockEffectType, List<Consumer<net.minecraft.world.entity.Entity>>> afterEffectsInStep = Util.makeEnumMap(
         net.minecraft.world.entity.InsideBlockEffectType.class, $$0 -> new ArrayList()
      );
      private final List<Consumer<net.minecraft.world.entity.Entity>> finalEffects = new ArrayList<>();
      private int lastStep = -1;

      public void advanceStep(int $$0) {
         if (this.lastStep != $$0) {
            this.lastStep = $$0;
            this.flushStep();
         }
      }

      public void applyAndClear(net.minecraft.world.entity.Entity $$0) {
         this.flushStep();

         for (Consumer<net.minecraft.world.entity.Entity> $$1 : this.finalEffects) {
            if (!$$0.isAlive()) {
               break;
            }

            $$1.accept($$0);
         }

         this.finalEffects.clear();
         this.lastStep = -1;
      }

      private void flushStep() {
         for (net.minecraft.world.entity.InsideBlockEffectType $$0 : APPLY_ORDER) {
            List<Consumer<net.minecraft.world.entity.Entity>> $$1 = this.beforeEffectsInStep.get($$0);
            this.finalEffects.addAll($$1);
            $$1.clear();
            if (this.effectsInStep.remove($$0)) {
               this.finalEffects.add($$0.effect());
            }

            List<Consumer<net.minecraft.world.entity.Entity>> $$2 = this.afterEffectsInStep.get($$0);
            this.finalEffects.addAll($$2);
            $$2.clear();
         }
      }

      @Override
      public void apply(net.minecraft.world.entity.InsideBlockEffectType $$0) {
         this.effectsInStep.add($$0);
      }

      @Override
      public void runBefore(net.minecraft.world.entity.InsideBlockEffectType $$0, Consumer<net.minecraft.world.entity.Entity> $$1) {
         this.beforeEffectsInStep.get($$0).add($$1);
      }

      @Override
      public void runAfter(net.minecraft.world.entity.InsideBlockEffectType $$0, Consumer<net.minecraft.world.entity.Entity> $$1) {
         this.afterEffectsInStep.get($$0).add($$1);
      }
   }
}
