package net.minecraft.world.entity.projectile.throwableitemprojectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownLingeringPotion extends AbstractThrownPotion {
   public ThrownLingeringPotion(net.minecraft.world.entity.EntityType<? extends ThrownLingeringPotion> $$0, Level $$1) {
      super($$0, $$1);
   }

   public ThrownLingeringPotion(Level $$0, net.minecraft.world.entity.LivingEntity $$1, ItemStack $$2) {
      super(net.minecraft.world.entity.EntityType.LINGERING_POTION, $$0, $$1, $$2);
   }

   public ThrownLingeringPotion(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
      super(net.minecraft.world.entity.EntityType.LINGERING_POTION, $$0, $$1, $$2, $$3, $$4);
   }

   @Override
   protected Item getDefaultItem() {
      return Items.LINGERING_POTION;
   }

   @Override
   public void onHitAsPotion(ServerLevel $$0, ItemStack $$1, HitResult $$2) {
      net.minecraft.world.entity.AreaEffectCloud $$3 = new net.minecraft.world.entity.AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
      if (this.getOwner() instanceof net.minecraft.world.entity.LivingEntity $$4) {
         $$3.setOwner($$4);
      }

      $$3.setRadius(3.0F);
      $$3.setRadiusOnUse(-0.5F);
      $$3.setDuration(600);
      $$3.setWaitTime(10);
      $$3.setRadiusPerTick(-$$3.getRadius() / $$3.getDuration());
      $$3.applyComponentsFromItemStack($$1);
      $$0.addFreshEntity($$3);
   }
}
