/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectType;

public interface InsideBlockEffectApplier {
    public static final InsideBlockEffectApplier NOOP = new InsideBlockEffectApplier(){

        @Override
        public void apply(InsideBlockEffectType $$0) {
        }

        @Override
        public void runBefore(InsideBlockEffectType $$0, Consumer<Entity> $$1) {
        }

        @Override
        public void runAfter(InsideBlockEffectType $$0, Consumer<Entity> $$1) {
        }
    };

    public void apply(InsideBlockEffectType var1);

    public void runBefore(InsideBlockEffectType var1, Consumer<Entity> var2);

    public void runAfter(InsideBlockEffectType var1, Consumer<Entity> var2);

    public static class StepBasedCollector
    implements InsideBlockEffectApplier {
        private static final InsideBlockEffectType[] APPLY_ORDER = InsideBlockEffectType.values();
        private static final int NO_STEP = -1;
        private final Set<InsideBlockEffectType> effectsInStep = EnumSet.noneOf(InsideBlockEffectType.class);
        private final Map<InsideBlockEffectType, List<Consumer<Entity>>> beforeEffectsInStep = Util.makeEnumMap(InsideBlockEffectType.class, $$0 -> new ArrayList());
        private final Map<InsideBlockEffectType, List<Consumer<Entity>>> afterEffectsInStep = Util.makeEnumMap(InsideBlockEffectType.class, $$0 -> new ArrayList());
        private final List<Consumer<Entity>> finalEffects = new ArrayList<Consumer<Entity>>();
        private int lastStep = -1;

        public void advanceStep(int $$0) {
            if (this.lastStep != $$0) {
                this.lastStep = $$0;
                this.flushStep();
            }
        }

        public void applyAndClear(Entity $$0) {
            this.flushStep();
            for (Consumer<Entity> $$1 : this.finalEffects) {
                if (!$$0.isAlive()) break;
                $$1.accept($$0);
            }
            this.finalEffects.clear();
            this.lastStep = -1;
        }

        private void flushStep() {
            for (InsideBlockEffectType $$0 : APPLY_ORDER) {
                List<Consumer<Entity>> $$1 = this.beforeEffectsInStep.get((Object)$$0);
                this.finalEffects.addAll($$1);
                $$1.clear();
                if (this.effectsInStep.remove((Object)$$0)) {
                    this.finalEffects.add($$0.effect());
                }
                List<Consumer<Entity>> $$2 = this.afterEffectsInStep.get((Object)$$0);
                this.finalEffects.addAll($$2);
                $$2.clear();
            }
        }

        @Override
        public void apply(InsideBlockEffectType $$0) {
            this.effectsInStep.add($$0);
        }

        @Override
        public void runBefore(InsideBlockEffectType $$0, Consumer<Entity> $$1) {
            this.beforeEffectsInStep.get((Object)$$0).add($$1);
        }

        @Override
        public void runAfter(InsideBlockEffectType $$0, Consumer<Entity> $$1) {
            this.afterEffectsInStep.get((Object)$$0).add($$1);
        }
    }
}

