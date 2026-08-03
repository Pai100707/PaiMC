package net.minecraft.gametest.framework;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TestBlock;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.TestBlockMode;

public class BlockBasedTestInstance extends GameTestInstance {
   public static final MapCodec<BlockBasedTestInstance> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(TestData.CODEC.forGetter(GameTestInstance::info)).apply($$0, BlockBasedTestInstance::new)
   );

   public BlockBasedTestInstance(TestData<Holder<TestEnvironmentDefinition>> $$0) {
      super($$0);
   }

   @Override
   public void run(GameTestHelper $$0) {
      BlockPos $$1 = this.findStartBlock($$0);
      TestBlockEntity $$2 = $$0.getBlockEntity($$1, TestBlockEntity.class);
      $$2.trigger();
      $$0.onEachTick(() -> {
         List<BlockPos> $$1x = this.findTestBlocks($$0, TestBlockMode.ACCEPT);
         if ($$1x.isEmpty()) {
            $$0.fail(Component.translatable("test_block.error.missing", new Object[]{TestBlockMode.ACCEPT.getDisplayName()}));
         }

         boolean $$2x = $$1x.stream().map($$1xx -> $$0.getBlockEntity($$1xx, TestBlockEntity.class)).anyMatch(TestBlockEntity::hasTriggered);
         if ($$2x) {
            $$0.succeed();
         } else {
            this.forAllTriggeredTestBlocks($$0, TestBlockMode.FAIL, $$1xx -> $$0.fail(Component.literal($$1xx.getMessage())));
            this.forAllTriggeredTestBlocks($$0, TestBlockMode.LOG, TestBlockEntity::trigger);
         }
      });
   }

   private void forAllTriggeredTestBlocks(GameTestHelper $$0, TestBlockMode $$1, Consumer<TestBlockEntity> $$2) {
      for (BlockPos $$4 : this.findTestBlocks($$0, $$1)) {
         TestBlockEntity $$5 = $$0.getBlockEntity($$4, TestBlockEntity.class);
         if ($$5.hasTriggered()) {
            $$2.accept($$5);
            $$5.reset();
         }
      }
   }

   private BlockPos findStartBlock(GameTestHelper $$0) {
      List<BlockPos> $$1 = this.findTestBlocks($$0, TestBlockMode.START);
      if ($$1.isEmpty()) {
         $$0.fail(Component.translatable("test_block.error.missing", new Object[]{TestBlockMode.START.getDisplayName()}));
      }

      if ($$1.size() != 1) {
         $$0.fail(Component.translatable("test_block.error.too_many", new Object[]{TestBlockMode.START.getDisplayName()}));
      }

      return $$1.getFirst();
   }

   private List<BlockPos> findTestBlocks(GameTestHelper $$0, TestBlockMode $$1) {
      List<BlockPos> $$2 = new ArrayList<>();
      $$0.forEveryBlockInStructure($$3 -> {
         BlockState $$4 = $$0.getBlockState($$3);
         if ($$4.is(Blocks.TEST_BLOCK) && $$4.getValue(TestBlock.MODE) == $$1) {
            $$2.add($$3.immutable());
         }
      });
      return $$2;
   }

   @Override
   public MapCodec<BlockBasedTestInstance> codec() {
      return CODEC;
   }

   @Override
   protected MutableComponent typeDescription() {
      return Component.translatable("test_instance.type.block_based");
   }
}
