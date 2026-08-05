package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.kinds.Const.Mu;
import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public interface MemoryCondition<F extends K1, Value> {
   MemoryModuleType<Value> memory();

   MemoryStatus condition();

   
   MemoryAccessor<F, Value> createAccessor(Brain<?> var1, Optional<Value> var2);

   public record Absent<Value>(MemoryModuleType<Value> memory) implements MemoryCondition<Mu<Unit>, Value> {
      @Override
      public MemoryStatus condition() {
         return MemoryStatus.VALUE_ABSENT;
      }

      @Override
      public MemoryAccessor<Mu<Unit>, Value> createAccessor(Brain<?> $$0, Optional<Value> $$1) {
         return $$1.isPresent() ? null : new MemoryAccessor<>($$0, this.memory, Const.create(Unit.INSTANCE));
      }
   }

   public record Present<Value>(MemoryModuleType<Value> memory) implements MemoryCondition<com.mojang.datafixers.kinds.IdF.Mu, Value> {
      @Override
      public MemoryStatus condition() {
         return MemoryStatus.VALUE_PRESENT;
      }

      @Override
      public MemoryAccessor<com.mojang.datafixers.kinds.IdF.Mu, Value> createAccessor(Brain<?> $$0, Optional<Value> $$1) {
         return $$1.isEmpty() ? null : new MemoryAccessor<>($$0, this.memory, IdF.create($$1.get()));
      }
   }

   public record Registered<Value>(MemoryModuleType<Value> memory) implements MemoryCondition<com.mojang.datafixers.kinds.OptionalBox.Mu, Value> {
      @Override
      public MemoryStatus condition() {
         return MemoryStatus.REGISTERED;
      }

      @Override
      public MemoryAccessor<com.mojang.datafixers.kinds.OptionalBox.Mu, Value> createAccessor(Brain<?> $$0, Optional<Value> $$1) {
         return new MemoryAccessor<>($$0, this.memory, OptionalBox.create($$1));
      }
   }
}
