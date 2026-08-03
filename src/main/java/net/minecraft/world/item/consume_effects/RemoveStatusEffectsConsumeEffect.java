package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record RemoveStatusEffectsConsumeEffect(HolderSet<MobEffect> effects) implements ConsumeEffect {
   public static final MapCodec<RemoveStatusEffectsConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(RemoveStatusEffectsConsumeEffect::effects))
         .apply($$0, RemoveStatusEffectsConsumeEffect::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, RemoveStatusEffectsConsumeEffect> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.holderSet(Registries.MOB_EFFECT), RemoveStatusEffectsConsumeEffect::effects, RemoveStatusEffectsConsumeEffect::new
   );

   public RemoveStatusEffectsConsumeEffect(Holder<MobEffect> $$0) {
      this(HolderSet.direct(new Holder[]{$$0}));
   }

   @Override
   public ConsumeEffect.Type<RemoveStatusEffectsConsumeEffect> getType() {
      return ConsumeEffect.Type.REMOVE_EFFECTS;
   }

   @Override
   public boolean apply(Level $$0, net.minecraft.world.item.ItemStack $$1, LivingEntity $$2) {
      boolean $$3 = false;

      for (Holder<MobEffect> $$4 : this.effects) {
         if ($$2.removeEffect($$4)) {
            $$3 = true;
         }
      }

      return $$3;
   }
}
