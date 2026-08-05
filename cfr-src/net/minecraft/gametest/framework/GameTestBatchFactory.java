/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Streams
 */
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
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class GameTestBatchFactory {
    private static final int MAX_TESTS_PER_BATCH = 50;
    public static final TestDecorator DIRECT = ($$0, $$1) -> Stream.of(new GameTestInfo($$0, Rotation.NONE, $$1, RetryOptions.noRetries()));

    public static List<GameTestBatch> divideIntoBatches(Collection<Holder.Reference<GameTestInstance>> $$02, TestDecorator $$1, ServerLevel $$22) {
        Map<Holder, List<GameTestInfo>> $$3 = $$02.stream().flatMap($$2 -> $$1.decorate((Holder.Reference<GameTestInstance>)$$2, $$22)).collect(Collectors.groupingBy($$0 -> $$0.getTest().batch()));
        return $$3.entrySet().stream().flatMap($$0 -> {
            Holder $$12 = (Holder)$$0.getKey();
            List $$22 = (List)$$0.getValue();
            return Streams.mapWithIndex(Lists.partition((List)$$22, (int)50).stream(), ($$1, $$2) -> GameTestBatchFactory.toGameTestBatch($$1, $$12, (int)$$2));
        }).toList();
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo() {
        return GameTestBatchFactory.fromGameTestInfo(50);
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo(int $$0) {
        return $$1 -> {
            Map<Holder, List<GameTestInfo>> $$2 = $$1.stream().filter(Objects::nonNull).collect(Collectors.groupingBy($$0 -> $$0.getTest().batch()));
            return $$2.entrySet().stream().flatMap($$12 -> {
                Holder $$22 = (Holder)$$12.getKey();
                List $$3 = (List)$$12.getValue();
                return Streams.mapWithIndex(Lists.partition((List)$$3, (int)$$0).stream(), ($$1, $$2) -> GameTestBatchFactory.toGameTestBatch(List.copyOf($$1), $$22, (int)$$2));
            }).toList();
        };
    }

    public static GameTestBatch toGameTestBatch(Collection<GameTestInfo> $$0, Holder<TestEnvironmentDefinition> $$1, int $$2) {
        return new GameTestBatch($$2, $$0, $$1);
    }

    @FunctionalInterface
    public static interface TestDecorator {
        public Stream<GameTestInfo> decorate(Holder.Reference<GameTestInstance> var1, ServerLevel var2);
    }
}

