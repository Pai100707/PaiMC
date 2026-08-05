/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;

public class ItemCooldowns {
    private final Map<Identifier, CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public boolean isOnCooldown(ItemStack $$0) {
        return this.getCooldownPercent($$0, 0.0f) > 0.0f;
    }

    public float getCooldownPercent(ItemStack $$0, float $$1) {
        Identifier $$2 = this.getCooldownGroup($$0);
        CooldownInstance $$3 = this.cooldowns.get($$2);
        if ($$3 != null) {
            float $$4 = $$3.endTime - $$3.startTime;
            float $$5 = (float)$$3.endTime - ((float)this.tickCount + $$1);
            return Mth.clamp($$5 / $$4, 0.0f, 1.0f);
        }
        return 0.0f;
    }

    public void tick() {
        ++this.tickCount;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Map.Entry<Identifier, CooldownInstance>> $$0 = this.cooldowns.entrySet().iterator();
            while ($$0.hasNext()) {
                Map.Entry<Identifier, CooldownInstance> $$1 = $$0.next();
                if ($$1.getValue().endTime > this.tickCount) continue;
                $$0.remove();
                this.onCooldownEnded($$1.getKey());
            }
        }
    }

    public Identifier getCooldownGroup(ItemStack $$0) {
        UseCooldown $$1 = $$0.get(DataComponents.USE_COOLDOWN);
        Identifier $$2 = BuiltInRegistries.ITEM.getKey($$0.getItem());
        if ($$1 == null) {
            return $$2;
        }
        return $$1.cooldownGroup().orElse($$2);
    }

    public void addCooldown(ItemStack $$0, int $$1) {
        this.addCooldown(this.getCooldownGroup($$0), $$1);
    }

    public void addCooldown(Identifier $$0, int $$1) {
        this.cooldowns.put($$0, new CooldownInstance(this.tickCount, this.tickCount + $$1));
        this.onCooldownStarted($$0, $$1);
    }

    public void removeCooldown(Identifier $$0) {
        this.cooldowns.remove($$0);
        this.onCooldownEnded($$0);
    }

    protected void onCooldownStarted(Identifier $$0, int $$1) {
    }

    protected void onCooldownEnded(Identifier $$0) {
    }

    static final class CooldownInstance
    extends Record {
        final int startTime;
        final int endTime;

        CooldownInstance(int $$0, int $$1) {
            this.startTime = $$0;
            this.endTime = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this, $$0);
        }

        public int startTime() {
            return this.startTime;
        }

        public int endTime() {
            return this.endTime;
        }
    }
}

