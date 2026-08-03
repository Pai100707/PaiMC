package net.minecraft.world.entity.ai.behavior.warden;

import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TryToSniff {
   private static final IntProvider SNIFF_COOLDOWN = UniformInt.of(100, 200);

   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group(
               $$0.registered(MemoryModuleType.IS_SNIFFING),
               $$0.registered(MemoryModuleType.WALK_TARGET),
               $$0.absent(MemoryModuleType.SNIFF_COOLDOWN),
               $$0.present(MemoryModuleType.NEAREST_ATTACKABLE),
               $$0.absent(MemoryModuleType.DISTURBANCE_LOCATION)
            )
            .apply($$0, ($$0x, $$1, $$2, $$3, $$4) -> ($$3x, $$4x, $$5) -> {
               $$0x.set(Unit.INSTANCE);
               $$2.setWithExpiry(Unit.INSTANCE, SNIFF_COOLDOWN.sample($$3x.getRandom()));
               $$1.erase();
               $$4x.setPose(net.minecraft.world.entity.Pose.SNIFFING);
               return true;
            })
      );
   }
}
