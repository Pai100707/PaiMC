/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DamageSource {
    private final Holder<DamageType> type;
    private final @Nullable Entity causingEntity;
    private final @Nullable Entity directEntity;
    private final @Nullable Vec3 damageSourcePosition;

    public String toString() {
        return "DamageSource (" + this.type().msgId() + ")";
    }

    public float getFoodExhaustion() {
        return this.type().exhaustion();
    }

    public boolean isDirect() {
        return this.causingEntity == this.directEntity;
    }

    private DamageSource(Holder<DamageType> $$0, @Nullable Entity $$1, @Nullable Entity $$2, @Nullable Vec3 $$3) {
        this.type = $$0;
        this.causingEntity = $$2;
        this.directEntity = $$1;
        this.damageSourcePosition = $$3;
    }

    public DamageSource(Holder<DamageType> $$0, @Nullable Entity $$1, @Nullable Entity $$2) {
        this($$0, $$1, $$2, null);
    }

    public DamageSource(Holder<DamageType> $$0, Vec3 $$1) {
        this($$0, null, null, $$1);
    }

    public DamageSource(Holder<DamageType> $$0, @Nullable Entity $$1) {
        this($$0, $$1, $$1);
    }

    public DamageSource(Holder<DamageType> $$0) {
        this($$0, null, null, null);
    }

    public @Nullable Entity getDirectEntity() {
        return this.directEntity;
    }

    public @Nullable Entity getEntity() {
        return this.causingEntity;
    }

    public @Nullable ItemStack getWeaponItem() {
        return this.directEntity != null ? this.directEntity.getWeaponItem() : null;
    }

    public Component getLocalizedDeathMessage(LivingEntity $$0) {
        String $$1 = "death.attack." + this.type().msgId();
        if (this.causingEntity != null || this.directEntity != null) {
            ItemStack $$4;
            Component $$2 = this.causingEntity == null ? this.directEntity.getDisplayName() : this.causingEntity.getDisplayName();
            Entity entity = this.causingEntity;
            if (entity instanceof LivingEntity) {
                LivingEntity $$3 = (LivingEntity)entity;
                v0 = $$3.getMainHandItem();
            } else {
                v0 = $$4 = ItemStack.EMPTY;
            }
            if (!$$4.isEmpty() && $$4.has(DataComponents.CUSTOM_NAME)) {
                return Component.translatable($$1 + ".item", $$0.getDisplayName(), $$2, $$4.getDisplayName());
            }
            return Component.translatable($$1, $$0.getDisplayName(), $$2);
        }
        LivingEntity $$5 = $$0.getKillCredit();
        String $$6 = $$1 + ".player";
        if ($$5 != null) {
            return Component.translatable($$6, $$0.getDisplayName(), $$5.getDisplayName());
        }
        return Component.translatable($$1, $$0.getDisplayName());
    }

    public String getMsgId() {
        return this.type().msgId();
    }

    public boolean scalesWithDifficulty() {
        return switch (this.type().scaling()) {
            default -> throw new MatchException(null, null);
            case DamageScaling.NEVER -> false;
            case DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER -> {
                if (this.causingEntity instanceof LivingEntity && !(this.causingEntity instanceof Player)) {
                    yield true;
                }
                yield false;
            }
            case DamageScaling.ALWAYS -> true;
        };
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean isCreativePlayer() {
        Entity entity = this.getEntity();
        if (!(entity instanceof Player)) return false;
        Player $$0 = (Player)entity;
        if (!$$0.getAbilities().instabuild) return false;
        return true;
    }

    public @Nullable Vec3 getSourcePosition() {
        if (this.damageSourcePosition != null) {
            return this.damageSourcePosition;
        }
        if (this.directEntity != null) {
            return this.directEntity.position();
        }
        return null;
    }

    public @Nullable Vec3 sourcePositionRaw() {
        return this.damageSourcePosition;
    }

    public boolean is(TagKey<DamageType> $$0) {
        return this.type.is($$0);
    }

    public boolean is(ResourceKey<DamageType> $$0) {
        return this.type.is($$0);
    }

    public DamageType type() {
        return this.type.value();
    }

    public Holder<DamageType> typeHolder() {
        return this.type;
    }
}

