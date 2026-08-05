/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.spider;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CaveSpider
extends Spider {
    public CaveSpider(EntityType<? extends CaveSpider> $$0, Level $$1) {
        super((EntityType<? extends Spider>)$$0, $$1);
    }

    public static AttributeSupplier.Builder createCaveSpider() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 12.0);
    }

    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        if (super.doHurtTarget($$0, $$1)) {
            if ($$1 instanceof LivingEntity) {
                int $$2 = 0;
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    $$2 = 7;
                } else if (this.level().getDifficulty() == Difficulty.HARD) {
                    $$2 = 15;
                }
                if ($$2 > 0) {
                    ((LivingEntity)$$1).addEffect(new MobEffectInstance(MobEffects.POISON, $$2 * 20, 0), this);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        return $$3;
    }

    @Override
    public Vec3 getVehicleAttachmentPoint(Entity $$0) {
        if ($$0.getBbWidth() <= this.getBbWidth()) {
            return new Vec3(0.0, 0.21875 * (double)this.getScale(), 0.0);
        }
        return super.getVehicleAttachmentPoint($$0);
    }
}

