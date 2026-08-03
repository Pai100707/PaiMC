package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record ApplyStatusEffectsConsumeEffect(List<MobEffectInstance> effects, float probability) implements ConsumeEffect {
   public static final MapCodec<ApplyStatusEffectsConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(ApplyStatusEffectsConsumeEffect::effects),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("probability", 1.0F).forGetter(ApplyStatusEffectsConsumeEffect::probability)
         )
         .apply($$0, ApplyStatusEffectsConsumeEffect::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ApplyStatusEffectsConsumeEffect> STREAM_CODEC = StreamCodec.composite(
      MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()),
      ApplyStatusEffectsConsumeEffect::effects,
      ByteBufCodecs.FLOAT,
      ApplyStatusEffectsConsumeEffect::probability,
      ApplyStatusEffectsConsumeEffect::new
   );

   public ApplyStatusEffectsConsumeEffect(MobEffectInstance $$0, float $$1) {
      this(List.of($$0), $$1);
   }

   public ApplyStatusEffectsConsumeEffect(List<MobEffectInstance> $$0) {
      this($$0, 1.0F);
   }

   public ApplyStatusEffectsConsumeEffect(MobEffectInstance $$0) {
      this($$0, 1.0F);
   }

   @Override
   public ConsumeEffect.Type<ApplyStatusEffectsConsumeEffect> getType() {
      return ConsumeEffect.Type.APPLY_EFFECTS;
   }

   @Override
   public boolean apply(Level $$0, net.minecraft.world.item.ItemStack $$1, LivingEntity $$2) {
      if ($$2.getRandom().nextFloat() >= this.probability) {
         return false;
      } else {
         boolean $$3 = false;

         for (MobEffectInstance $$4 : this.effects) {
            if ($$2.addEffect(new MobEffectInstance($$4))) {
               $$3 = true;
            }
         }

         return $$3;
      }
   }
}
