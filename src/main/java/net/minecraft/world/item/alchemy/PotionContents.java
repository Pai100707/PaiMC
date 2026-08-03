package net.minecraft.world.item.alchemy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;

public record PotionContents(Optional<Holder<Potion>> potion, Optional<Integer> customColor, List<MobEffectInstance> customEffects, Optional<String> customName)
   implements ConsumableListener,
   TooltipProvider {
   public static final PotionContents EMPTY = new PotionContents(Optional.empty(), Optional.empty(), List.of(), Optional.empty());
   private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);
   public static final int BASE_POTION_COLOR = -13083194;
   private static final Codec<PotionContents> FULL_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Potion.CODEC.optionalFieldOf("potion").forGetter(PotionContents::potion),
            Codec.INT.optionalFieldOf("custom_color").forGetter(PotionContents::customColor),
            MobEffectInstance.CODEC.listOf().optionalFieldOf("custom_effects", List.of()).forGetter(PotionContents::customEffects),
            Codec.STRING.optionalFieldOf("custom_name").forGetter(PotionContents::customName)
         )
         .apply($$0, PotionContents::new)
   );
   public static final Codec<PotionContents> CODEC = Codec.withAlternative(FULL_CODEC, Potion.CODEC, PotionContents::new);
   public static final StreamCodec<RegistryFriendlyByteBuf, PotionContents> STREAM_CODEC = StreamCodec.composite(
      Potion.STREAM_CODEC.apply(ByteBufCodecs::optional),
      PotionContents::potion,
      ByteBufCodecs.INT.apply(ByteBufCodecs::optional),
      PotionContents::customColor,
      MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()),
      PotionContents::customEffects,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional),
      PotionContents::customName,
      PotionContents::new
   );

   public PotionContents(Holder<Potion> $$0) {
      this(Optional.of($$0), Optional.empty(), List.of(), Optional.empty());
   }

   public static net.minecraft.world.item.ItemStack createItemStack(net.minecraft.world.item.Item $$0, Holder<Potion> $$1) {
      net.minecraft.world.item.ItemStack $$2 = new net.minecraft.world.item.ItemStack($$0);
      $$2.set(DataComponents.POTION_CONTENTS, new PotionContents($$1));
      return $$2;
   }

   public boolean is(Holder<Potion> $$0) {
      return this.potion.isPresent() && this.potion.get().is($$0) && this.customEffects.isEmpty();
   }

   public Iterable<MobEffectInstance> getAllEffects() {
      if (this.potion.isEmpty()) {
         return this.customEffects;
      } else {
         return (Iterable<MobEffectInstance>)(this.customEffects.isEmpty()
            ? ((Potion)this.potion.get().value()).getEffects()
            : Iterables.concat(((Potion)this.potion.get().value()).getEffects(), this.customEffects));
      }
   }

   public void forEachEffect(Consumer<MobEffectInstance> $$0, float $$1) {
      if (this.potion.isPresent()) {
         for (MobEffectInstance $$2 : ((Potion)this.potion.get().value()).getEffects()) {
            $$0.accept($$2.withScaledDuration($$1));
         }
      }

      for (MobEffectInstance $$3 : this.customEffects) {
         $$0.accept($$3.withScaledDuration($$1));
      }
   }

   public PotionContents withPotion(Holder<Potion> $$0) {
      return new PotionContents(Optional.of($$0), this.customColor, this.customEffects, this.customName);
   }

   public PotionContents withEffectAdded(MobEffectInstance $$0) {
      return new PotionContents(this.potion, this.customColor, Util.copyAndAdd(this.customEffects, $$0), this.customName);
   }

   public int getColor() {
      return this.getColorOr(-13083194);
   }

   public int getColorOr(int $$0) {
      return this.customColor.isPresent() ? this.customColor.get() : getColorOptional(this.getAllEffects()).orElse($$0);
   }

   public Component getName(String $$0) {
      String $$1 = this.customName.or(() -> this.potion.map($$0x -> ((Potion)$$0x.value()).name())).orElse("empty");
      return Component.translatable($$0 + $$1);
   }

   public static OptionalInt getColorOptional(Iterable<MobEffectInstance> $$0) {
      int $$1 = 0;
      int $$2 = 0;
      int $$3 = 0;
      int $$4 = 0;

      for (MobEffectInstance $$5 : $$0) {
         if ($$5.isVisible()) {
            int $$6 = ((MobEffect)$$5.getEffect().value()).getColor();
            int $$7 = $$5.getAmplifier() + 1;
            $$1 += $$7 * ARGB.red($$6);
            $$2 += $$7 * ARGB.green($$6);
            $$3 += $$7 * ARGB.blue($$6);
            $$4 += $$7;
         }
      }

      return $$4 == 0 ? OptionalInt.empty() : OptionalInt.of(ARGB.color($$1 / $$4, $$2 / $$4, $$3 / $$4));
   }

   public boolean hasEffects() {
      return !this.customEffects.isEmpty() ? true : this.potion.isPresent() && !((Potion)this.potion.get().value()).getEffects().isEmpty();
   }

   public List<MobEffectInstance> customEffects() {
      return Lists.transform(this.customEffects, MobEffectInstance::new);
   }

   public void applyToLivingEntity(LivingEntity $$0, float $$1) {
      if ($$0.level() instanceof ServerLevel $$2) {
         Player $$5 = $$0 instanceof Player $$4 ? $$4 : null;
         this.forEachEffect($$3x -> {
            if (((MobEffect)$$3x.getEffect().value()).isInstantenous()) {
               ((MobEffect)$$3x.getEffect().value()).applyInstantenousEffect($$2, $$5, $$5, $$0, $$3x.getAmplifier(), 1.0);
            } else {
               $$0.addEffect($$3x);
            }
         }, $$1);
      }
   }

   public static void addPotionTooltip(Iterable<MobEffectInstance> $$0, Consumer<Component> $$1, float $$2, float $$3) {
      List<Pair<Holder<Attribute>, AttributeModifier>> $$4 = Lists.newArrayList();
      boolean $$5 = true;

      for (MobEffectInstance $$6 : $$0) {
         $$5 = false;
         Holder<MobEffect> $$7 = $$6.getEffect();
         int $$8 = $$6.getAmplifier();
         ((MobEffect)$$7.value()).createModifiers($$8, ($$1x, $$2x) -> $$4.add(new Pair($$1x, $$2x)));
         MutableComponent $$9 = getPotionDescription($$7, $$8);
         if (!$$6.endsWithin(20)) {
            $$9 = Component.translatable("potion.withDuration", new Object[]{$$9, MobEffectUtil.formatDuration($$6, $$2, $$3)});
         }

         $$1.accept($$9.withStyle(((MobEffect)$$7.value()).getCategory().getTooltipFormatting()));
      }

      if ($$5) {
         $$1.accept(NO_EFFECT);
      }

      if (!$$4.isEmpty()) {
         $$1.accept(CommonComponents.EMPTY);
         $$1.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

         for (Pair<Holder<Attribute>, AttributeModifier> $$10 : $$4) {
            AttributeModifier $$11 = (AttributeModifier)$$10.getSecond();
            double $$12 = $$11.amount();
            double $$14;
            if ($$11.operation() != Operation.ADD_MULTIPLIED_BASE && $$11.operation() != Operation.ADD_MULTIPLIED_TOTAL) {
               $$14 = $$11.amount();
            } else {
               $$14 = $$11.amount() * 100.0;
            }

            if ($$12 > 0.0) {
               $$1.accept(
                  Component.translatable(
                        "attribute.modifier.plus." + $$11.operation().id(),
                        new Object[]{
                           ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format($$14),
                           Component.translatable(((Attribute)((Holder)$$10.getFirst()).value()).getDescriptionId())
                        }
                     )
                     .withStyle(ChatFormatting.BLUE)
               );
            } else if ($$12 < 0.0) {
               $$14 *= -1.0;
               $$1.accept(
                  Component.translatable(
                        "attribute.modifier.take." + $$11.operation().id(),
                        new Object[]{
                           ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format($$14),
                           Component.translatable(((Attribute)((Holder)$$10.getFirst()).value()).getDescriptionId())
                        }
                     )
                     .withStyle(ChatFormatting.RED)
               );
            }
         }
      }
   }

   public static MutableComponent getPotionDescription(Holder<MobEffect> $$0, int $$1) {
      MutableComponent $$2 = Component.translatable(((MobEffect)$$0.value()).getDescriptionId());
      return $$1 > 0 ? Component.translatable("potion.withAmplifier", new Object[]{$$2, Component.translatable("potion.potency." + $$1)}) : $$2;
   }

   @Override
   public void onConsume(Level $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2, Consumable $$3) {
      this.applyToLivingEntity($$1, (Float)$$2.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F));
   }

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      addPotionTooltip(this.getAllEffects(), $$1, (Float)$$3.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F), $$0.tickRate());
   }
}
