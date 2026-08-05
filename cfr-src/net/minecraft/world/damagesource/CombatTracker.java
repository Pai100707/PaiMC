/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.CommonLinks;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class CombatTracker {
    public static final int RESET_DAMAGE_STATUS_TIME = 100;
    public static final int RESET_COMBAT_STATUS_TIME = 300;
    private static final Style INTENTIONAL_GAME_DESIGN_STYLE = Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(CommonLinks.INTENTIONAL_GAME_DESIGN_BUG)).withHoverEvent(new HoverEvent.ShowText(Component.literal("MCPE-28723")));
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final LivingEntity mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;

    public CombatTracker(LivingEntity $$0) {
        this.mob = $$0;
    }

    public void recordDamage(DamageSource $$0, float $$1) {
        this.recheckStatus();
        FallLocation $$2 = FallLocation.getCurrentFallLocation(this.mob);
        CombatEntry $$3 = new CombatEntry($$0, $$1, $$2, (float)this.mob.fallDistance);
        this.entries.add($$3);
        this.lastDamageTime = this.mob.tickCount;
        this.takingDamage = true;
        if (!this.inCombat && this.mob.isAlive() && CombatTracker.shouldEnterCombat($$0)) {
            this.inCombat = true;
            this.combatEndTime = this.combatStartTime = this.mob.tickCount;
            this.mob.onEnterCombat();
        }
    }

    private static boolean shouldEnterCombat(DamageSource $$0) {
        return $$0.getEntity() instanceof LivingEntity;
    }

    private Component getMessageForAssistedFall(Entity $$0, Component $$1, String $$2, String $$3) {
        ItemStack $$5;
        if ($$0 instanceof LivingEntity) {
            LivingEntity $$4 = (LivingEntity)$$0;
            v0 = $$4.getMainHandItem();
        } else {
            v0 = $$5 = ItemStack.EMPTY;
        }
        if (!$$5.isEmpty() && $$5.has(DataComponents.CUSTOM_NAME)) {
            return Component.translatable($$2, this.mob.getDisplayName(), $$1, $$5.getDisplayName());
        }
        return Component.translatable($$3, this.mob.getDisplayName(), $$1);
    }

    private Component getFallMessage(CombatEntry $$0, @Nullable Entity $$1) {
        DamageSource $$2 = $$0.source();
        if ($$2.is(DamageTypeTags.IS_FALL) || $$2.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
            FallLocation $$3 = Objects.requireNonNullElse($$0.fallLocation(), FallLocation.GENERIC);
            return Component.translatable($$3.languageKey(), this.mob.getDisplayName());
        }
        Component $$4 = CombatTracker.getDisplayName($$1);
        Entity $$5 = $$2.getEntity();
        Component $$6 = CombatTracker.getDisplayName($$5);
        if ($$6 != null && !$$6.equals($$4)) {
            return this.getMessageForAssistedFall($$5, $$6, "death.fell.assist.item", "death.fell.assist");
        }
        if ($$4 != null) {
            return this.getMessageForAssistedFall($$1, $$4, "death.fell.finish.item", "death.fell.finish");
        }
        return Component.translatable("death.fell.killer", this.mob.getDisplayName());
    }

    private static @Nullable Component getDisplayName(@Nullable Entity $$0) {
        return $$0 == null ? null : $$0.getDisplayName();
    }

    public Component getDeathMessage() {
        if (this.entries.isEmpty()) {
            return Component.translatable("death.attack.generic", this.mob.getDisplayName());
        }
        CombatEntry $$0 = this.entries.get(this.entries.size() - 1);
        DamageSource $$1 = $$0.source();
        CombatEntry $$2 = this.getMostSignificantFall();
        DeathMessageType $$3 = $$1.type().deathMessageType();
        if ($$3 == DeathMessageType.FALL_VARIANTS && $$2 != null) {
            return this.getFallMessage($$2, $$1.getEntity());
        }
        if ($$3 == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
            String $$4 = "death.attack." + $$1.getMsgId();
            MutableComponent $$5 = ComponentUtils.wrapInSquareBrackets(Component.translatable($$4 + ".link")).withStyle(INTENTIONAL_GAME_DESIGN_STYLE);
            return Component.translatable($$4 + ".message", this.mob.getDisplayName(), $$5);
        }
        return $$1.getLocalizedDeathMessage(this.mob);
    }

    private @Nullable CombatEntry getMostSignificantFall() {
        CombatEntry $$0 = null;
        CombatEntry $$1 = null;
        float $$2 = 0.0f;
        float $$3 = 0.0f;
        for (int $$4 = 0; $$4 < this.entries.size(); ++$$4) {
            float $$9;
            CombatEntry $$5 = this.entries.get($$4);
            CombatEntry $$6 = $$4 > 0 ? this.entries.get($$4 - 1) : null;
            DamageSource $$7 = $$5.source();
            boolean $$8 = $$7.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
            float f = $$9 = $$8 ? Float.MAX_VALUE : $$5.fallDistance();
            if (($$7.is(DamageTypeTags.IS_FALL) || $$8) && $$9 > 0.0f && ($$0 == null || $$9 > $$3)) {
                $$0 = $$4 > 0 ? $$6 : $$5;
                $$3 = $$9;
            }
            if ($$5.fallLocation() == null || $$1 != null && !($$5.damage() > $$2)) continue;
            $$1 = $$5;
            $$2 = $$5.damage();
        }
        if ($$3 > 5.0f && $$0 != null) {
            return $$0;
        }
        if ($$2 > 5.0f && $$1 != null) {
            return $$1;
        }
        return null;
    }

    public int getCombatDuration() {
        if (this.inCombat) {
            return this.mob.tickCount - this.combatStartTime;
        }
        return this.combatEndTime - this.combatStartTime;
    }

    public void recheckStatus() {
        int $$0;
        int n = $$0 = this.inCombat ? 300 : 100;
        if (this.takingDamage && (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > $$0)) {
            boolean $$1 = this.inCombat;
            this.takingDamage = false;
            this.inCombat = false;
            this.combatEndTime = this.mob.tickCount;
            if ($$1) {
                this.mob.onLeaveCombat();
            }
            this.entries.clear();
        }
    }
}

