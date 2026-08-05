/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ComparisonChain
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MobEffectInstance
implements Comparable<MobEffectInstance> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int INFINITE_DURATION = -1;
    public static final int MIN_AMPLIFIER = 0;
    public static final int MAX_AMPLIFIER = 255;
    public static final Codec<MobEffectInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)MobEffect.CODEC.fieldOf("id").forGetter(MobEffectInstance::getEffect), (App)Details.MAP_CODEC.forGetter(MobEffectInstance::asDetails)).apply((Applicative)$$0, MobEffectInstance::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MobEffectInstance> STREAM_CODEC = StreamCodec.composite(MobEffect.STREAM_CODEC, MobEffectInstance::getEffect, Details.STREAM_CODEC, MobEffectInstance::asDetails, MobEffectInstance::new);
    private final Holder<MobEffect> effect;
    private int duration;
    private int amplifier;
    private boolean ambient;
    private boolean visible;
    private boolean showIcon;
    private @Nullable MobEffectInstance hiddenEffect;
    private final BlendState blendState = new BlendState();

    public MobEffectInstance(Holder<MobEffect> $$0) {
        this($$0, 0, 0);
    }

    public MobEffectInstance(Holder<MobEffect> $$0, int $$1) {
        this($$0, $$1, 0);
    }

    public MobEffectInstance(Holder<MobEffect> $$0, int $$1, int $$2) {
        this($$0, $$1, $$2, false, true);
    }

    public MobEffectInstance(Holder<MobEffect> $$0, int $$1, int $$2, boolean $$3, boolean $$4) {
        this($$0, $$1, $$2, $$3, $$4, $$4);
    }

    public MobEffectInstance(Holder<MobEffect> $$0, int $$1, int $$2, boolean $$3, boolean $$4, boolean $$5) {
        this($$0, $$1, $$2, $$3, $$4, $$5, null);
    }

    public MobEffectInstance(Holder<MobEffect> $$0, int $$1, int $$2, boolean $$3, boolean $$4, boolean $$5, @Nullable MobEffectInstance $$6) {
        this.effect = $$0;
        this.duration = $$1;
        this.amplifier = Mth.clamp($$2, 0, 255);
        this.ambient = $$3;
        this.visible = $$4;
        this.showIcon = $$5;
        this.hiddenEffect = $$6;
    }

    public MobEffectInstance(MobEffectInstance $$0) {
        this.effect = $$0.effect;
        this.setDetailsFrom($$0);
    }

    private MobEffectInstance(Holder<MobEffect> $$0, Details $$12) {
        this($$0, $$12.duration(), $$12.amplifier(), $$12.ambient(), $$12.showParticles(), $$12.showIcon(), $$12.hiddenEffect().map($$1 -> new MobEffectInstance($$0, (Details)$$1)).orElse(null));
    }

    private Details asDetails() {
        return new Details(this.getAmplifier(), this.getDuration(), this.isAmbient(), this.isVisible(), this.showIcon(), Optional.ofNullable(this.hiddenEffect).map(MobEffectInstance::asDetails));
    }

    public float getBlendFactor(LivingEntity $$0, float $$1) {
        return this.blendState.getFactor($$0, $$1);
    }

    public ParticleOptions getParticleOptions() {
        return this.effect.value().createParticleOptions(this);
    }

    void setDetailsFrom(MobEffectInstance $$0) {
        this.duration = $$0.duration;
        this.amplifier = $$0.amplifier;
        this.ambient = $$0.ambient;
        this.visible = $$0.visible;
        this.showIcon = $$0.showIcon;
    }

    public boolean update(MobEffectInstance $$0) {
        if (!this.effect.equals($$0.effect)) {
            LOGGER.warn("This method should only be called for matching effects!");
        }
        boolean $$1 = false;
        if ($$0.amplifier > this.amplifier) {
            if ($$0.isShorterDurationThan(this)) {
                MobEffectInstance $$2 = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance(this);
                this.hiddenEffect.hiddenEffect = $$2;
            }
            this.amplifier = $$0.amplifier;
            this.duration = $$0.duration;
            $$1 = true;
        } else if (this.isShorterDurationThan($$0)) {
            if ($$0.amplifier == this.amplifier) {
                this.duration = $$0.duration;
                $$1 = true;
            } else if (this.hiddenEffect == null) {
                this.hiddenEffect = new MobEffectInstance($$0);
            } else {
                this.hiddenEffect.update($$0);
            }
        }
        if (!$$0.ambient && this.ambient || $$1) {
            this.ambient = $$0.ambient;
            $$1 = true;
        }
        if ($$0.visible != this.visible) {
            this.visible = $$0.visible;
            $$1 = true;
        }
        if ($$0.showIcon != this.showIcon) {
            this.showIcon = $$0.showIcon;
            $$1 = true;
        }
        return $$1;
    }

    private boolean isShorterDurationThan(MobEffectInstance $$0) {
        return !this.isInfiniteDuration() && (this.duration < $$0.duration || $$0.isInfiniteDuration());
    }

    public boolean isInfiniteDuration() {
        return this.duration == -1;
    }

    public boolean endsWithin(int $$0) {
        return !this.isInfiniteDuration() && this.duration <= $$0;
    }

    public MobEffectInstance withScaledDuration(float $$0) {
        MobEffectInstance $$12 = new MobEffectInstance(this);
        $$12.duration = $$12.mapDuration($$1 -> Math.max(Mth.floor((float)$$1 * $$0), 1));
        return $$12;
    }

    public int mapDuration(Int2IntFunction $$0) {
        if (this.isInfiniteDuration() || this.duration == 0) {
            return this.duration;
        }
        return $$0.applyAsInt(this.duration);
    }

    public Holder<MobEffect> getEffect() {
        return this.effect;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isAmbient() {
        return this.ambient;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public boolean tickServer(ServerLevel $$0, LivingEntity $$1, Runnable $$2) {
        int $$3;
        if (!this.hasRemainingDuration()) {
            return false;
        }
        int n = $$3 = this.isInfiniteDuration() ? $$1.tickCount : this.duration;
        if (this.effect.value().shouldApplyEffectTickThisTick($$3, this.amplifier) && !this.effect.value().applyEffectTick($$0, $$1, this.amplifier)) {
            return false;
        }
        this.tickDownDuration();
        if (this.downgradeToHiddenEffect()) {
            $$2.run();
        }
        return this.hasRemainingDuration();
    }

    public void tickClient() {
        if (this.hasRemainingDuration()) {
            this.tickDownDuration();
            this.downgradeToHiddenEffect();
        }
        this.blendState.tick(this);
    }

    private boolean hasRemainingDuration() {
        return this.isInfiniteDuration() || this.duration > 0;
    }

    private void tickDownDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.tickDownDuration();
        }
        this.duration = this.mapDuration($$0 -> $$0 - 1);
    }

    private boolean downgradeToHiddenEffect() {
        if (this.duration == 0 && this.hiddenEffect != null) {
            this.setDetailsFrom(this.hiddenEffect);
            this.hiddenEffect = this.hiddenEffect.hiddenEffect;
            return true;
        }
        return false;
    }

    public void onEffectStarted(LivingEntity $$0) {
        this.effect.value().onEffectStarted($$0, this.amplifier);
    }

    public void onMobRemoved(ServerLevel $$0, LivingEntity $$1, Entity.RemovalReason $$2) {
        this.effect.value().onMobRemoved($$0, $$1, this.amplifier, $$2);
    }

    public void onMobHurt(ServerLevel $$0, LivingEntity $$1, DamageSource $$2, float $$3) {
        this.effect.value().onMobHurt($$0, $$1, this.amplifier, $$2, $$3);
    }

    public String getDescriptionId() {
        return this.effect.value().getDescriptionId();
    }

    public String toString() {
        String $$1;
        if (this.amplifier > 0) {
            String $$0 = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration();
        } else {
            $$1 = this.getDescriptionId() + ", Duration: " + this.describeDuration();
        }
        if (!this.visible) {
            $$1 = $$1 + ", Particles: false";
        }
        if (!this.showIcon) {
            $$1 = $$1 + ", Show Icon: false";
        }
        return $$1;
    }

    private String describeDuration() {
        if (this.isInfiniteDuration()) {
            return "infinite";
        }
        return Integer.toString(this.duration);
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 instanceof MobEffectInstance) {
            MobEffectInstance $$1 = (MobEffectInstance)$$0;
            return this.duration == $$1.duration && this.amplifier == $$1.amplifier && this.ambient == $$1.ambient && this.visible == $$1.visible && this.showIcon == $$1.showIcon && this.effect.equals($$1.effect);
        }
        return false;
    }

    public int hashCode() {
        int $$0 = this.effect.hashCode();
        $$0 = 31 * $$0 + this.duration;
        $$0 = 31 * $$0 + this.amplifier;
        $$0 = 31 * $$0 + (this.ambient ? 1 : 0);
        $$0 = 31 * $$0 + (this.visible ? 1 : 0);
        $$0 = 31 * $$0 + (this.showIcon ? 1 : 0);
        return $$0;
    }

    @Override
    public int compareTo(MobEffectInstance $$0) {
        int $$1 = 32147;
        if (this.getDuration() > 32147 && $$0.getDuration() > 32147 || this.isAmbient() && $$0.isAmbient()) {
            return ComparisonChain.start().compare(Boolean.valueOf(this.isAmbient()), Boolean.valueOf($$0.isAmbient())).compare(this.getEffect().value().getColor(), $$0.getEffect().value().getColor()).result();
        }
        return ComparisonChain.start().compareFalseFirst(this.isAmbient(), $$0.isAmbient()).compareFalseFirst(this.isInfiniteDuration(), $$0.isInfiniteDuration()).compare(this.getDuration(), $$0.getDuration()).compare(this.getEffect().value().getColor(), $$0.getEffect().value().getColor()).result();
    }

    public void onEffectAdded(LivingEntity $$0) {
        this.effect.value().onEffectAdded($$0, this.amplifier);
    }

    public boolean is(Holder<MobEffect> $$0) {
        return this.effect.equals($$0);
    }

    public void copyBlendState(MobEffectInstance $$0) {
        this.blendState.copyFrom($$0.blendState);
    }

    public void skipBlending() {
        this.blendState.setImmediate(this);
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((MobEffectInstance)object);
    }

    static class BlendState {
        private float factor;
        private float factorPreviousFrame;

        BlendState() {
        }

        public void setImmediate(MobEffectInstance $$0) {
            this.factorPreviousFrame = this.factor = BlendState.hasEffect($$0) ? 1.0f : 0.0f;
        }

        public void copyFrom(BlendState $$0) {
            this.factor = $$0.factor;
            this.factorPreviousFrame = $$0.factorPreviousFrame;
        }

        public void tick(MobEffectInstance $$0) {
            int $$4;
            float $$2;
            this.factorPreviousFrame = this.factor;
            boolean $$1 = BlendState.hasEffect($$0);
            float f = $$2 = $$1 ? 1.0f : 0.0f;
            if (this.factor == $$2) {
                return;
            }
            MobEffect $$3 = $$0.getEffect().value();
            int n = $$4 = $$1 ? $$3.getBlendInDurationTicks() : $$3.getBlendOutDurationTicks();
            if ($$4 == 0) {
                this.factor = $$2;
            } else {
                float $$5 = 1.0f / (float)$$4;
                this.factor += Mth.clamp($$2 - this.factor, -$$5, $$5);
            }
        }

        private static boolean hasEffect(MobEffectInstance $$0) {
            return !$$0.endsWithin($$0.getEffect().value().getBlendOutAdvanceTicks());
        }

        public float getFactor(LivingEntity $$0, float $$1) {
            if ($$0.isRemoved()) {
                this.factorPreviousFrame = this.factor;
            }
            return Mth.lerp($$1, this.factorPreviousFrame, this.factor);
        }
    }

    record Details(int amplifier, int duration, boolean ambient, boolean showParticles, boolean showIcon, Optional<Details> hiddenEffect) {
        public static final MapCodec<Details> MAP_CODEC = MapCodec.recursive((String)"MobEffectInstance.Details", $$0 -> RecordCodecBuilder.mapCodec($$1 -> $$1.group((App)ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", (Object)0).forGetter(Details::amplifier), (App)Codec.INT.optionalFieldOf("duration", (Object)0).forGetter(Details::duration), (App)Codec.BOOL.optionalFieldOf("ambient", (Object)false).forGetter(Details::ambient), (App)Codec.BOOL.optionalFieldOf("show_particles", (Object)true).forGetter(Details::showParticles), (App)Codec.BOOL.optionalFieldOf("show_icon").forGetter($$0 -> Optional.of($$0.showIcon())), (App)$$0.optionalFieldOf("hidden_effect").forGetter(Details::hiddenEffect)).apply((Applicative)$$1, Details::create)));
        public static final StreamCodec<ByteBuf, Details> STREAM_CODEC = StreamCodec.recursive($$0 -> StreamCodec.composite(ByteBufCodecs.VAR_INT, Details::amplifier, ByteBufCodecs.VAR_INT, Details::duration, ByteBufCodecs.BOOL, Details::ambient, ByteBufCodecs.BOOL, Details::showParticles, ByteBufCodecs.BOOL, Details::showIcon, $$0.apply(ByteBufCodecs::optional), Details::hiddenEffect, Details::new));

        private static Details create(int $$0, int $$1, boolean $$2, boolean $$3, Optional<Boolean> $$4, Optional<Details> $$5) {
            return new Details($$0, $$1, $$2, $$3, $$4.orElse($$3), $$5);
        }
    }
}

