package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.phys.EntityHitResult;

public record PiercingWeapon(boolean dealsKnockback, boolean dismounts, Optional<Holder<SoundEvent>> sound, Optional<Holder<SoundEvent>> hitSound) {
   public static final Codec<PiercingWeapon> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.BOOL.optionalFieldOf("deals_knockback", true).forGetter(PiercingWeapon::dealsKnockback),
            Codec.BOOL.optionalFieldOf("dismounts", false).forGetter(PiercingWeapon::dismounts),
            SoundEvent.CODEC.optionalFieldOf("sound").forGetter(PiercingWeapon::sound),
            SoundEvent.CODEC.optionalFieldOf("hit_sound").forGetter(PiercingWeapon::hitSound)
         )
         .apply($$0, PiercingWeapon::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, PiercingWeapon> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      PiercingWeapon::dealsKnockback,
      ByteBufCodecs.BOOL,
      PiercingWeapon::dismounts,
      SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional),
      PiercingWeapon::sound,
      SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional),
      PiercingWeapon::hitSound,
      PiercingWeapon::new
   );

   public void makeSound(Entity $$0) {
      this.sound.ifPresent($$1 -> $$0.level().playSound($$0, $$0.getX(), $$0.getY(), $$0.getZ(), $$1, $$0.getSoundSource(), 1.0F, 1.0F));
   }

   public void makeHitSound(Entity $$0) {
      this.hitSound.ifPresent($$1 -> $$0.level().playSound(null, $$0.getX(), $$0.getY(), $$0.getZ(), $$1, $$0.getSoundSource(), 1.0F, 1.0F));
   }

   public static boolean canHitEntity(Entity $$0, Entity $$1) {
      if ($$1.isInvulnerable() || !$$1.isAlive()) {
         return false;
      } else if ($$1 instanceof Interaction) {
         return true;
      } else if (!$$1.canBeHitByProjectile()) {
         return false;
      } else {
         return $$1 instanceof Player $$2 && $$0 instanceof Player $$3 && !$$3.canHarmPlayer($$2) ? false : !$$0.isPassengerOfSameVehicle($$1);
      }
   }

   public void attack(LivingEntity $$0, EquipmentSlot $$1) {
      float $$2 = (float)$$0.getAttributeValue(Attributes.ATTACK_DAMAGE);
      AttackRange $$3 = $$0.entityAttackRange();
      boolean $$4 = false;

      for (EntityHitResult $$5 : (Collection)ProjectileUtil.getHitEntitiesAlong($$0, $$3, $$1x -> canHitEntity($$0, $$1x), Block.COLLIDER)
         .map($$0x -> List.of(), $$0x -> $$0x)) {
         $$4 |= $$0.stabAttack($$1, $$5.getEntity(), $$2, true, this.dealsKnockback, this.dismounts);
      }

      $$0.onAttack();
      $$0.lungeForwardMaybe();
      if ($$4) {
         this.makeHitSound($$0);
      }

      this.makeSound($$0);
      $$0.swing(InteractionHand.MAIN_HAND, false);
   }
}
