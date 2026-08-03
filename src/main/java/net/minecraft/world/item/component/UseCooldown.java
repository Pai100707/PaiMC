package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record UseCooldown(float seconds, Optional<Identifier> cooldownGroup) {
   public static final Codec<UseCooldown> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("seconds").forGetter(UseCooldown::seconds),
            Identifier.CODEC.optionalFieldOf("cooldown_group").forGetter(UseCooldown::cooldownGroup)
         )
         .apply($$0, UseCooldown::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, UseCooldown> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT, UseCooldown::seconds, Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), UseCooldown::cooldownGroup, UseCooldown::new
   );

   public UseCooldown(float $$0) {
      this($$0, Optional.empty());
   }

   public int ticks() {
      return (int)(this.seconds * 20.0F);
   }

   public void apply(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1) {
      if ($$1 instanceof Player $$2) {
         $$2.getCooldowns().addCooldown($$0, this.ticks());
      }
   }
}
