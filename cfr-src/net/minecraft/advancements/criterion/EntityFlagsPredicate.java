/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record EntityFlagsPredicate(Optional<Boolean> isOnGround, Optional<Boolean> isOnFire, Optional<Boolean> isCrouching, Optional<Boolean> isSprinting, Optional<Boolean> isSwimming, Optional<Boolean> isFlying, Optional<Boolean> isBaby, Optional<Boolean> isInWater, Optional<Boolean> isFallFlying) {
    public static final Codec<EntityFlagsPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.BOOL.optionalFieldOf("is_on_ground").forGetter(EntityFlagsPredicate::isOnGround), (App)Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(EntityFlagsPredicate::isOnFire), (App)Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(EntityFlagsPredicate::isCrouching), (App)Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(EntityFlagsPredicate::isSprinting), (App)Codec.BOOL.optionalFieldOf("is_swimming").forGetter(EntityFlagsPredicate::isSwimming), (App)Codec.BOOL.optionalFieldOf("is_flying").forGetter(EntityFlagsPredicate::isFlying), (App)Codec.BOOL.optionalFieldOf("is_baby").forGetter(EntityFlagsPredicate::isBaby), (App)Codec.BOOL.optionalFieldOf("is_in_water").forGetter(EntityFlagsPredicate::isInWater), (App)Codec.BOOL.optionalFieldOf("is_fall_flying").forGetter(EntityFlagsPredicate::isFallFlying)).apply((Applicative)$$0, EntityFlagsPredicate::new));

    /*
     * Unable to fully structure code
     */
    public boolean matches(Entity $$0) {
        block11: {
            if (this.isOnGround.isPresent() && $$0.onGround() != this.isOnGround.get().booleanValue()) {
                return false;
            }
            if (this.isOnFire.isPresent() && $$0.isOnFire() != this.isOnFire.get().booleanValue()) {
                return false;
            }
            if (this.isCrouching.isPresent() && $$0.isCrouching() != this.isCrouching.get().booleanValue()) {
                return false;
            }
            if (this.isSprinting.isPresent() && $$0.isSprinting() != this.isSprinting.get().booleanValue()) {
                return false;
            }
            if (this.isSwimming.isPresent() && $$0.isSwimming() != this.isSwimming.get().booleanValue()) {
                return false;
            }
            if (!this.isFlying.isPresent()) break block11;
            if (!($$0 instanceof LivingEntity)) ** GOTO lbl-1000
            $$1 = (LivingEntity)$$0;
            if ($$1.isFallFlying()) ** GOTO lbl-1000
            if ($$1 instanceof Player) {
                $$2 = (Player)$$1;
                ** if (!$$2.getAbilities().flying) goto lbl-1000
            }
            ** GOTO lbl-1000
lbl-1000:
            // 2 sources

            {
                v0 = true;
                ** GOTO lbl22
            }
lbl-1000:
            // 3 sources

            {
                v0 = $$3 = false;
            }
lbl22:
            // 2 sources

            if ($$3 != this.isFlying.get()) {
                return false;
            }
        }
        if (this.isInWater.isPresent() && $$0.isInWater() != this.isInWater.get().booleanValue()) {
            return false;
        }
        if (this.isFallFlying.isPresent() && $$0 instanceof LivingEntity && ($$4 = (LivingEntity)$$0).isFallFlying() != this.isFallFlying.get().booleanValue()) {
            return false;
        }
        return this.isBaby.isPresent() == false || $$0 instanceof LivingEntity == false || ($$5 = (LivingEntity)$$0).isBaby() == this.isBaby.get().booleanValue();
    }

    public static class Builder {
        private Optional<Boolean> isOnGround = Optional.empty();
        private Optional<Boolean> isOnFire = Optional.empty();
        private Optional<Boolean> isCrouching = Optional.empty();
        private Optional<Boolean> isSprinting = Optional.empty();
        private Optional<Boolean> isSwimming = Optional.empty();
        private Optional<Boolean> isFlying = Optional.empty();
        private Optional<Boolean> isBaby = Optional.empty();
        private Optional<Boolean> isInWater = Optional.empty();
        private Optional<Boolean> isFallFlying = Optional.empty();

        public static Builder flags() {
            return new Builder();
        }

        public Builder setOnGround(Boolean $$0) {
            this.isOnGround = Optional.of($$0);
            return this;
        }

        public Builder setOnFire(Boolean $$0) {
            this.isOnFire = Optional.of($$0);
            return this;
        }

        public Builder setCrouching(Boolean $$0) {
            this.isCrouching = Optional.of($$0);
            return this;
        }

        public Builder setSprinting(Boolean $$0) {
            this.isSprinting = Optional.of($$0);
            return this;
        }

        public Builder setSwimming(Boolean $$0) {
            this.isSwimming = Optional.of($$0);
            return this;
        }

        public Builder setIsFlying(Boolean $$0) {
            this.isFlying = Optional.of($$0);
            return this;
        }

        public Builder setIsBaby(Boolean $$0) {
            this.isBaby = Optional.of($$0);
            return this;
        }

        public Builder setIsInWater(Boolean $$0) {
            this.isInWater = Optional.of($$0);
            return this;
        }

        public Builder setIsFallFlying(Boolean $$0) {
            this.isFallFlying = Optional.of($$0);
            return this;
        }

        public EntityFlagsPredicate build() {
            return new EntityFlagsPredicate(this.isOnGround, this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isFlying, this.isBaby, this.isInWater, this.isFallFlying);
        }
    }
}

