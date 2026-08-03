package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CraftingTableBlock extends Block {
   public static final MapCodec<CraftingTableBlock> CODEC = simpleCodec(CraftingTableBlock::new);
   private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

   @Override
   public MapCodec<? extends CraftingTableBlock> codec() {
      return CODEC;
   }

   protected CraftingTableBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide()) {
         $$3.openMenu($$0.getMenuProvider($$1, $$2));
         $$3.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   protected MenuProvider getMenuProvider(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      return new SimpleMenuProvider(($$2x, $$3, $$4) -> new CraftingMenu($$2x, $$3, ContainerLevelAccess.create($$1, $$2)), CONTAINER_TITLE);
   }
}
