package net.minecraft.world.food;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FoodData {
   private static final int DEFAULT_TICK_TIMER = 0;
   private static final float DEFAULT_EXHAUSTION_LEVEL = 0.0F;
   private int foodLevel = 20;
   private float saturationLevel = 5.0F;
   private float exhaustionLevel;
   private int tickTimer;

   private void add(int $$0, float $$1) {
      this.foodLevel = Mth.clamp($$0 + this.foodLevel, 0, 20);
      this.saturationLevel = Mth.clamp($$1 + this.saturationLevel, 0.0F, this.foodLevel);
   }

   public void eat(int $$0, float $$1) {
      this.add($$0, net.minecraft.world.food.FoodConstants.saturationByModifier($$0, $$1));
   }

   public void eat(net.minecraft.world.food.FoodProperties $$0) {
      this.add($$0.nutrition(), $$0.saturation());
   }

   public void tick(ServerPlayer $$0) {
      ServerLevel $$1 = $$0.level();
      Difficulty $$2 = $$1.getDifficulty();
      if (this.exhaustionLevel > 4.0F) {
         this.exhaustionLevel -= 4.0F;
         if (this.saturationLevel > 0.0F) {
            this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
         } else if ($$2 != Difficulty.PEACEFUL) {
            this.foodLevel = Math.max(this.foodLevel - 1, 0);
         }
      }

      boolean $$3 = (Boolean)$$1.getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION);
      if ($$3 && this.saturationLevel > 0.0F && $$0.isHurt() && this.foodLevel >= 20) {
         this.tickTimer++;
         if (this.tickTimer >= 10) {
            float $$4 = Math.min(this.saturationLevel, 6.0F);
            $$0.heal($$4 / 6.0F);
            this.addExhaustion($$4);
            this.tickTimer = 0;
         }
      } else if ($$3 && this.foodLevel >= 18 && $$0.isHurt()) {
         this.tickTimer++;
         if (this.tickTimer >= 80) {
            $$0.heal(1.0F);
            this.addExhaustion(6.0F);
            this.tickTimer = 0;
         }
      } else if (this.foodLevel <= 0) {
         this.tickTimer++;
         if (this.tickTimer >= 80) {
            if ($$0.getHealth() > 10.0F || $$2 == Difficulty.HARD || $$0.getHealth() > 1.0F && $$2 == Difficulty.NORMAL) {
               $$0.hurtServer($$1, $$0.damageSources().starve(), 1.0F);
            }

            this.tickTimer = 0;
         }
      } else {
         this.tickTimer = 0;
      }
   }

   public void readAdditionalSaveData(ValueInput $$0) {
      this.foodLevel = $$0.getIntOr("foodLevel", 20);
      this.tickTimer = $$0.getIntOr("foodTickTimer", 0);
      this.saturationLevel = $$0.getFloatOr("foodSaturationLevel", 5.0F);
      this.exhaustionLevel = $$0.getFloatOr("foodExhaustionLevel", 0.0F);
   }

   public void addAdditionalSaveData(ValueOutput $$0) {
      $$0.putInt("foodLevel", this.foodLevel);
      $$0.putInt("foodTickTimer", this.tickTimer);
      $$0.putFloat("foodSaturationLevel", this.saturationLevel);
      $$0.putFloat("foodExhaustionLevel", this.exhaustionLevel);
   }

   public int getFoodLevel() {
      return this.foodLevel;
   }

   public boolean hasEnoughFood() {
      return this.getFoodLevel() > 6.0F;
   }

   public boolean needsFood() {
      return this.foodLevel < 20;
   }

   public void addExhaustion(float $$0) {
      this.exhaustionLevel = Math.min(this.exhaustionLevel + $$0, 40.0F);
   }

   public float getSaturationLevel() {
      return this.saturationLevel;
   }

   public void setFoodLevel(int $$0) {
      this.foodLevel = $$0;
   }

   public void setSaturation(float $$0) {
      this.saturationLevel = $$0;
   }
}
