package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public record KineticWeapon(
   int contactCooldownTicks,
   int delayTicks,
   Optional<KineticWeapon.Condition> dismountConditions,
   Optional<KineticWeapon.Condition> knockbackConditions,
   Optional<KineticWeapon.Condition> damageConditions,
   float forwardMovement,
   float damageMultiplier,
   Optional<Holder<SoundEvent>> sound,
   Optional<Holder<SoundEvent>> hitSound
) {
   public static final int HIT_FEEDBACK_TICKS = 10;
   public static final Codec<KineticWeapon> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("contact_cooldown_ticks", 10).forGetter(KineticWeapon::contactCooldownTicks),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("delay_ticks", 0).forGetter(KineticWeapon::delayTicks),
            KineticWeapon.Condition.CODEC.optionalFieldOf("dismount_conditions").forGetter(KineticWeapon::dismountConditions),
            KineticWeapon.Condition.CODEC.optionalFieldOf("knockback_conditions").forGetter(KineticWeapon::knockbackConditions),
            KineticWeapon.Condition.CODEC.optionalFieldOf("damage_conditions").forGetter(KineticWeapon::damageConditions),
            Codec.FLOAT.optionalFieldOf("forward_movement", 0.0F).forGetter(KineticWeapon::forwardMovement),
            Codec.FLOAT.optionalFieldOf("damage_multiplier", 1.0F).forGetter(KineticWeapon::damageMultiplier),
            SoundEvent.CODEC.optionalFieldOf("sound").forGetter(KineticWeapon::sound),
            SoundEvent.CODEC.optionalFieldOf("hit_sound").forGetter(KineticWeapon::hitSound)
         )
         .apply($$0, KineticWeapon::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, KineticWeapon> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      KineticWeapon::contactCooldownTicks,
      ByteBufCodecs.VAR_INT,
      KineticWeapon::delayTicks,
      KineticWeapon.Condition.STREAM_CODEC.apply(ByteBufCodecs::optional),
      KineticWeapon::dismountConditions,
      KineticWeapon.Condition.STREAM_CODEC.apply(ByteBufCodecs::optional),
      KineticWeapon::knockbackConditions,
      KineticWeapon.Condition.STREAM_CODEC.apply(ByteBufCodecs::optional),
      KineticWeapon::damageConditions,
      ByteBufCodecs.FLOAT,
      KineticWeapon::forwardMovement,
      ByteBufCodecs.FLOAT,
      KineticWeapon::damageMultiplier,
      SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional),
      KineticWeapon::sound,
      SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional),
      KineticWeapon::hitSound,
      KineticWeapon::new
   );

   public static Vec3 getMotion(Entity $$0) {
      if (!($$0 instanceof Player) && $$0.isPassenger()) {
         $$0 = $$0.getRootVehicle();
      }

      return $$0.getKnownSpeed().scale(20.0);
   }

   public void makeSound(Entity $$0) {
      this.sound.ifPresent($$1 -> $$0.level().playSound($$0, $$0.getX(), $$0.getY(), $$0.getZ(), $$1, $$0.getSoundSource(), 1.0F, 1.0F));
   }

   public void makeLocalHitSound(Entity $$0) {
      this.hitSound.ifPresent($$1 -> $$0.level().playLocalSound($$0, (SoundEvent)$$1.value(), $$0.getSoundSource(), 1.0F, 1.0F));
   }

   public int computeDamageUseDuration() {
      return this.delayTicks + this.damageConditions.map(KineticWeapon.Condition::maxDurationTicks).orElse(0);
   }

   public void damageEntities(net.minecraft.world.item.ItemStack $$0, int $$1, LivingEntity $$2, EquipmentSlot $$3) {
      int $$4 = $$0.getUseDuration($$2) - $$1;
      if ($$4 >= this.delayTicks) {
         $$4 -= this.delayTicks;
         Vec3 $$5 = $$2.getLookAngle();
         double $$6 = $$5.dot(getMotion($$2));
         float $$7 = $$2 instanceof Player ? 1.0F : 0.2F;
         AttackRange $$8 = $$2.entityAttackRange();
         double $$9 = $$2.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
         boolean $$10 = false;

         for (EntityHitResult $$11 : (Collection)ProjectileUtil.getHitEntitiesAlong($$2, $$8, $$1x -> PiercingWeapon.canHitEntity($$2, $$1x), Block.COLLIDER)
            .map($$0x -> List.of(), $$0x -> $$0x)) {
            Entity $$12 = $$11.getEntity();
            if ($$12 instanceof EnderDragonPart $$13) {
               $$12 = $$13.parentMob;
            }

            boolean $$14 = $$2.wasRecentlyStabbed($$12, this.contactCooldownTicks);
            if (!$$14) {
               $$2.rememberStabbedEntity($$12);
               double $$15 = $$5.dot(getMotion($$12));
               double $$16 = Math.max(0.0, $$6 - $$15);
               boolean $$17 = this.dismountConditions.isPresent() && this.dismountConditions.get().test($$4, $$6, $$16, $$7);
               boolean $$18 = this.knockbackConditions.isPresent() && this.knockbackConditions.get().test($$4, $$6, $$16, $$7);
               boolean $$19 = this.damageConditions.isPresent() && this.damageConditions.get().test($$4, $$6, $$16, $$7);
               if ($$17 || $$18 || $$19) {
                  float $$20 = (float)$$9 + Mth.floor($$16 * this.damageMultiplier);
                  $$10 |= $$2.stabAttack($$3, $$12, $$20, $$19, $$18, $$17);
               }
            }
         }

         if ($$10) {
            $$2.level().broadcastEntityEvent($$2, (byte)2);
            if ($$2 instanceof ServerPlayer $$21) {
               CriteriaTriggers.SPEAR_MOBS_TRIGGER.trigger($$21, $$2.stabbedEntities($$0x -> $$0x instanceof LivingEntity));
            }
         }
      }
   }

   public record Condition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
      public static final Codec<KineticWeapon.Condition> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_duration_ticks").forGetter(KineticWeapon.Condition::maxDurationTicks),
               Codec.FLOAT.optionalFieldOf("min_speed", 0.0F).forGetter(KineticWeapon.Condition::minSpeed),
               Codec.FLOAT.optionalFieldOf("min_relative_speed", 0.0F).forGetter(KineticWeapon.Condition::minRelativeSpeed)
            )
            .apply($$0, KineticWeapon.Condition::new)
      );
      public static final StreamCodec<ByteBuf, KineticWeapon.Condition> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT,
         KineticWeapon.Condition::maxDurationTicks,
         ByteBufCodecs.FLOAT,
         KineticWeapon.Condition::minSpeed,
         ByteBufCodecs.FLOAT,
         KineticWeapon.Condition::minRelativeSpeed,
         KineticWeapon.Condition::new
      );

      public boolean test(int $$0, double $$1, double $$2, double $$3) {
         return $$0 <= this.maxDurationTicks && $$1 >= this.minSpeed * $$3 && $$2 >= this.minRelativeSpeed * $$3;
      }

      public static Optional<KineticWeapon.Condition> ofAttackerSpeed(int $$0, float $$1) {
         return Optional.of(new KineticWeapon.Condition($$0, $$1, 0.0F));
      }

      public static Optional<KineticWeapon.Condition> ofRelativeSpeed(int $$0, float $$1) {
         return Optional.of(new KineticWeapon.Condition($$0, 0.0F, $$1));
      }
   }
}
