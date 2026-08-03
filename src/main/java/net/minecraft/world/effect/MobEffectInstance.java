package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MobEffectInstance implements Comparable<net.minecraft.world.effect.MobEffectInstance> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int INFINITE_DURATION = -1;
   public static final int MIN_AMPLIFIER = 0;
   public static final int MAX_AMPLIFIER = 255;
   public static final Codec<net.minecraft.world.effect.MobEffectInstance> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.world.effect.MobEffect.CODEC.fieldOf("id").forGetter(net.minecraft.world.effect.MobEffectInstance::getEffect),
            net.minecraft.world.effect.MobEffectInstance.Details.MAP_CODEC.forGetter(net.minecraft.world.effect.MobEffectInstance::asDetails)
         )
         .apply($$0, net.minecraft.world.effect.MobEffectInstance::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.effect.MobEffectInstance> STREAM_CODEC = StreamCodec.composite(
      net.minecraft.world.effect.MobEffect.STREAM_CODEC,
      net.minecraft.world.effect.MobEffectInstance::getEffect,
      net.minecraft.world.effect.MobEffectInstance.Details.STREAM_CODEC,
      net.minecraft.world.effect.MobEffectInstance::asDetails,
      net.minecraft.world.effect.MobEffectInstance::new
   );
   private final Holder<net.minecraft.world.effect.MobEffect> effect;
   private int duration;
   private int amplifier;
   private boolean ambient;
   private boolean visible;
   private boolean showIcon;
   @Nullable
   private net.minecraft.world.effect.MobEffectInstance hiddenEffect;
   private final net.minecraft.world.effect.MobEffectInstance.BlendState blendState = new net.minecraft.world.effect.MobEffectInstance.BlendState();

   public MobEffectInstance(Holder<net.minecraft.world.effect.MobEffect> $$0) {
      this($$0, 0, 0);
   }

   public MobEffectInstance(Holder<net.minecraft.world.effect.MobEffect> $$0, int $$1) {
      this($$0, $$1, 0);
   }

   public MobEffectInstance(Holder<net.minecraft.world.effect.MobEffect> $$0, int $$1, int $$2) {
      this($$0, $$1, $$2, false, true);
   }

   public MobEffectInstance(Holder<net.minecraft.world.effect.MobEffect> $$0, int $$1, int $$2, boolean $$3, boolean $$4) {
      this($$0, $$1, $$2, $$3, $$4, $$4);
   }

   public MobEffectInstance(Holder<net.minecraft.world.effect.MobEffect> $$0, int $$1, int $$2, boolean $$3, boolean $$4, boolean $$5) {
      this($$0, $$1, $$2, $$3, $$4, $$5, null);
   }

   public MobEffectInstance(
      Holder<net.minecraft.world.effect.MobEffect> $$0,
      int $$1,
      int $$2,
      boolean $$3,
      boolean $$4,
      boolean $$5,
      @Nullable net.minecraft.world.effect.MobEffectInstance $$6
   ) {
      this.effect = $$0;
      this.duration = $$1;
      this.amplifier = Mth.clamp($$2, 0, 255);
      this.ambient = $$3;
      this.visible = $$4;
      this.showIcon = $$5;
      this.hiddenEffect = $$6;
   }

   public MobEffectInstance(net.minecraft.world.effect.MobEffectInstance $$0) {
      this.effect = $$0.effect;
      this.setDetailsFrom($$0);
   }

   private MobEffectInstance(Holder<net.minecraft.world.effect.MobEffect> $$0, net.minecraft.world.effect.MobEffectInstance.Details $$1) {
      this(
         $$0,
         $$1.duration(),
         $$1.amplifier(),
         $$1.ambient(),
         $$1.showParticles(),
         $$1.showIcon(),
         $$1.hiddenEffect().map($$1x -> new net.minecraft.world.effect.MobEffectInstance($$0, $$1x)).orElse(null)
      );
   }

   private net.minecraft.world.effect.MobEffectInstance.Details asDetails() {
      return new net.minecraft.world.effect.MobEffectInstance.Details(
         this.getAmplifier(),
         this.getDuration(),
         this.isAmbient(),
         this.isVisible(),
         this.showIcon(),
         Optional.ofNullable(this.hiddenEffect).map(net.minecraft.world.effect.MobEffectInstance::asDetails)
      );
   }

   public float getBlendFactor(LivingEntity $$0, float $$1) {
      return this.blendState.getFactor($$0, $$1);
   }

   public ParticleOptions getParticleOptions() {
      return ((net.minecraft.world.effect.MobEffect)this.effect.value()).createParticleOptions(this);
   }

   void setDetailsFrom(net.minecraft.world.effect.MobEffectInstance $$0) {
      this.duration = $$0.duration;
      this.amplifier = $$0.amplifier;
      this.ambient = $$0.ambient;
      this.visible = $$0.visible;
      this.showIcon = $$0.showIcon;
   }

   public boolean update(net.minecraft.world.effect.MobEffectInstance $$0) {
      if (!this.effect.equals($$0.effect)) {
         LOGGER.warn("This method should only be called for matching effects!");
      }

      boolean $$1 = false;
      if ($$0.amplifier > this.amplifier) {
         if ($$0.isShorterDurationThan(this)) {
            net.minecraft.world.effect.MobEffectInstance $$2 = this.hiddenEffect;
            this.hiddenEffect = new net.minecraft.world.effect.MobEffectInstance(this);
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
            this.hiddenEffect = new net.minecraft.world.effect.MobEffectInstance($$0);
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

   private boolean isShorterDurationThan(net.minecraft.world.effect.MobEffectInstance $$0) {
      return !this.isInfiniteDuration() && (this.duration < $$0.duration || $$0.isInfiniteDuration());
   }

   public boolean isInfiniteDuration() {
      return this.duration == -1;
   }

   public boolean endsWithin(int $$0) {
      return !this.isInfiniteDuration() && this.duration <= $$0;
   }

   public net.minecraft.world.effect.MobEffectInstance withScaledDuration(float $$0) {
      net.minecraft.world.effect.MobEffectInstance $$1 = new net.minecraft.world.effect.MobEffectInstance(this);
      $$1.duration = $$1.mapDuration($$1x -> Math.max(Mth.floor($$1x * $$0), 1));
      return $$1;
   }

   public int mapDuration(Int2IntFunction $$0) {
      return !this.isInfiniteDuration() && this.duration != 0 ? $$0.applyAsInt(this.duration) : this.duration;
   }

   public Holder<net.minecraft.world.effect.MobEffect> getEffect() {
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
      if (!this.hasRemainingDuration()) {
         return false;
      } else {
         int $$3 = this.isInfiniteDuration() ? $$1.tickCount : this.duration;
         if (((net.minecraft.world.effect.MobEffect)this.effect.value()).shouldApplyEffectTickThisTick($$3, this.amplifier)
            && !((net.minecraft.world.effect.MobEffect)this.effect.value()).applyEffectTick($$0, $$1, this.amplifier)) {
            return false;
         } else {
            this.tickDownDuration();
            if (this.downgradeToHiddenEffect()) {
               $$2.run();
            }

            return this.hasRemainingDuration();
         }
      }
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
      } else {
         return false;
      }
   }

   public void onEffectStarted(LivingEntity $$0) {
      ((net.minecraft.world.effect.MobEffect)this.effect.value()).onEffectStarted($$0, this.amplifier);
   }

   public void onMobRemoved(ServerLevel $$0, LivingEntity $$1, RemovalReason $$2) {
      ((net.minecraft.world.effect.MobEffect)this.effect.value()).onMobRemoved($$0, $$1, this.amplifier, $$2);
   }

   public void onMobHurt(ServerLevel $$0, LivingEntity $$1, DamageSource $$2, float $$3) {
      ((net.minecraft.world.effect.MobEffect)this.effect.value()).onMobHurt($$0, $$1, this.amplifier, $$2, $$3);
   }

   public String getDescriptionId() {
      return ((net.minecraft.world.effect.MobEffect)this.effect.value()).getDescriptionId();
   }

   @Override
   public String toString() {
      String $$0;
      if (this.amplifier > 0) {
         $$0 = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration();
      } else {
         $$0 = this.getDescriptionId() + ", Duration: " + this.describeDuration();
      }

      if (!this.visible) {
         $$0 = $$0 + ", Particles: false";
      }

      if (!this.showIcon) {
         $$0 = $$0 + ", Show Icon: false";
      }

      return $$0;
   }

   private String describeDuration() {
      return this.isInfiniteDuration() ? "infinite" : Integer.toString(this.duration);
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return !($$0 instanceof net.minecraft.world.effect.MobEffectInstance $$1)
            ? false
            : this.duration == $$1.duration
               && this.amplifier == $$1.amplifier
               && this.ambient == $$1.ambient
               && this.visible == $$1.visible
               && this.showIcon == $$1.showIcon
               && this.effect.equals($$1.effect);
      }
   }

   @Override
   public int hashCode() {
      int $$0 = this.effect.hashCode();
      $$0 = 31 * $$0 + this.duration;
      $$0 = 31 * $$0 + this.amplifier;
      $$0 = 31 * $$0 + (this.ambient ? 1 : 0);
      $$0 = 31 * $$0 + (this.visible ? 1 : 0);
      return 31 * $$0 + (this.showIcon ? 1 : 0);
   }

   public int compareTo(net.minecraft.world.effect.MobEffectInstance $$0) {
      int $$1 = 32147;
      return (this.getDuration() <= 32147 || $$0.getDuration() <= 32147) && (!this.isAmbient() || !$$0.isAmbient())
         ? ComparisonChain.start()
            .compareFalseFirst(this.isAmbient(), $$0.isAmbient())
            .compareFalseFirst(this.isInfiniteDuration(), $$0.isInfiniteDuration())
            .compare(this.getDuration(), $$0.getDuration())
            .compare(
               ((net.minecraft.world.effect.MobEffect)this.getEffect().value()).getColor(),
               ((net.minecraft.world.effect.MobEffect)$$0.getEffect().value()).getColor()
            )
            .result()
         : ComparisonChain.start()
            .compare(this.isAmbient(), $$0.isAmbient())
            .compare(
               ((net.minecraft.world.effect.MobEffect)this.getEffect().value()).getColor(),
               ((net.minecraft.world.effect.MobEffect)$$0.getEffect().value()).getColor()
            )
            .result();
   }

   public void onEffectAdded(LivingEntity $$0) {
      ((net.minecraft.world.effect.MobEffect)this.effect.value()).onEffectAdded($$0, this.amplifier);
   }

   public boolean is(Holder<net.minecraft.world.effect.MobEffect> $$0) {
      return this.effect.equals($$0);
   }

   public void copyBlendState(net.minecraft.world.effect.MobEffectInstance $$0) {
      this.blendState.copyFrom($$0.blendState);
   }

   public void skipBlending() {
      this.blendState.setImmediate(this);
   }

   static class BlendState {
      private float factor;
      private float factorPreviousFrame;

      public void setImmediate(net.minecraft.world.effect.MobEffectInstance $$0) {
         this.factor = hasEffect($$0) ? 1.0F : 0.0F;
         this.factorPreviousFrame = this.factor;
      }

      public void copyFrom(net.minecraft.world.effect.MobEffectInstance.BlendState $$0) {
         this.factor = $$0.factor;
         this.factorPreviousFrame = $$0.factorPreviousFrame;
      }

      public void tick(net.minecraft.world.effect.MobEffectInstance $$0) {
         this.factorPreviousFrame = this.factor;
         boolean $$1 = hasEffect($$0);
         float $$2 = $$1 ? 1.0F : 0.0F;
         if (this.factor != $$2) {
            net.minecraft.world.effect.MobEffect $$3 = (net.minecraft.world.effect.MobEffect)$$0.getEffect().value();
            int $$4 = $$1 ? $$3.getBlendInDurationTicks() : $$3.getBlendOutDurationTicks();
            if ($$4 == 0) {
               this.factor = $$2;
            } else {
               float $$5 = 1.0F / $$4;
               this.factor = this.factor + Mth.clamp($$2 - this.factor, -$$5, $$5);
            }
         }
      }

      private static boolean hasEffect(net.minecraft.world.effect.MobEffectInstance $$0) {
         return !$$0.endsWithin(((net.minecraft.world.effect.MobEffect)$$0.getEffect().value()).getBlendOutAdvanceTicks());
      }

      public float getFactor(LivingEntity $$0, float $$1) {
         if ($$0.isRemoved()) {
            this.factorPreviousFrame = this.factor;
         }

         return Mth.lerp($$1, this.factorPreviousFrame, this.factor);
      }
   }

   record Details(
      int amplifier,
      int duration,
      boolean ambient,
      boolean showParticles,
      boolean showIcon,
      Optional<net.minecraft.world.effect.MobEffectInstance.Details> hiddenEffect
   ) {
      public static final MapCodec<net.minecraft.world.effect.MobEffectInstance.Details> MAP_CODEC = MapCodec.recursive(
         "MobEffectInstance.Details",
         $$0 -> RecordCodecBuilder.mapCodec(
            $$1 -> $$1.group(
                  ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", 0).forGetter(net.minecraft.world.effect.MobEffectInstance.Details::amplifier),
                  Codec.INT.optionalFieldOf("duration", 0).forGetter(net.minecraft.world.effect.MobEffectInstance.Details::duration),
                  Codec.BOOL.optionalFieldOf("ambient", false).forGetter(net.minecraft.world.effect.MobEffectInstance.Details::ambient),
                  Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(net.minecraft.world.effect.MobEffectInstance.Details::showParticles),
                  Codec.BOOL.optionalFieldOf("show_icon").forGetter($$0xx -> Optional.of($$0xx.showIcon())),
                  $$0.optionalFieldOf("hidden_effect").forGetter(net.minecraft.world.effect.MobEffectInstance.Details::hiddenEffect)
               )
               .apply($$1, net.minecraft.world.effect.MobEffectInstance.Details::create)
         )
      );
      public static final StreamCodec<ByteBuf, net.minecraft.world.effect.MobEffectInstance.Details> STREAM_CODEC = StreamCodec.recursive(
         $$0 -> StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            net.minecraft.world.effect.MobEffectInstance.Details::amplifier,
            ByteBufCodecs.VAR_INT,
            net.minecraft.world.effect.MobEffectInstance.Details::duration,
            ByteBufCodecs.BOOL,
            net.minecraft.world.effect.MobEffectInstance.Details::ambient,
            ByteBufCodecs.BOOL,
            net.minecraft.world.effect.MobEffectInstance.Details::showParticles,
            ByteBufCodecs.BOOL,
            net.minecraft.world.effect.MobEffectInstance.Details::showIcon,
            $$0.apply(ByteBufCodecs::optional),
            net.minecraft.world.effect.MobEffectInstance.Details::hiddenEffect,
            net.minecraft.world.effect.MobEffectInstance.Details::new
         )
      );

      private static net.minecraft.world.effect.MobEffectInstance.Details create(
         int $$0, int $$1, boolean $$2, boolean $$3, Optional<Boolean> $$4, Optional<net.minecraft.world.effect.MobEffectInstance.Details> $$5
      ) {
         return new net.minecraft.world.effect.MobEffectInstance.Details($$0, $$1, $$2, $$3, $$4.orElse($$3), $$5);
      }
   }
}
