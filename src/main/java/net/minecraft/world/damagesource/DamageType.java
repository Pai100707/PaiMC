package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;

public record DamageType(
   String msgId,
   net.minecraft.world.damagesource.DamageScaling scaling,
   float exhaustion,
   net.minecraft.world.damagesource.DamageEffects effects,
   net.minecraft.world.damagesource.DeathMessageType deathMessageType
) {
   public static final Codec<net.minecraft.world.damagesource.DamageType> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.STRING.fieldOf("message_id").forGetter(net.minecraft.world.damagesource.DamageType::msgId),
            net.minecraft.world.damagesource.DamageScaling.CODEC.fieldOf("scaling").forGetter(net.minecraft.world.damagesource.DamageType::scaling),
            Codec.FLOAT.fieldOf("exhaustion").forGetter(net.minecraft.world.damagesource.DamageType::exhaustion),
            net.minecraft.world.damagesource.DamageEffects.CODEC
               .optionalFieldOf("effects", net.minecraft.world.damagesource.DamageEffects.HURT)
               .forGetter(net.minecraft.world.damagesource.DamageType::effects),
            net.minecraft.world.damagesource.DeathMessageType.CODEC
               .optionalFieldOf("death_message_type", net.minecraft.world.damagesource.DeathMessageType.DEFAULT)
               .forGetter(net.minecraft.world.damagesource.DamageType::deathMessageType)
         )
         .apply($$0, net.minecraft.world.damagesource.DamageType::new)
   );
   public static final Codec<Holder<net.minecraft.world.damagesource.DamageType>> CODEC = RegistryFixedCodec.create(Registries.DAMAGE_TYPE);
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<net.minecraft.world.damagesource.DamageType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(
      Registries.DAMAGE_TYPE
   );

   public DamageType(String $$0, net.minecraft.world.damagesource.DamageScaling $$1, float $$2) {
      this($$0, $$1, $$2, net.minecraft.world.damagesource.DamageEffects.HURT, net.minecraft.world.damagesource.DeathMessageType.DEFAULT);
   }

   public DamageType(String $$0, net.minecraft.world.damagesource.DamageScaling $$1, float $$2, net.minecraft.world.damagesource.DamageEffects $$3) {
      this($$0, $$1, $$2, $$3, net.minecraft.world.damagesource.DeathMessageType.DEFAULT);
   }

   public DamageType(String $$0, float $$1, net.minecraft.world.damagesource.DamageEffects $$2) {
      this($$0, net.minecraft.world.damagesource.DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, $$1, $$2);
   }

   public DamageType(String $$0, float $$1) {
      this($$0, net.minecraft.world.damagesource.DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, $$1);
   }
}
