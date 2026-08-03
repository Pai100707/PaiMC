package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class RunOne<E extends net.minecraft.world.entity.LivingEntity> extends GateBehavior<E> {
   public RunOne(List<Pair<? extends BehaviorControl<? super E>, Integer>> $$0) {
      this(ImmutableMap.of(), $$0);
   }

   public RunOne(Map<MemoryModuleType<?>, MemoryStatus> $$0, List<Pair<? extends BehaviorControl<? super E>, Integer>> $$1) {
      super($$0, ImmutableSet.of(), GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE, $$1);
   }
}
