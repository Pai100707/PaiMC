package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.UseCooldown;

public class ItemCooldowns {
   private final Map<Identifier, net.minecraft.world.item.ItemCooldowns.CooldownInstance> cooldowns = Maps.newHashMap();
   private int tickCount;

   public boolean isOnCooldown(net.minecraft.world.item.ItemStack $$0) {
      return this.getCooldownPercent($$0, 0.0F) > 0.0F;
   }

   public float getCooldownPercent(net.minecraft.world.item.ItemStack $$0, float $$1) {
      Identifier $$2 = this.getCooldownGroup($$0);
      net.minecraft.world.item.ItemCooldowns.CooldownInstance $$3 = this.cooldowns.get($$2);
      if ($$3 != null) {
         float $$4 = $$3.endTime - $$3.startTime;
         float $$5 = $$3.endTime - (this.tickCount + $$1);
         return Mth.clamp($$5 / $$4, 0.0F, 1.0F);
      } else {
         return 0.0F;
      }
   }

   public void tick() {
      this.tickCount++;
      if (!this.cooldowns.isEmpty()) {
         Iterator<Entry<Identifier, net.minecraft.world.item.ItemCooldowns.CooldownInstance>> $$0 = this.cooldowns.entrySet().iterator();

         while ($$0.hasNext()) {
            Entry<Identifier, net.minecraft.world.item.ItemCooldowns.CooldownInstance> $$1 = $$0.next();
            if ($$1.getValue().endTime <= this.tickCount) {
               $$0.remove();
               this.onCooldownEnded($$1.getKey());
            }
         }
      }
   }

   public Identifier getCooldownGroup(net.minecraft.world.item.ItemStack $$0) {
      UseCooldown $$1 = (UseCooldown)$$0.get(DataComponents.USE_COOLDOWN);
      Identifier $$2 = BuiltInRegistries.ITEM.getKey($$0.getItem());
      return $$1 == null ? $$2 : $$1.cooldownGroup().orElse($$2);
   }

   public void addCooldown(net.minecraft.world.item.ItemStack $$0, int $$1) {
      this.addCooldown(this.getCooldownGroup($$0), $$1);
   }

   public void addCooldown(Identifier $$0, int $$1) {
      this.cooldowns.put($$0, new net.minecraft.world.item.ItemCooldowns.CooldownInstance(this.tickCount, this.tickCount + $$1));
      this.onCooldownStarted($$0, $$1);
   }

   public void removeCooldown(Identifier $$0) {
      this.cooldowns.remove($$0);
      this.onCooldownEnded($$0);
   }

   protected void onCooldownStarted(Identifier $$0, int $$1) {
   }

   protected void onCooldownEnded(Identifier $$0) {
   }

   record CooldownInstance(int startTime, int endTime) {
   }
}
