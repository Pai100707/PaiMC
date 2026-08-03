package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TridentItem extends net.minecraft.world.item.Item implements net.minecraft.world.item.ProjectileItem {
   public static final int THROW_THRESHOLD_TIME = 10;
   public static final float BASE_DAMAGE = 8.0F;
   public static final float PROJECTILE_SHOOT_POWER = 2.5F;

   public TridentItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   public static ItemAttributeModifiers createAttributes() {
      return ItemAttributeModifiers.builder()
         .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
         .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9F, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
         .build();
   }

   public static Tool createToolProperties() {
      return new Tool(List.of(), 1.0F, 2, false);
   }

   @Override
   public net.minecraft.world.item.ItemUseAnimation getUseAnimation(net.minecraft.world.item.ItemStack $$0) {
      return net.minecraft.world.item.ItemUseAnimation.TRIDENT;
   }

   @Override
   public int getUseDuration(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1) {
      return 72000;
   }

   @Override
   public boolean releaseUsing(net.minecraft.world.item.ItemStack $$0, Level $$1, LivingEntity $$2, int $$3) {
      if ($$2 instanceof Player $$4) {
         int $$6 = this.getUseDuration($$0, $$2) - $$3;
         if ($$6 < 10) {
            return false;
         } else {
            float $$7 = EnchantmentHelper.getTridentSpinAttackStrength($$0, $$4);
            if ($$7 > 0.0F && !$$4.isInWaterOrRain()) {
               return false;
            } else if ($$0.nextDamageWillBreak()) {
               return false;
            } else {
               Holder<SoundEvent> $$8 = EnchantmentHelper.<Holder<SoundEvent>>pickHighestLevel($$0, EnchantmentEffectComponents.TRIDENT_SOUND)
                  .orElse(SoundEvents.TRIDENT_THROW);
               $$4.awardStat(Stats.ITEM_USED.get(this));
               if ($$1 instanceof ServerLevel $$9) {
                  $$0.hurtWithoutBreaking(1, $$4);
                  if ($$7 == 0.0F) {
                     net.minecraft.world.item.ItemStack $$10 = $$0.consumeAndReturn(1, $$4);
                     ThrownTrident $$11 = (ThrownTrident)Projectile.spawnProjectileFromRotation(ThrownTrident::new, $$9, $$10, $$4, 0.0F, 2.5F, 1.0F);
                     if ($$4.hasInfiniteMaterials()) {
                        $$11.pickup = Pickup.CREATIVE_ONLY;
                     }

                     $$1.playSound(null, $$11, (SoundEvent)$$8.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                     return true;
                  }
               }

               if ($$7 > 0.0F) {
                  float $$12 = $$4.getYRot();
                  float $$13 = $$4.getXRot();
                  float $$14 = -Mth.sin($$12 * (float) (Math.PI / 180.0)) * Mth.cos($$13 * (float) (Math.PI / 180.0));
                  float $$15 = -Mth.sin($$13 * (float) (Math.PI / 180.0));
                  float $$16 = Mth.cos($$12 * (float) (Math.PI / 180.0)) * Mth.cos($$13 * (float) (Math.PI / 180.0));
                  float $$17 = Mth.sqrt($$14 * $$14 + $$15 * $$15 + $$16 * $$16);
                  $$14 *= $$7 / $$17;
                  $$15 *= $$7 / $$17;
                  $$16 *= $$7 / $$17;
                  $$4.push($$14, $$15, $$16);
                  $$4.startAutoSpinAttack(20, 8.0F, $$0);
                  if ($$4.onGround()) {
                     float $$18 = 1.1999999F;
                     $$4.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
                  }

                  $$1.playSound(null, $$4, (SoundEvent)$$8.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                  return true;
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      if ($$3.nextDamageWillBreak()) {
         return InteractionResult.FAIL;
      } else if (EnchantmentHelper.getTridentSpinAttackStrength($$3, $$1) > 0.0F && !$$1.isInWaterOrRain()) {
         return InteractionResult.FAIL;
      } else {
         $$1.startUsingItem($$2);
         return InteractionResult.CONSUME;
      }
   }

   @Override
   public Projectile asProjectile(Level $$0, Position $$1, net.minecraft.world.item.ItemStack $$2, Direction $$3) {
      ThrownTrident $$4 = new ThrownTrident($$0, $$1.x(), $$1.y(), $$1.z(), $$2.copyWithCount(1));
      $$4.pickup = Pickup.ALLOWED;
      return $$4;
   }
}
