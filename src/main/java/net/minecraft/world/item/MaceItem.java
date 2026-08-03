package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MaceItem extends net.minecraft.world.item.Item {
   private static final int DEFAULT_ATTACK_DAMAGE = 5;
   private static final float DEFAULT_ATTACK_SPEED = -3.4F;
   public static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5F;
   private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
   public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5F;
   private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7F;

   public MaceItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   public static ItemAttributeModifiers createAttributes() {
      return ItemAttributeModifiers.builder()
         .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
         .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4F, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
         .build();
   }

   public static Tool createToolProperties() {
      return new Tool(List.of(), 1.0F, 2, false);
   }

   @Override
   public void hurtEnemy(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1, LivingEntity $$2) {
      if (canSmashAttack($$2)) {
         ServerLevel $$3 = (ServerLevel)$$2.level();
         $$2.setDeltaMovement($$2.getDeltaMovement().with(Axis.Y, 0.01F));
         if ($$2 instanceof ServerPlayer $$4) {
            $$4.currentImpulseImpactPos = this.calculateImpactPosition($$4);
            $$4.setIgnoreFallDamageFromCurrentImpulse(true);
            $$4.connection.send(new ClientboundSetEntityMotionPacket($$4));
         }

         if ($$1.onGround()) {
            if ($$2 instanceof ServerPlayer $$5) {
               $$5.setSpawnExtraParticlesOnFall(true);
            }

            SoundEvent $$6 = $$2.fallDistance > 5.0 ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
            $$3.playSound(null, $$2.getX(), $$2.getY(), $$2.getZ(), $$6, $$2.getSoundSource(), 1.0F, 1.0F);
         } else {
            $$3.playSound(null, $$2.getX(), $$2.getY(), $$2.getZ(), SoundEvents.MACE_SMASH_AIR, $$2.getSoundSource(), 1.0F, 1.0F);
         }

         knockback($$3, $$2, $$1);
      }
   }

   private Vec3 calculateImpactPosition(ServerPlayer $$0) {
      return $$0.isIgnoringFallDamageFromCurrentImpulse() && $$0.currentImpulseImpactPos != null && $$0.currentImpulseImpactPos.y <= $$0.position().y
         ? $$0.currentImpulseImpactPos
         : $$0.position();
   }

   @Override
   public void postHurtEnemy(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1, LivingEntity $$2) {
      if (canSmashAttack($$2)) {
         $$2.resetFallDistance();
      }
   }

   @Override
   public float getAttackDamageBonus(Entity $$0, float $$1, DamageSource $$2) {
      if ($$2.getDirectEntity() instanceof LivingEntity $$3) {
         if (!canSmashAttack($$3)) {
            return 0.0F;
         } else {
            double $$5 = 3.0;
            double $$6 = 8.0;
            double $$7 = $$3.fallDistance;
            double $$8;
            if ($$7 <= 3.0) {
               $$8 = 4.0 * $$7;
            } else if ($$7 <= 8.0) {
               $$8 = 12.0 + 2.0 * ($$7 - 3.0);
            } else {
               $$8 = 22.0 + $$7 - 8.0;
            }

            return $$3.level() instanceof ServerLevel $$11
               ? (float)($$8 + EnchantmentHelper.modifyFallBasedDamage($$11, $$3.getWeaponItem(), $$0, $$2, 0.0F) * $$7)
               : (float)$$8;
         }
      } else {
         return 0.0F;
      }
   }

   private static void knockback(Level $$0, Entity $$1, Entity $$2) {
      $$0.levelEvent(2013, $$2.getOnPos(), 750);
      $$0.getEntitiesOfClass(LivingEntity.class, $$2.getBoundingBox().inflate(3.5), knockbackPredicate($$1, $$2)).forEach($$2x -> {
         Vec3 $$3 = $$2x.position().subtract($$2.position());
         double $$4 = getKnockbackPower($$1, $$2x, $$3);
         Vec3 $$5 = $$3.normalize().scale($$4);
         if ($$4 > 0.0) {
            $$2x.push($$5.x, 0.7F, $$5.z);
            if ($$2x instanceof ServerPlayer $$6) {
               $$6.connection.send(new ClientboundSetEntityMotionPacket($$6));
            }
         }
      });
   }

   private static Predicate<LivingEntity> knockbackPredicate(Entity $$0, Entity $$1) {
      return $$2 -> {
         boolean $$3 = !$$2.isSpectator();
         boolean $$4 = $$2 != $$0 && $$2 != $$1;
         boolean $$5 = !$$0.isAlliedTo($$2);
         boolean $$8 = !($$2 instanceof TamableAnimal $$6 && $$1 instanceof LivingEntity $$7 && $$6.isTame() && $$6.isOwnedBy($$7));
         boolean $$11 = !($$2 instanceof ArmorStand $$9 && $$9.isMarker());
         boolean $$12 = $$1.distanceToSqr($$2) <= Math.pow(3.5, 2.0);
         boolean $$14 = !($$2 instanceof Player $$13 && $$13.isCreative() && $$13.getAbilities().flying);
         return $$3 && $$4 && $$5 && $$8 && $$11 && $$12 && $$14;
      };
   }

   private static double getKnockbackPower(Entity $$0, LivingEntity $$1, Vec3 $$2) {
      return (3.5 - $$2.length()) * 0.7F * ($$0.fallDistance > 5.0 ? 2 : 1) * (1.0 - $$1.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
   }

   public static boolean canSmashAttack(LivingEntity $$0) {
      return $$0.fallDistance > 1.5 && !$$0.isFallFlying();
   }

   @Nullable
   @Override
   public DamageSource getItemDamageSource(LivingEntity $$0) {
      return canSmashAttack($$0) ? $$0.damageSources().mace($$0) : super.getItemDamageSource($$0);
   }
}
