package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public class SonicBoom extends Behavior<Warden> {
   private static final int DISTANCE_XZ = 15;
   private static final int DISTANCE_Y = 20;
   private static final double KNOCKBACK_VERTICAL = 0.5;
   private static final double KNOCKBACK_HORIZONTAL = 2.5;
   public static final int COOLDOWN = 40;
   private static final int TICKS_BEFORE_PLAYING_SOUND = Mth.ceil(34.0);
   private static final int DURATION = Mth.ceil(60.0F);

   public SonicBoom() {
      super(
         ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.SONIC_BOOM_COOLDOWN,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,
            MemoryStatus.REGISTERED,
            MemoryModuleType.SONIC_BOOM_SOUND_DELAY,
            MemoryStatus.REGISTERED
         ),
         DURATION
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Warden $$1) {
      return $$1.closerThan($$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get(), 15.0, 20.0);
   }

   protected boolean canStillUse(ServerLevel $$0, Warden $$1, long $$2) {
      return true;
   }

   protected void start(ServerLevel $$0, Warden $$1, long $$2) {
      $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, DURATION);
      $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_DELAY, Unit.INSTANCE, TICKS_BEFORE_PLAYING_SOUND);
      $$0.broadcastEntityEvent($$1, (byte)62);
      $$1.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
   }

   protected void tick(ServerLevel $$0, Warden $$1, long $$2) {
      $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent($$1x -> $$1.getLookControl().setLookAt($$1x.position()));
      if (!$$1.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY) && !$$1.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)
         )
       {
         $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, DURATION - TICKS_BEFORE_PLAYING_SOUND);
         $$1.getBrain()
            .getMemory(MemoryModuleType.ATTACK_TARGET)
            .filter($$1::canTargetEntity)
            .filter($$1x -> $$1.closerThan($$1x, 15.0, 20.0))
            .ifPresent($$2x -> {
               Vec3 $$3 = $$1.position().add($$1.getAttachments().get(net.minecraft.world.entity.EntityAttachment.WARDEN_CHEST, 0, $$1.getYRot()));
               Vec3 $$4 = $$2x.getEyePosition().subtract($$3);
               Vec3 $$5 = $$4.normalize();
               int $$6 = Mth.floor($$4.length()) + 7;

               for (int $$7 = 1; $$7 < $$6; $$7++) {
                  Vec3 $$8 = $$3.add($$5.scale($$7));
                  $$0.sendParticles(ParticleTypes.SONIC_BOOM, $$8.x, $$8.y, $$8.z, 1, 0.0, 0.0, 0.0, 0.0);
               }

               $$1.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
               if ($$2x.hurtServer($$0, $$0.damageSources().sonicBoom($$1), 10.0F)) {
                  double $$9 = 0.5 * (1.0 - $$2x.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                  double $$10 = 2.5 * (1.0 - $$2x.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                  $$2x.push($$5.x() * $$10, $$5.y() * $$9, $$5.z() * $$10);
               }
            });
      }
   }

   protected void stop(ServerLevel $$0, Warden $$1, long $$2) {
      setCooldown($$1, 40);
   }

   public static void setCooldown(net.minecraft.world.entity.LivingEntity $$0, int $$1) {
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, $$1);
   }
}
