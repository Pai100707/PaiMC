package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.OpenUrl;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.CommonLinks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class CombatTracker {
   public static final int RESET_DAMAGE_STATUS_TIME = 100;
   public static final int RESET_COMBAT_STATUS_TIME = 300;
   private static final Style INTENTIONAL_GAME_DESIGN_STYLE = Style.EMPTY
      .withClickEvent(new OpenUrl(CommonLinks.INTENTIONAL_GAME_DESIGN_BUG))
      .withHoverEvent(new ShowText(Component.literal("MCPE-28723")));
   private final List<net.minecraft.world.damagesource.CombatEntry> entries = Lists.newArrayList();
   private final LivingEntity mob;
   private int lastDamageTime;
   private int combatStartTime;
   private int combatEndTime;
   private boolean inCombat;
   private boolean takingDamage;

   public CombatTracker(LivingEntity $$0) {
      this.mob = $$0;
   }

   public void recordDamage(net.minecraft.world.damagesource.DamageSource $$0, float $$1) {
      this.recheckStatus();
      net.minecraft.world.damagesource.FallLocation $$2 = net.minecraft.world.damagesource.FallLocation.getCurrentFallLocation(this.mob);
      net.minecraft.world.damagesource.CombatEntry $$3 = new net.minecraft.world.damagesource.CombatEntry($$0, $$1, $$2, (float)this.mob.fallDistance);
      this.entries.add($$3);
      this.lastDamageTime = this.mob.tickCount;
      this.takingDamage = true;
      if (!this.inCombat && this.mob.isAlive() && shouldEnterCombat($$0)) {
         this.inCombat = true;
         this.combatStartTime = this.mob.tickCount;
         this.combatEndTime = this.combatStartTime;
         this.mob.onEnterCombat();
      }
   }

   private static boolean shouldEnterCombat(net.minecraft.world.damagesource.DamageSource $$0) {
      return $$0.getEntity() instanceof LivingEntity;
   }

   private Component getMessageForAssistedFall(Entity $$0, Component $$1, String $$2, String $$3) {
      ItemStack $$5 = $$0 instanceof LivingEntity $$4 ? $$4.getMainHandItem() : ItemStack.EMPTY;
      return !$$5.isEmpty() && $$5.has(DataComponents.CUSTOM_NAME)
         ? Component.translatable($$2, new Object[]{this.mob.getDisplayName(), $$1, $$5.getDisplayName()})
         : Component.translatable($$3, new Object[]{this.mob.getDisplayName(), $$1});
   }

   private Component getFallMessage(net.minecraft.world.damagesource.CombatEntry $$0, @Nullable Entity $$1) {
      net.minecraft.world.damagesource.DamageSource $$2 = $$0.source();
      if (!$$2.is(DamageTypeTags.IS_FALL) && !$$2.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
         Component $$4 = getDisplayName($$1);
         Entity $$5 = $$2.getEntity();
         Component $$6 = getDisplayName($$5);
         if ($$6 != null && !$$6.equals($$4)) {
            return this.getMessageForAssistedFall($$5, $$6, "death.fell.assist.item", "death.fell.assist");
         } else {
            return (Component)($$4 != null
               ? this.getMessageForAssistedFall($$1, $$4, "death.fell.finish.item", "death.fell.finish")
               : Component.translatable("death.fell.killer", new Object[]{this.mob.getDisplayName()}));
         }
      } else {
         net.minecraft.world.damagesource.FallLocation $$3 = Objects.requireNonNullElse(
            $$0.fallLocation(), net.minecraft.world.damagesource.FallLocation.GENERIC
         );
         return Component.translatable($$3.languageKey(), new Object[]{this.mob.getDisplayName()});
      }
   }

   @Nullable
   private static Component getDisplayName(@Nullable Entity $$0) {
      return $$0 == null ? null : $$0.getDisplayName();
   }

   public Component getDeathMessage() {
      if (this.entries.isEmpty()) {
         return Component.translatable("death.attack.generic", new Object[]{this.mob.getDisplayName()});
      } else {
         net.minecraft.world.damagesource.CombatEntry $$0 = this.entries.get(this.entries.size() - 1);
         net.minecraft.world.damagesource.DamageSource $$1 = $$0.source();
         net.minecraft.world.damagesource.CombatEntry $$2 = this.getMostSignificantFall();
         net.minecraft.world.damagesource.DeathMessageType $$3 = $$1.type().deathMessageType();
         if ($$3 == net.minecraft.world.damagesource.DeathMessageType.FALL_VARIANTS && $$2 != null) {
            return this.getFallMessage($$2, $$1.getEntity());
         } else if ($$3 == net.minecraft.world.damagesource.DeathMessageType.INTENTIONAL_GAME_DESIGN) {
            String $$4 = "death.attack." + $$1.getMsgId();
            Component $$5 = ComponentUtils.wrapInSquareBrackets(Component.translatable($$4 + ".link")).withStyle(INTENTIONAL_GAME_DESIGN_STYLE);
            return Component.translatable($$4 + ".message", new Object[]{this.mob.getDisplayName(), $$5});
         } else {
            return $$1.getLocalizedDeathMessage(this.mob);
         }
      }
   }

   @Nullable
   private net.minecraft.world.damagesource.CombatEntry getMostSignificantFall() {
      net.minecraft.world.damagesource.CombatEntry $$0 = null;
      net.minecraft.world.damagesource.CombatEntry $$1 = null;
      float $$2 = 0.0F;
      float $$3 = 0.0F;

      for (int $$4 = 0; $$4 < this.entries.size(); $$4++) {
         net.minecraft.world.damagesource.CombatEntry $$5 = this.entries.get($$4);
         net.minecraft.world.damagesource.CombatEntry $$6 = $$4 > 0 ? this.entries.get($$4 - 1) : null;
         net.minecraft.world.damagesource.DamageSource $$7 = $$5.source();
         boolean $$8 = $$7.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
         float $$9 = $$8 ? Float.MAX_VALUE : $$5.fallDistance();
         if (($$7.is(DamageTypeTags.IS_FALL) || $$8) && $$9 > 0.0F && ($$0 == null || $$9 > $$3)) {
            if ($$4 > 0) {
               $$0 = $$6;
            } else {
               $$0 = $$5;
            }

            $$3 = $$9;
         }

         if ($$5.fallLocation() != null && ($$1 == null || $$5.damage() > $$2)) {
            $$1 = $$5;
            $$2 = $$5.damage();
         }
      }

      if ($$3 > 5.0F && $$0 != null) {
         return $$0;
      } else {
         return $$2 > 5.0F && $$1 != null ? $$1 : null;
      }
   }

   public int getCombatDuration() {
      return this.inCombat ? this.mob.tickCount - this.combatStartTime : this.combatEndTime - this.combatStartTime;
   }

   public void recheckStatus() {
      int $$0 = this.inCombat ? 300 : 100;
      if (this.takingDamage && (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > $$0)) {
         boolean $$1 = this.inCombat;
         this.takingDamage = false;
         this.inCombat = false;
         this.combatEndTime = this.mob.tickCount;
         if ($$1) {
            this.mob.onLeaveCombat();
         }

         this.entries.clear();
      }
   }
}
