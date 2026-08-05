/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class CrossbowItem
extends ProjectileWeaponItem {
    private static final float MAX_CHARGE_DURATION = 1.25f;
    public static final int DEFAULT_RANGE = 8;
    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;
    private static final float START_SOUND_PERCENT = 0.2f;
    private static final float MID_SOUND_PERCENT = 0.5f;
    private static final float ARROW_POWER = 3.15f;
    private static final float FIREWORK_POWER = 1.6f;
    public static final float MOB_ARROW_POWER = 1.6f;
    private static final ChargingSounds DEFAULT_SOUNDS = new ChargingSounds(Optional.of(SoundEvents.CROSSBOW_LOADING_START), Optional.of(SoundEvents.CROSSBOW_LOADING_MIDDLE), Optional.of(SoundEvents.CROSSBOW_LOADING_END));

    public CrossbowItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return ARROW_OR_FIREWORK;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
        ItemStack $$3 = $$1.getItemInHand($$2);
        ChargedProjectiles $$4 = $$3.get(DataComponents.CHARGED_PROJECTILES);
        if ($$4 != null && !$$4.isEmpty()) {
            this.performShooting($$0, $$1, $$2, $$3, CrossbowItem.getShootingPower($$4), 1.0f, null);
            return InteractionResult.CONSUME;
        }
        if (!$$1.getProjectile($$3).isEmpty()) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
            $$1.startUsingItem($$2);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    private static float getShootingPower(ChargedProjectiles $$0) {
        if ($$0.contains(Items.FIREWORK_ROCKET)) {
            return 1.6f;
        }
        return 3.15f;
    }

    @Override
    public boolean releaseUsing(ItemStack $$0, Level $$1, LivingEntity $$2, int $$3) {
        int $$4 = this.getUseDuration($$0, $$2) - $$3;
        return CrossbowItem.getPowerForTime($$4, $$0, $$2) >= 1.0f && CrossbowItem.isCharged($$0);
    }

    private static boolean tryLoadProjectiles(LivingEntity $$0, ItemStack $$1) {
        List<ItemStack> $$2 = CrossbowItem.draw($$1, $$0.getProjectile($$1), $$0);
        if (!$$2.isEmpty()) {
            $$1.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of($$2));
            return true;
        }
        return false;
    }

    public static boolean isCharged(ItemStack $$0) {
        ChargedProjectiles $$1 = $$0.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        return !$$1.isEmpty();
    }

    @Override
    protected void shootProjectile(LivingEntity $$0, Projectile $$1, int $$2, float $$3, float $$4, float $$5, @Nullable LivingEntity $$6) {
        Vector3f $$15;
        if ($$6 != null) {
            double $$7 = $$6.getX() - $$0.getX();
            double $$8 = $$6.getZ() - $$0.getZ();
            double $$9 = Math.sqrt($$7 * $$7 + $$8 * $$8);
            double $$10 = $$6.getY(0.3333333333333333) - $$1.getY() + $$9 * (double)0.2f;
            Vector3f $$11 = CrossbowItem.getProjectileShotVector($$0, new Vec3($$7, $$10, $$8), $$5);
        } else {
            Vec3 $$12 = $$0.getUpVector(1.0f);
            Quaternionf $$13 = new Quaternionf().setAngleAxis((double)($$5 * ((float)Math.PI / 180)), $$12.x, $$12.y, $$12.z);
            Vec3 $$14 = $$0.getViewVector(1.0f);
            $$15 = $$14.toVector3f().rotate((Quaternionfc)$$13);
        }
        $$1.shoot($$15.x(), $$15.y(), $$15.z(), $$3, $$4);
        float $$16 = CrossbowItem.getShotPitch($$0.getRandom(), $$2);
        $$0.level().playSound(null, $$0.getX(), $$0.getY(), $$0.getZ(), SoundEvents.CROSSBOW_SHOOT, $$0.getSoundSource(), 1.0f, $$16);
    }

    private static Vector3f getProjectileShotVector(LivingEntity $$0, Vec3 $$1, float $$2) {
        Vector3f $$3 = $$1.toVector3f().normalize();
        Vector3f $$4 = new Vector3f((Vector3fc)$$3).cross((Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f));
        if ((double)$$4.lengthSquared() <= 1.0E-7) {
            Vec3 $$5 = $$0.getUpVector(1.0f);
            $$4 = new Vector3f((Vector3fc)$$3).cross((Vector3fc)$$5.toVector3f());
        }
        Vector3f $$6 = new Vector3f((Vector3fc)$$3).rotateAxis(1.5707964f, $$4.x, $$4.y, $$4.z);
        return new Vector3f((Vector3fc)$$3).rotateAxis($$2 * ((float)Math.PI / 180), $$6.x, $$6.y, $$6.z);
    }

    @Override
    protected Projectile createProjectile(Level $$0, LivingEntity $$1, ItemStack $$2, ItemStack $$3, boolean $$4) {
        if ($$3.is(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity($$0, $$3, $$1, $$1.getX(), $$1.getEyeY() - (double)0.15f, $$1.getZ(), true);
        }
        Projectile $$5 = super.createProjectile($$0, $$1, $$2, $$3, $$4);
        if ($$5 instanceof AbstractArrow) {
            AbstractArrow $$6 = (AbstractArrow)$$5;
            $$6.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        }
        return $$5;
    }

    @Override
    protected int getDurabilityUse(ItemStack $$0) {
        return $$0.is(Items.FIREWORK_ROCKET) ? 3 : 1;
    }

    /*
     * WARNING - void declaration
     */
    public void performShooting(Level $$0, LivingEntity $$1, InteractionHand $$2, ItemStack $$3, float $$4, float $$5, @Nullable LivingEntity $$6) {
        void $$8;
        if (!($$0 instanceof ServerLevel)) {
            return;
        }
        ServerLevel $$7 = (ServerLevel)$$0;
        ChargedProjectiles $$9 = $$3.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        if ($$9 == null || $$9.isEmpty()) {
            return;
        }
        this.shoot((ServerLevel)$$8, $$1, $$2, $$3, $$9.getItems(), $$4, $$5, $$1 instanceof Player, $$6);
        if ($$1 instanceof ServerPlayer) {
            ServerPlayer $$10 = (ServerPlayer)$$1;
            CriteriaTriggers.SHOT_CROSSBOW.trigger($$10, $$3);
            $$10.awardStat(Stats.ITEM_USED.get($$3.getItem()));
        }
    }

    private static float getShotPitch(RandomSource $$0, int $$1) {
        if ($$1 == 0) {
            return 1.0f;
        }
        return CrossbowItem.getRandomShotPitch(($$1 & 1) == 1, $$0);
    }

    private static float getRandomShotPitch(boolean $$0, RandomSource $$1) {
        float $$2 = $$0 ? 0.63f : 0.43f;
        return 1.0f / ($$1.nextFloat() * 0.5f + 1.8f) + $$2;
    }

    @Override
    public void onUseTick(Level $$0, LivingEntity $$1, ItemStack $$22, int $$3) {
        if (!$$0.isClientSide()) {
            ChargingSounds $$4 = this.getChargingSounds($$22);
            float $$5 = (float)($$22.getUseDuration($$1) - $$3) / (float)CrossbowItem.getChargeDuration($$22, $$1);
            if ($$5 < 0.2f) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }
            if ($$5 >= 0.2f && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                $$4.start().ifPresent($$2 -> $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), (SoundEvent)$$2.value(), SoundSource.PLAYERS, 0.5f, 1.0f));
            }
            if ($$5 >= 0.5f && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                $$4.mid().ifPresent($$2 -> $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), (SoundEvent)$$2.value(), SoundSource.PLAYERS, 0.5f, 1.0f));
            }
            if ($$5 >= 1.0f && !CrossbowItem.isCharged($$22) && CrossbowItem.tryLoadProjectiles($$1, $$22)) {
                $$4.end().ifPresent($$2 -> $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), (SoundEvent)$$2.value(), $$1.getSoundSource(), 1.0f, 1.0f / ($$0.getRandom().nextFloat() * 0.5f + 1.0f) + 0.2f));
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack $$0, LivingEntity $$1) {
        return 72000;
    }

    public static int getChargeDuration(ItemStack $$0, LivingEntity $$1) {
        float $$2 = EnchantmentHelper.modifyCrossbowChargingTime($$0, $$1, 1.25f);
        return Mth.floor($$2 * 20.0f);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack $$0) {
        return ItemUseAnimation.CROSSBOW;
    }

    ChargingSounds getChargingSounds(ItemStack $$0) {
        return EnchantmentHelper.pickHighestLevel($$0, EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS).orElse(DEFAULT_SOUNDS);
    }

    private static float getPowerForTime(int $$0, ItemStack $$1, LivingEntity $$2) {
        float $$3 = (float)$$0 / (float)CrossbowItem.getChargeDuration($$1, $$2);
        if ($$3 > 1.0f) {
            $$3 = 1.0f;
        }
        return $$3;
    }

    @Override
    public boolean useOnRelease(ItemStack $$0) {
        return $$0.is(this);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 8;
    }

    public record ChargingSounds(Optional<Holder<SoundEvent>> start, Optional<Holder<SoundEvent>> mid, Optional<Holder<SoundEvent>> end) {
        public static final Codec<ChargingSounds> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)SoundEvent.CODEC.optionalFieldOf("start").forGetter(ChargingSounds::start), (App)SoundEvent.CODEC.optionalFieldOf("mid").forGetter(ChargingSounds::mid), (App)SoundEvent.CODEC.optionalFieldOf("end").forGetter(ChargingSounds::end)).apply((Applicative)$$0, ChargingSounds::new));
    }

    public static enum ChargeType implements StringRepresentable
    {
        NONE("none"),
        ARROW("arrow"),
        ROCKET("rocket");

        public static final Codec<ChargeType> CODEC;
        private final String name;

        private ChargeType(String $$0) {
            this.name = $$0;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(ChargeType::values);
        }
    }
}

