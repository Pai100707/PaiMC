package net.minecraft.world.damagesource;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public record FallLocation(String id) {
   public static final net.minecraft.world.damagesource.FallLocation GENERIC = new net.minecraft.world.damagesource.FallLocation("generic");
   public static final net.minecraft.world.damagesource.FallLocation LADDER = new net.minecraft.world.damagesource.FallLocation("ladder");
   public static final net.minecraft.world.damagesource.FallLocation VINES = new net.minecraft.world.damagesource.FallLocation("vines");
   public static final net.minecraft.world.damagesource.FallLocation WEEPING_VINES = new net.minecraft.world.damagesource.FallLocation("weeping_vines");
   public static final net.minecraft.world.damagesource.FallLocation TWISTING_VINES = new net.minecraft.world.damagesource.FallLocation("twisting_vines");
   public static final net.minecraft.world.damagesource.FallLocation SCAFFOLDING = new net.minecraft.world.damagesource.FallLocation("scaffolding");
   public static final net.minecraft.world.damagesource.FallLocation OTHER_CLIMBABLE = new net.minecraft.world.damagesource.FallLocation("other_climbable");
   public static final net.minecraft.world.damagesource.FallLocation WATER = new net.minecraft.world.damagesource.FallLocation("water");

   public static net.minecraft.world.damagesource.FallLocation blockToFallLocation(BlockState $$0) {
      if ($$0.is(Blocks.LADDER) || $$0.is(BlockTags.TRAPDOORS)) {
         return LADDER;
      } else if ($$0.is(Blocks.VINE)) {
         return VINES;
      } else if ($$0.is(Blocks.WEEPING_VINES) || $$0.is(Blocks.WEEPING_VINES_PLANT)) {
         return WEEPING_VINES;
      } else if ($$0.is(Blocks.TWISTING_VINES) || $$0.is(Blocks.TWISTING_VINES_PLANT)) {
         return TWISTING_VINES;
      } else {
         return $$0.is(Blocks.SCAFFOLDING) ? SCAFFOLDING : OTHER_CLIMBABLE;
      }
   }

   @Nullable
   public static net.minecraft.world.damagesource.FallLocation getCurrentFallLocation(LivingEntity $$0) {
      Optional<BlockPos> $$1 = $$0.getLastClimbablePos();
      if ($$1.isPresent()) {
         BlockState $$2 = $$0.level().getBlockState($$1.get());
         return blockToFallLocation($$2);
      } else {
         return $$0.isInWater() ? WATER : null;
      }
   }

   public String languageKey() {
      return "death.fell.accident." + this.id;
   }
}
