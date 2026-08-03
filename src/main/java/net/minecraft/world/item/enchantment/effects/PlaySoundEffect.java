package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public record PlaySoundEffect(List<Holder<SoundEvent>> soundEvents, FloatProvider volume, FloatProvider pitch) implements EnchantmentEntityEffect {
   public static final MapCodec<PlaySoundEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            ExtraCodecs.compactListCodec(SoundEvent.CODEC, SoundEvent.CODEC.sizeLimitedListOf(255)).fieldOf("sound").forGetter(PlaySoundEffect::soundEvents),
            FloatProvider.codec(1.0E-5F, 10.0F).fieldOf("volume").forGetter(PlaySoundEffect::volume),
            FloatProvider.codec(1.0E-5F, 2.0F).fieldOf("pitch").forGetter(PlaySoundEffect::pitch)
         )
         .apply($$0, PlaySoundEffect::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      if (!$$3.isSilent()) {
         RandomSource $$5 = $$3.getRandom();
         int $$6 = Mth.clamp($$1 - 1, 0, this.soundEvents.size() - 1);
         $$0.playSound(null, $$4.x(), $$4.y(), $$4.z(), this.soundEvents.get($$6), $$3.getSoundSource(), this.volume.sample($$5), this.pitch.sample($$5));
      }
   }

   @Override
   public MapCodec<PlaySoundEffect> codec() {
      return CODEC;
   }
}
