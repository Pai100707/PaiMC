package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record PlaySoundConsumeEffect(Holder<SoundEvent> sound) implements ConsumeEffect {
   public static final MapCodec<PlaySoundConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(SoundEvent.CODEC.fieldOf("sound").forGetter(PlaySoundConsumeEffect::sound)).apply($$0, PlaySoundConsumeEffect::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, PlaySoundConsumeEffect> STREAM_CODEC = StreamCodec.composite(
      SoundEvent.STREAM_CODEC, PlaySoundConsumeEffect::sound, PlaySoundConsumeEffect::new
   );

   @Override
   public ConsumeEffect.Type<PlaySoundConsumeEffect> getType() {
      return ConsumeEffect.Type.PLAY_SOUND;
   }

   @Override
   public boolean apply(Level $$0, net.minecraft.world.item.ItemStack $$1, LivingEntity $$2) {
      $$0.playSound(null, $$2.blockPosition(), (SoundEvent)this.sound.value(), $$2.getSoundSource(), 1.0F, 1.0F);
      return true;
   }
}
