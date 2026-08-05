/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.gametest.framework;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TestBlock;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.TestBlockMode;

public class BlockBasedTestInstance
extends GameTestInstance {
    public static final MapCodec<BlockBasedTestInstance> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)TestData.CODEC.forGetter(GameTestInstance::info)).apply((Applicative)$$0, BlockBasedTestInstance::new));

    public BlockBasedTestInstance(TestData<Holder<TestEnvironmentDefinition>> $$0) {
        super($$0);
    }

    @Override
    public void run(GameTestHelper $$0) {
        BlockPos $$1 = this.findStartBlock($$0);
        TestBlockEntity $$2 = $$0.getBlockEntity($$1, TestBlockEntity.class);
        $$2.trigger();
        $$0.onEachTick(() -> {
            boolean $$2;
            List<BlockPos> $$12 = this.findTestBlocks($$0, TestBlockMode.ACCEPT);
            if ($$12.isEmpty()) {
                $$0.fail(Component.translatable("test_block.error.missing", TestBlockMode.ACCEPT.getDisplayName()));
            }
            if ($$2 = $$12.stream().map($$1 -> $$0.getBlockEntity((BlockPos)$$1, TestBlockEntity.class)).anyMatch(TestBlockEntity::hasTriggered)) {
                $$0.succeed();
            } else {
                this.forAllTriggeredTestBlocks($$0, TestBlockMode.FAIL, $$1 -> $$0.fail(Component.literal($$1.getMessage())));
                this.forAllTriggeredTestBlocks($$0, TestBlockMode.LOG, TestBlockEntity::trigger);
            }
        });
    }

    private void forAllTriggeredTestBlocks(GameTestHelper $$0, TestBlockMode $$1, Consumer<TestBlockEntity> $$2) {
        List<BlockPos> $$3 = this.findTestBlocks($$0, $$1);
        for (BlockPos $$4 : $$3) {
            TestBlockEntity $$5 = $$0.getBlockEntity($$4, TestBlockEntity.class);
            if (!$$5.hasTriggered()) continue;
            $$2.accept($$5);
            $$5.reset();
        }
    }

    private BlockPos findStartBlock(GameTestHelper $$0) {
        List<BlockPos> $$1 = this.findTestBlocks($$0, TestBlockMode.START);
        if ($$1.isEmpty()) {
            $$0.fail(Component.translatable("test_block.error.missing", TestBlockMode.START.getDisplayName()));
        }
        if ($$1.size() != 1) {
            $$0.fail(Component.translatable("test_block.error.too_many", TestBlockMode.START.getDisplayName()));
        }
        return $$1.getFirst();
    }

    private List<BlockPos> findTestBlocks(GameTestHelper $$0, TestBlockMode $$1) {
        ArrayList<BlockPos> $$2 = new ArrayList<BlockPos>();
        $$0.forEveryBlockInStructure($$3 -> {
            BlockState $$4 = $$0.getBlockState((BlockPos)$$3);
            if ($$4.is(Blocks.TEST_BLOCK) && $$4.getValue(TestBlock.MODE) == $$1) {
                $$2.add($$3.immutable());
            }
        });
        return $$2;
    }

    public MapCodec<BlockBasedTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.translatable("test_instance.type.block_based");
    }
}

