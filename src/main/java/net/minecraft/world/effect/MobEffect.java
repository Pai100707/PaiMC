package net.minecraft.world.effect;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class MobEffect implements FeatureElement {
   public static final Codec<Holder<net.minecraft.world.effect.MobEffect>> CODEC = BuiltInRegistries.MOB_EFFECT.holderByNameCodec();
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<net.minecraft.world.effect.MobEffect>> STREAM_CODEC = ByteBufCodecs.holderRegistry(
      Registries.MOB_EFFECT
   );
   private static final int AMBIENT_ALPHA = Mth.floor(38.25F);
   private final Map<Holder<Attribute>, net.minecraft.world.effect.MobEffect.AttributeTemplate> attributeModifiers = new Object2ObjectOpenHashMap();
   private final net.minecraft.world.effect.MobEffectCategory category;
   private final int color;
   private final Function<net.minecraft.world.effect.MobEffectInstance, ParticleOptions> particleFactory;
   
   private String descriptionId;
   private int blendInDurationTicks;
   private int blendOutDurationTicks;
   private int blendOutAdvanceTicks;
   private Optional<SoundEvent> soundOnAdded = Optional.empty();
   private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

   protected MobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1) {
      this.category = $$0;
      this.color = $$1;
      this.particleFactory = $$1x -> {
         int $$2 = $$1x.isAmbient() ? AMBIENT_ALPHA : 255;
         return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, ARGB.color($$2, $$1));
      };
   }

   protected MobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1, ParticleOptions $$2) {
      this.category = $$0;
      this.color = $$1;
      this.particleFactory = $$1x -> $$2;
   }

   public int getBlendInDurationTicks() {
      return this.blendInDurationTicks;
   }

   public int getBlendOutDurationTicks() {
      return this.blendOutDurationTicks;
   }

   public int getBlendOutAdvanceTicks() {
      return this.blendOutAdvanceTicks;
   }

   public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
      return true;
   }

   public void applyInstantenousEffect(ServerLevel $$0, Entity $$1, Entity $$2, LivingEntity $$3, int $$4, double $$5) {
      this.applyEffectTick($$0, $$3, $$4);
   }

   public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
      return false;
   }

   public void onEffectStarted(LivingEntity $$0, int $$1) {
   }

   public void onEffectAdded(LivingEntity $$0, int $$1) {
      this.soundOnAdded.ifPresent($$1x -> $$0.level().playSound(null, $$0.getX(), $$0.getY(), $$0.getZ(), $$1x, $$0.getSoundSource(), 1.0F, 1.0F));
   }

   public void onMobRemoved(ServerLevel $$0, LivingEntity $$1, int $$2, RemovalReason $$3) {
   }

   public void onMobHurt(ServerLevel $$0, LivingEntity $$1, int $$2, DamageSource $$3, float $$4) {
   }

   public boolean isInstantenous() {
      return false;
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
      }

      return this.descriptionId;
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public Component getDisplayName() {
      return Component.translatable(this.getDescriptionId());
   }

   public net.minecraft.world.effect.MobEffectCategory getCategory() {
      return this.category;
   }

   public int getColor() {
      return this.color;
   }

   public net.minecraft.world.effect.MobEffect addAttributeModifier(Holder<Attribute> $$0, Identifier $$1, double $$2, Operation $$3) {
      this.attributeModifiers.put($$0, new net.minecraft.world.effect.MobEffect.AttributeTemplate($$1, $$2, $$3));
      return this;
   }

   public net.minecraft.world.effect.MobEffect setBlendDuration(int $$0) {
      return this.setBlendDuration($$0, $$0, $$0);
   }

   public net.minecraft.world.effect.MobEffect setBlendDuration(int $$0, int $$1, int $$2) {
      this.blendInDurationTicks = $$0;
      this.blendOutDurationTicks = $$1;
      this.blendOutAdvanceTicks = $$2;
      return this;
   }

   public void createModifiers(int $$0, BiConsumer<Holder<Attribute>, AttributeModifier> $$1) {
      this.attributeModifiers.forEach(($$2, $$3) -> $$1.accept((Holder<Attribute>)$$2, $$3.create($$0)));
   }

   public void removeAttributeModifiers(AttributeMap $$0) {
      for (Entry<Holder<Attribute>, net.minecraft.world.effect.MobEffect.AttributeTemplate> $$1 : this.attributeModifiers.entrySet()) {
         AttributeInstance $$2 = $$0.getInstance($$1.getKey());
         if ($$2 != null) {
            $$2.removeModifier($$1.getValue().id());
         }
      }
   }

   public void addAttributeModifiers(AttributeMap $$0, int $$1) {
      for (Entry<Holder<Attribute>, net.minecraft.world.effect.MobEffect.AttributeTemplate> $$2 : this.attributeModifiers.entrySet()) {
         AttributeInstance $$3 = $$0.getInstance($$2.getKey());
         if ($$3 != null) {
            $$3.removeModifier($$2.getValue().id());
            $$3.addPermanentModifier($$2.getValue().create($$1));
         }
      }
   }

   public boolean isBeneficial() {
      return this.category == net.minecraft.world.effect.MobEffectCategory.BENEFICIAL;
   }

   public ParticleOptions createParticleOptions(net.minecraft.world.effect.MobEffectInstance $$0) {
      return this.particleFactory.apply($$0);
   }

   public net.minecraft.world.effect.MobEffect withSoundOnAdded(SoundEvent $$0) {
      this.soundOnAdded = Optional.of($$0);
      return this;
   }

   public net.minecraft.world.effect.MobEffect requiredFeatures(FeatureFlag... $$0) {
      this.requiredFeatures = FeatureFlags.REGISTRY.subset($$0);
      return this;
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   record AttributeTemplate(Identifier id, double amount, Operation operation) {
      public AttributeModifier create(int $$0) {
         return new AttributeModifier(this.id, this.amount * ($$0 + 1), this.operation);
      }
   }
}
