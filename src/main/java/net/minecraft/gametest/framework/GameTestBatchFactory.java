package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class GameTestBatchFactory {
   private static final int MAX_TESTS_PER_BATCH = 50;
   public static final GameTestBatchFactory.TestDecorator DIRECT = ($$0, $$1) -> Stream.of(new GameTestInfo($$0, Rotation.NONE, $$1, RetryOptions.noRetries()));

   public static List<GameTestBatch> divideIntoBatches(Collection<Reference<GameTestInstance>> $$0, GameTestBatchFactory.TestDecorator $$1, ServerLevel $$2) {
      Map<Holder<TestEnvironmentDefinition>, List<GameTestInfo>> $$3 = $$0.stream()
         .flatMap($$2x -> $$1.decorate($$2x, $$2))
         .collect(Collectors.groupingBy($$0x -> $$0x.getTest().batch()));
      return $$3.entrySet().stream().flatMap($$0x -> {
         Holder<TestEnvironmentDefinition> $$1x = (Holder<TestEnvironmentDefinition>)$$0x.getKey();
         List<GameTestInfo> $$2x = (List<GameTestInfo>)$$0x.getValue();
         return Streams.mapWithIndex(Lists.partition($$2x, 50).stream(), ($$1xx, $$2xx) -> toGameTestBatch($$1xx, $$1x, (int)$$2xx));
      }).toList();
   }

   public static GameTestRunner.GameTestBatcher fromGameTestInfo() {
      return fromGameTestInfo(50);
   }

   public static GameTestRunner.GameTestBatcher fromGameTestInfo(int $$0) {
      return $$1 -> {
         Map<Holder<TestEnvironmentDefinition>, List<GameTestInfo>> $$2 = $$1.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy($$0xx -> $$0xx.getTest().batch()));
         return $$2.entrySet().stream().flatMap($$1x -> {
            Holder<TestEnvironmentDefinition> $$2x = (Holder<TestEnvironmentDefinition>)$$1x.getKey();
            List<GameTestInfo> $$3 = (List<GameTestInfo>)$$1x.getValue();
            return Streams.mapWithIndex(Lists.partition($$3, $$0).stream(), ($$1xx, $$2xx) -> toGameTestBatch(List.copyOf($$1xx), $$2x, (int)$$2xx));
         }).toList();
      };
   }

   public static GameTestBatch toGameTestBatch(Collection<GameTestInfo> $$0, Holder<TestEnvironmentDefinition> $$1, int $$2) {
      return new GameTestBatch($$2, $$0, $$1);
   }

   @FunctionalInterface
   public interface TestDecorator {
      Stream<GameTestInfo> decorate(Reference<GameTestInstance> var1, ServerLevel var2);
   }
}
