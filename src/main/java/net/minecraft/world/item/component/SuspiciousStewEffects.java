package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public record SuspiciousStewEffects(List<SuspiciousStewEffects.Entry> effects) implements ConsumableListener, TooltipProvider {
   public static final SuspiciousStewEffects EMPTY = new SuspiciousStewEffects(List.of());
   public static final int DEFAULT_DURATION = 160;
   public static final Codec<SuspiciousStewEffects> CODEC = SuspiciousStewEffects.Entry.CODEC
      .listOf()
      .xmap(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);
   public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects> STREAM_CODEC = SuspiciousStewEffects.Entry.STREAM_CODEC
      .apply(ByteBufCodecs.list())
      .map(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);

   public SuspiciousStewEffects withEffectAdded(SuspiciousStewEffects.Entry $$0) {
      return new SuspiciousStewEffects(Util.copyAndAdd(this.effects, $$0));
   }

   @Override
   public void onConsume(Level $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2, Consumable $$3) {
      for (SuspiciousStewEffects.Entry $$4 : this.effects) {
         $$1.addEffect($$4.createEffectInstance());
      }
   }

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      if ($$2.isCreative()) {
         List<MobEffectInstance> $$4 = new ArrayList<>();

         for (SuspiciousStewEffects.Entry $$5 : this.effects) {
            $$4.add($$5.createEffectInstance());
         }

         PotionContents.addPotionTooltip($$4, $$1, 1.0F, $$0.tickRate());
      }
   }

   public record Entry(Holder<MobEffect> effect, int duration) {
      public static final Codec<SuspiciousStewEffects.Entry> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               MobEffect.CODEC.fieldOf("id").forGetter(SuspiciousStewEffects.Entry::effect),
               Codec.INT.lenientOptionalFieldOf("duration", 160).forGetter(SuspiciousStewEffects.Entry::duration)
            )
            .apply($$0, SuspiciousStewEffects.Entry::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects.Entry> STREAM_CODEC = StreamCodec.composite(
         MobEffect.STREAM_CODEC,
         SuspiciousStewEffects.Entry::effect,
         ByteBufCodecs.VAR_INT,
         SuspiciousStewEffects.Entry::duration,
         SuspiciousStewEffects.Entry::new
      );

      public MobEffectInstance createEffectInstance() {
         return new MobEffectInstance(this.effect, this.duration);
      }
   }
}
