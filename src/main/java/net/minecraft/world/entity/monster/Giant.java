package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class Giant extends Monster {
   public Giant(net.minecraft.world.entity.EntityType<? extends Giant> $$0, Level $$1) {
      super($$0, $$1);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 100.0)
         .add(Attributes.MOVEMENT_SPEED, 0.5)
         .add(Attributes.ATTACK_DAMAGE, 50.0)
         .add(Attributes.CAMERA_DISTANCE, 16.0);
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      return $$1.getPathfindingCostFromLightLevels($$0);
   }
}
