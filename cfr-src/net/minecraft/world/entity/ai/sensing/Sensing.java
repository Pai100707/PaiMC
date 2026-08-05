/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 */
package net.minecraft.world.entity.ai.sensing;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class Sensing {
    private final Mob mob;
    private final IntSet seen = new IntOpenHashSet();
    private final IntSet unseen = new IntOpenHashSet();

    public Sensing(Mob $$0) {
        this.mob = $$0;
    }

    public void tick() {
        this.seen.clear();
        this.unseen.clear();
    }

    public boolean hasLineOfSight(Entity $$0) {
        int $$1 = $$0.getId();
        if (this.seen.contains($$1)) {
            return true;
        }
        if (this.unseen.contains($$1)) {
            return false;
        }
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

