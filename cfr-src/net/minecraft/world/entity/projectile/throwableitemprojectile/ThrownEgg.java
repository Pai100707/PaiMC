/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.throwableitemprojectile;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEgg
extends ThrowableItemProjectile {
    private static final EntityDimensions ZERO_SIZED_DIMENSIONS = EntityDimensions.fixed(0.0f, 0.0f);

    public ThrownEgg(EntityType<? extends ThrownEgg> $$0, Level $$1) {
        super((EntityType<? extends ThrowableItemProjectile>)$$0, $$1);
    }

    public ThrownEgg(Level $$0, LivingEntity $$1, ItemStack $$2) {
        super(EntityType.EGG, $$1, $$0, $$2);
    }

    public ThrownEgg(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
        super(EntityType.EGG, $$1, $$2, $$3, $$0, $$4);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 3) {
            double $$1 = 0.08;
            for (int $$2 = 0; $$2 < 8; ++$$2) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult $$0) {
        super.onHitEntity($$0);
        $$0.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onHit(HitResult $$02) {
        super.onHit($$02);
        if (!this.level().isClientSide()) {
            if (this.random.nextInt(8) == 0) {
                int $$1 = 1;
                if (this.random.nextInt(32) == 0) {
                    $$1 = 4;
                }
                for (int $$2 = 0; $$2 < $$1; ++$$2) {
                    Chicken $$3 = EntityType.CHICKEN.create(this.level(), EntitySpawnReason.TRIGGERED);
                    if ($$3 == null) continue;
                    $$3.setAge(-24000);
                    $$3.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
                    Optional.ofNullable(this.getItem().get(DataComponents.CHICKEN_VARIANT)).flatMap($$0 -> $$0.unwrap(this.registryAccess())).ifPresent($$3::setVariant);
                    if (!$$3.fudgePositionAfterSizeChange(ZERO_SIZED_DIMENSIONS)) break;
                    this.level().addFreshEntity($$3);
                }
            }
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EGG;
    }
}

