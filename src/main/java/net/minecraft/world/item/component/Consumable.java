package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public record Consumable(
   float consumeSeconds,
   net.minecraft.world.item.ItemUseAnimation animation,
   Holder<SoundEvent> sound,
   boolean hasConsumeParticles,
   List<ConsumeEffect> onConsumeEffects
) {
   public static final float DEFAULT_CONSUME_SECONDS = 1.6F;
   private static final int CONSUME_EFFECTS_INTERVAL = 4;
   private static final float CONSUME_EFFECTS_START_FRACTION = 0.21875F;
   public static final Codec<Consumable> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("consume_seconds", 1.6F).forGetter(Consumable::consumeSeconds),
            net.minecraft.world.item.ItemUseAnimation.CODEC
               .optionalFieldOf("animation", net.minecraft.world.item.ItemUseAnimation.EAT)
               .forGetter(Consumable::animation),
            SoundEvent.CODEC.optionalFieldOf("sound", SoundEvents.GENERIC_EAT).forGetter(Consumable::sound),
            Codec.BOOL.optionalFieldOf("has_consume_particles", true).forGetter(Consumable::hasConsumeParticles),
            ConsumeEffect.CODEC.listOf().optionalFieldOf("on_consume_effects", List.of()).forGetter(Consumable::onConsumeEffects)
         )
         .apply($$0, Consumable::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, Consumable> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT,
      Consumable::consumeSeconds,
      net.minecraft.world.item.ItemUseAnimation.STREAM_CODEC,
      Consumable::animation,
      SoundEvent.STREAM_CODEC,
      Consumable::sound,
      ByteBufCodecs.BOOL,
      Consumable::hasConsumeParticles,
      ConsumeEffect.STREAM_CODEC.apply(ByteBufCodecs.list()),
      Consumable::onConsumeEffects,
      Consumable::new
   );

   public InteractionResult startConsuming(LivingEntity $$0, net.minecraft.world.item.ItemStack $$1, InteractionHand $$2) {
      if (!this.canConsume($$0, $$1)) {
         return InteractionResult.FAIL;
      } else {
         boolean $$3 = this.consumeTicks() > 0;
         if ($$3) {
            $$0.startUsingItem($$2);
            return InteractionResult.CONSUME;
         } else {
            net.minecraft.world.item.ItemStack $$4 = this.onConsume($$0.level(), $$0, $$1);
            return InteractionResult.CONSUME.heldItemTransformedTo($$4);
         }
      }
   }

   public net.minecraft.world.item.ItemStack onConsume(Level $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2) {
      RandomSource $$3 = $$1.getRandom();
      this.emitParticlesAndSounds($$3, $$1, $$2, 16);
      if ($$1 instanceof ServerPlayer $$4) {
         $$4.awardStat(Stats.ITEM_USED.get($$2.getItem()));
         CriteriaTriggers.CONSUME_ITEM.trigger($$4, $$2);
      }

      $$2.getAllOfType(ConsumableListener.class).forEach($$3x -> $$3x.onConsume($$0, $$1, $$2, this));
      if (!$$0.isClientSide()) {
         this.onConsumeEffects.forEach($$3x -> $$3x.apply($$0, $$2, $$1));
      }

      $$1.gameEvent(this.animation == net.minecraft.world.item.ItemUseAnimation.DRINK ? GameEvent.DRINK : GameEvent.EAT);
      $$2.consume(1, $$1);
      return $$2;
   }

   public boolean canConsume(LivingEntity $$0, net.minecraft.world.item.ItemStack $$1) {
      FoodProperties $$2 = (FoodProperties)$$1.get(DataComponents.FOOD);
      return $$2 != null && $$0 instanceof Player $$3 ? $$3.canEat($$2.canAlwaysEat()) : true;
   }

   public int consumeTicks() {
      return (int)(this.consumeSeconds * 20.0F);
   }

   public void emitParticlesAndSounds(RandomSource $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2, int $$3) {
      float $$4 = $$0.nextBoolean() ? 0.5F : 1.0F;
      float $$5 = $$0.triangle(1.0F, 0.2F);
      float $$6 = 0.5F;
      float $$7 = Mth.randomBetween($$0, 0.9F, 1.0F);
      float $$8 = this.animation == net.minecraft.world.item.ItemUseAnimation.DRINK ? 0.5F : $$4;
      float $$9 = this.animation == net.minecraft.world.item.ItemUseAnimation.DRINK ? $$7 : $$5;
      if (this.hasConsumeParticles) {
         $$1.spawnItemParticles($$2, $$3);
      }

      SoundEvent $$11 = $$1 instanceof Consumable.OverrideConsumeSound $$10 ? $$10.getConsumeSound($$2) : (SoundEvent)this.sound.value();
      $$1.playSound($$11, $$8, $$9);
   }

   public boolean shouldEmitParticlesAndSounds(int $$0) {
      int $$1 = this.consumeTicks() - $$0;
      int $$2 = (int)(this.consumeTicks() * 0.21875F);
      boolean $$3 = $$1 > $$2;
      return $$3 && $$0 % 4 == 0;
   }

   public static Consumable.Builder builder() {
      return new Consumable.Builder();
   }

   public static class Builder {
      private float consumeSeconds = 1.6F;
      private net.minecraft.world.item.ItemUseAnimation animation = net.minecraft.world.item.ItemUseAnimation.EAT;
      private Holder<SoundEvent> sound = SoundEvents.GENERIC_EAT;
      private boolean hasConsumeParticles = true;
      private final List<ConsumeEffect> onConsumeEffects = new ArrayList<>();

      Builder() {
      }

      public Consumable.Builder consumeSeconds(float $$0) {
         this.consumeSeconds = $$0;
         return this;
      }

      public Consumable.Builder animation(net.minecraft.world.item.ItemUseAnimation $$0) {
         this.animation = $$0;
         return this;
      }

      public Consumable.Builder sound(Holder<SoundEvent> $$0) {
         this.sound = $$0;
         return this;
      }

      public Consumable.Builder soundAfterConsume(Holder<SoundEvent> $$0) {
         return this.onConsume(new PlaySoundConsumeEffect($$0));
      }

      public Consumable.Builder hasConsumeParticles(boolean $$0) {
         this.hasConsumeParticles = $$0;
         return this;
      }

      public Consumable.Builder onConsume(ConsumeEffect $$0) {
         this.onConsumeEffects.add($$0);
         return this;
      }

      public Consumable build() {
         return new Consumable(this.consumeSeconds, this.animation, this.sound, this.hasConsumeParticles, this.onConsumeEffects);
      }
   }

   public interface OverrideConsumeSound {
      SoundEvent getConsumeSound(net.minecraft.world.item.ItemStack var1);
   }
}
