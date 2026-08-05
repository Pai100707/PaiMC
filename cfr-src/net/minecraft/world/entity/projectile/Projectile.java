/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class Projectile
extends Entity
implements TraceableEntity {
    private static final boolean DEFAULT_LEFT_OWNER = false;
    private static final boolean DEFAULT_HAS_BEEN_SHOT = false;
    protected @Nullable EntityReference<Entity> owner;
    private boolean leftOwner = false;
    private boolean leftOwnerChecked;
    private boolean hasBeenShot = false;
    private @Nullable Entity lastDeflectedBy;

    protected Projectile(EntityType<? extends Projectile> $$0, Level $$1) {
        super($$0, $$1);
    }

    protected void setOwner(@Nullable EntityReference<Entity> $$0) {
        this.owner = $$0;
    }

    public void setOwner(@Nullable Entity $$0) {
        this.setOwner(EntityReference.of($$0));
    }

    @Override
    public @Nullable Entity getOwner() {
        return EntityReference.getEntity(this.owner, this.level());
    }

    public Entity getEffectSource() {
        return (Entity)MoreObjects.firstNonNull((Object)this.getOwner(), (Object)this);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        EntityReference.store(this.owner, $$0, "Owner");
        if (this.leftOwner) {
            $$0.putBoolean("LeftOwner", true);
        }
        $$0.putBoolean("HasBeenShot", this.hasBeenShot);
    }

    protected boolean ownedBy(Entity $$0) {
        return this.owner != null && this.owner.matches($$0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.setOwner(EntityReference.read($$0, "Owner"));
        this.leftOwner = $$0.getBooleanOr("LeftOwner", false);
        this.hasBeenShot = $$0.getBooleanOr("HasBeenShot", false);
    }

    @Override
    public void restoreFrom(Entity $$0) {
        super.restoreFrom($$0);
        if ($$0 instanceof Projectile) {
            Projectile $$1 = (Projectile)$$0;
            this.owner = $$1.owner;
        }
    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }
        this.checkLeftOwner();
        super.tick();
        this.leftOwnerChecked = false;
    }

    protected void checkLeftOwner() {
        if (!this.leftOwner && !this.leftOwnerChecked) {
            this.leftOwner = this.isOutsideOwnerCollisionRange();
            this.leftOwnerChecked = true;
        }
    }

    private boolean isOutsideOwnerCollisionRange() {
        Entity $$0 = this.getOwner();
        if ($$0 != null) {
            AABB $$12 = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
            return $$0.getRootVehicle().getSelfAndPassengers().filter(EntitySelector.CAN_BE_PICKED).noneMatch($$1 -> $$12.intersects($$1.getBoundingBox()));
        }
        return true;
    }

    public Vec3 getMovementToShoot(double $$0, double $$1, double $$2, float $$3, float $$4) {
        return new Vec3($$0, $$1, $$2).normalize().add(this.random.triangle(0.0, 0.0172275 * (double)$$4), this.random.triangle(0.0, 0.0172275 * (double)$$4), this.random.triangle(0.0, 0.0172275 * (double)$$4)).scale($$3);
    }

    public void shoot(double $$0, double $$1, double $$2, float $$3, float $$4) {
        Vec3 $$5 = this.getMovementToShoot($$0, $$1, $$2, $$3, $$4);
        this.setDeltaMovement($$5);
        this.needsSync = true;
        double $$6 = $$5.horizontalDistance();
        this.setYRot((float)(Mth.atan2($$5.x, $$5.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2($$5.y, $$6) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
        float $$6 = -Mth.sin($$2 * ((float)Math.PI / 180)) * Mth.cos($$1 * ((float)Math.PI / 180));
        float $$7 = -Mth.sin(($$1 + $$3) * ((float)Math.PI / 180));
        float $$8 = Mth.cos($$2 * ((float)Math.PI / 180)) * Mth.cos($$1 * ((float)Math.PI / 180));
        this.shoot($$6, $$7, $$8, $$4, $$5);
        Vec3 $$9 = $$0.getKnownMovement();
        this.setDeltaMovement(this.getDeltaMovement().add($$9.x, $$0.onGround() ? 0.0 : $$9.y, $$9.z));
    }

    @Override
    public void onAboveBubbleColumn(boolean $$0, BlockPos $$1) {
        double $$2 = $$0 ? -0.03 : 0.1;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, $$2, 0.0));
        Projectile.sendBubbleColumnParticles(this.level(), $$1);
    }

    @Override
    public void onInsideBubbleColumn(boolean $$0) {
        double $$1 = $$0 ? -0.03 : 0.06;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, $$1, 0.0));
        this.resetFallDistance();
    }

    public static <T extends Projectile> T spawnProjectileFromRotation(ProjectileFactory<T> $$0, ServerLevel $$1, ItemStack $$2, LivingEntity $$3, float $$42, float $$5, float $$6) {
        return (T)Projectile.spawnProjectile($$0.create($$1, $$3, $$2), $$1, $$2, $$4 -> $$4.shootFromRotation($$3, $$3.getXRot(), $$3.getYRot(), $$42, $$5, $$6));
    }

    public static <T extends Projectile> T spawnProjectileUsingShoot(ProjectileFactory<T> $$0, ServerLevel $$1, ItemStack $$2, LivingEntity $$3, double $$4, double $$52, double $$6, float $$7, float $$8) {
        return (T)Projectile.spawnProjectile($$0.create($$1, $$3, $$2), $$1, $$2, $$5 -> $$5.shoot($$4, $$52, $$6, $$7, $$8));
    }

    public static <T extends Projectile> T spawnProjectileUsingShoot(T $$0, ServerLevel $$1, ItemStack $$2, double $$3, double $$4, double $$5, float $$62, float $$7) {
        return (T)Projectile.spawnProjectile($$0, $$1, $$2, $$6 -> $$0.shoot($$3, $$4, $$5, $$62, $$7));
    }

    public static <T extends Projectile> T spawnProjectile(T $$02, ServerLevel $$1, ItemStack $$2) {
        return (T)Projectile.spawnProjectile($$02, $$1, $$2, $$0 -> {});
    }

    public static <T extends Projectile> T spawnProjectile(T $$0, ServerLevel $$1, ItemStack $$2, Consumer<T> $$3) {
        $$3.accept($$0);
        $$1.addFreshEntity($$0);
        $$0.applyOnProjectileSpawned($$1, $$2);
        return $$0;
    }

    public void applyOnProjectileSpawned(ServerLevel $$02, ItemStack $$1) {
        AbstractArrow $$2;
        ItemStack $$3;
        EnchantmentHelper.onProjectileSpawned($$02, $$1, this, $$0 -> {});
        Projectile projectile = this;
        if (projectile instanceof AbstractArrow && ($$3 = ($$2 = (AbstractArrow)projectile).getWeaponItem()) != null && !$$3.isEmpty() && !$$1.getItem().equals($$3.getItem())) {
            EnchantmentHelper.onProjectileSpawned($$02, $$3, this, $$2::onItemBreak);
        }
    }

    protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult $$0) {
        ProjectileDeflection $$5;
        BlockHitResult $$4;
        if ($$0.getType() == HitResult.Type.ENTITY) {
            EntityHitResult $$1 = (EntityHitResult)$$0;
            Entity $$2 = $$1.getEntity();
            ProjectileDeflection $$3 = $$2.deflection(this);
            if ($$3 != ProjectileDeflection.NONE) {
                if ($$2 != this.lastDeflectedBy && this.deflect($$3, $$2, this.owner, false)) {
                    this.lastDeflectedBy = $$2;
                }
                return $$3;
            }
        } else if (this.shouldBounceOnWorldBorder() && $$0 instanceof BlockHitResult && ($$4 = (BlockHitResult)$$0).isWorldBorderHit() && this.deflect($$5 = ProjectileDeflection.REVERSE, null, this.owner, false)) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            return $$5;
        }
        this.onHit($$0);
        return ProjectileDeflection.NONE;
    }

    protected boolean shouldBounceOnWorldBorder() {
        return false;
    }

    public boolean deflect(ProjectileDeflection $$0, @Nullable Entity $$1, @Nullable EntityReference<Entity> $$2, boolean $$3) {
        $$0.deflect(this, $$1, this.random);
        if (!this.level().isClientSide()) {
            this.setOwner($$2);
            this.onDeflection($$3);
        }
        return true;
    }

    protected void onDeflection(boolean $$0) {
    }

    protected void onItemBreak(Item $$0) {
    }

    protected void onHit(HitResult $$0) {
        HitResult.Type $$1 = $$0.getType();
        if ($$1 == HitResult.Type.ENTITY) {
            EntityHitResult $$2 = (EntityHitResult)$$0;
            Entity $$3 = $$2.getEntity();
            if ($$3.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && $$3 instanceof Projectile) {
                Projectile $$4 = (Projectile)$$3;
                $$4.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.owner, true);
            }
            this.onHitEntity($$2);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, $$0.getLocation(), GameEvent.Context.of(this, null));
        } else if ($$1 == HitResult.Type.BLOCK) {
            BlockHitResult $$5 = (BlockHitResult)$$0;
            this.onHitBlock($$5);
            BlockPos $$6 = $$5.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, $$6, GameEvent.Context.of(this, this.level().getBlockState($$6)));
        }
    }

    protected void onHitEntity(EntityHitResult $$0) {
    }

    protected void onHitBlock(BlockHitResult $$0) {
        BlockState $$1 = this.level().getBlockState($$0.getBlockPos());
        $$1.onProjectileHit(this.level(), $$1, $$0, this);
    }

    protected boolean canHitEntity(Entity $$0) {
        if (!$$0.canBeHitByProjectile()) {
            return false;
        }
        Entity $$1 = this.getOwner();
        return $$1 == null || this.leftOwner || !$$1.isPassengerOfSameVehicle($$0);
    }

    protected void updateRotation() {
        Vec3 $$0 = this.getDeltaMovement();
        double $$1 = $$0.horizontalDistance();
        this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2($$0.y, $$1) * 57.2957763671875)));
        this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2($$0.x, $$0.z) * 57.2957763671875)));
    }

    protected static float lerpRotation(float $$0, float $$1) {
        while ($$1 - $$0 < -180.0f) {
            $$0 -= 360.0f;
        }
        while ($$1 - $$0 >= 180.0f) {
            $$0 += 360.0f;
        }
        return Mth.lerp(0.2f, $$0, $$1);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
        Entity $$1 = this.getOwner();
        return new ClientboundAddEntityPacket((Entity)this, $$0, $$1 == null ? 0 : $$1.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
        super.recreateFromPacket($$0);
        Entity $$1 = this.level().getEntity($$0.getData());
        if ($$1 != null) {
            this.setOwner($$1);
        }
    }

    @Override
    public boolean mayInteract(ServerLevel $$0, BlockPos $$1) {
        Entity $$2 = this.getOwner();
        if ($$2 instanceof Player) {
            return $$2.mayInteract($$0, $$1);
        }
        return $$2 == null || $$0.getGameRules().get(GameRules.MOB_GRIEFING) != false;
    }

    public boolean mayBreak(ServerLevel $$0) {
        return this.getType().is(EntityTypeTags.IMPACT_PROJECTILES) && $$0.getGameRules().get(GameRules.PROJECTILES_CAN_BREAK_BLOCKS) != false;
    }

    @Override
    public boolean isPickable() {
        return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getPickRadius() {
        return this.isPickable() ? 1.0f : 0.0f;
    }

    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity $$0, DamageSource $$1) {
        double $$2 = this.getDeltaMovement().x;
        double $$3 = this.getDeltaMovement().z;
        return DoubleDoubleImmutablePair.of((double)$$2, (double)$$3);
    }

    @Override
    public int getDimensionChangingDelay() {
        return 2;
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (!this.isInvulnerableToBase($$1)) {
            this.markHurt();
        }
        return false;
    }

    @FunctionalInterface
    public static interface ProjectileFactory<T extends Projectile> {
        public T create(ServerLevel var1, LivingEntity var2, ItemStack var3);
    }
}

