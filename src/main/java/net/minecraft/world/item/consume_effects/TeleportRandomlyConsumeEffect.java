package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.Vec3;

public record TeleportRandomlyConsumeEffect(float diameter) implements ConsumeEffect {
   private static final float DEFAULT_DIAMETER = 16.0F;
   public static final MapCodec<TeleportRandomlyConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("diameter", 16.0F).forGetter(TeleportRandomlyConsumeEffect::diameter))
         .apply($$0, TeleportRandomlyConsumeEffect::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, TeleportRandomlyConsumeEffect> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT, TeleportRandomlyConsumeEffect::diameter, TeleportRandomlyConsumeEffect::new
   );

   public TeleportRandomlyConsumeEffect() {
      this(16.0F);
   }

   @Override
   public ConsumeEffect.Type<TeleportRandomlyConsumeEffect> getType() {
      return ConsumeEffect.Type.TELEPORT_RANDOMLY;
   }

   @Override
   public boolean apply(Level $$0, net.minecraft.world.item.ItemStack $$1, LivingEntity $$2) {
      boolean $$3 = false;

      for (int $$4 = 0; $$4 < 16; $$4++) {
         double $$5 = $$2.getX() + ($$2.getRandom().nextDouble() - 0.5) * this.diameter;
         double $$6 = Mth.clamp(
            $$2.getY() + ($$2.getRandom().nextDouble() - 0.5) * this.diameter, $$0.getMinY(), $$0.getMinY() + ((ServerLevel)$$0).getLogicalHeight() - 1
         );
         double $$7 = $$2.getZ() + ($$2.getRandom().nextDouble() - 0.5) * this.diameter;
         if ($$2.isPassenger()) {
            $$2.stopRiding();
         }

         Vec3 $$8 = $$2.position();
         if ($$2.randomTeleport($$5, $$6, $$7, true)) {
            $$0.gameEvent(GameEvent.TELEPORT, $$8, Context.of($$2));
            SoundSource $$10;
            SoundEvent $$9;
            if ($$2 instanceof Fox) {
               $$9 = SoundEvents.FOX_TELEPORT;
               $$10 = SoundSource.NEUTRAL;
            } else {
               $$9 = SoundEvents.CHORUS_FRUIT_TELEPORT;
               $$10 = SoundSource.PLAYERS;
            }

            $$0.playSound(null, $$2.getX(), $$2.getY(), $$2.getZ(), $$9, $$10);
            $$2.resetFallDistance();
            $$3 = true;
            break;
         }
      }

      if ($$3 && $$2 instanceof Player $$13) {
         $$13.resetCurrentImpulseContext();
      }

      return $$3;
   }
}
