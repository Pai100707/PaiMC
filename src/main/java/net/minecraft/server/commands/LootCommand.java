package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LootCommand {
   private static final DynamicCommandExceptionType ERROR_NO_HELD_ITEMS = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.drop.no_held_items", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_NO_ENTITY_LOOT_TABLE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.drop.no_loot_table.entity", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_NO_BLOCK_LOOT_TABLE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.drop.no_loot_table.block", new Object[]{$$0})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         addTargets(
            (LiteralArgumentBuilder & ArgumentBuilder)Commands.literal("loot").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)),
            ($$1x, $$2) -> $$1x.then(
                  Commands.literal("fish")
                     .then(
                        Commands.argument("loot_table", ResourceOrIdArgument.lootTable($$1))
                           .then(
                              ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                             "pos", BlockPosArgument.blockPos()
                                          )
                                          .executes(
                                             $$1xx -> dropFishingLoot(
                                                $$1xx,
                                                ResourceOrIdArgument.getLootTable($$1xx, "loot_table"),
                                                BlockPosArgument.getLoadedBlockPos($$1xx, "pos"),
                                                ItemStack.EMPTY,
                                                $$2
                                             )
                                          ))
                                       .then(
                                          Commands.argument("tool", ItemArgument.item($$1))
                                             .executes(
                                                $$1xx -> dropFishingLoot(
                                                   $$1xx,
                                                   ResourceOrIdArgument.getLootTable($$1xx, "loot_table"),
                                                   BlockPosArgument.getLoadedBlockPos($$1xx, "pos"),
                                                   ItemArgument.getItem($$1xx, "tool").createItemStack(1, false),
                                                   $$2
                                                )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("mainhand")
                                          .executes(
                                             $$1xx -> dropFishingLoot(
                                                $$1xx,
                                                ResourceOrIdArgument.getLootTable($$1xx, "loot_table"),
                                                BlockPosArgument.getLoadedBlockPos($$1xx, "pos"),
                                                getSourceHandItem((CommandSourceStack)$$1xx.getSource(), EquipmentSlot.MAINHAND),
                                                $$2
                                             )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("offhand")
                                       .executes(
                                          $$1xx -> dropFishingLoot(
                                             $$1xx,
                                             ResourceOrIdArgument.getLootTable($$1xx, "loot_table"),
                                             BlockPosArgument.getLoadedBlockPos($$1xx, "pos"),
                                             getSourceHandItem((CommandSourceStack)$$1xx.getSource(), EquipmentSlot.OFFHAND),
                                             $$2
                                          )
                                       )
                                 )
                           )
                     )
               )
               .then(
                  Commands.literal("loot")
                     .then(
                        Commands.argument("loot_table", ResourceOrIdArgument.lootTable($$1))
                           .executes($$1xx -> dropChestLoot($$1xx, ResourceOrIdArgument.getLootTable($$1xx, "loot_table"), $$2))
                     )
               )
               .then(
                  Commands.literal("kill")
                     .then(
                        Commands.argument("target", EntityArgument.entity())
                           .executes($$1xx -> dropKillLoot($$1xx, EntityArgument.getEntity($$1xx, "target"), $$2))
                     )
               )
               .then(
                  Commands.literal("mine")
                     .then(
                        ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos())
                                    .executes($$1xx -> dropBlockLoot($$1xx, BlockPosArgument.getLoadedBlockPos($$1xx, "pos"), ItemStack.EMPTY, $$2)))
                                 .then(
                                    Commands.argument("tool", ItemArgument.item($$1))
                                       .executes(
                                          $$1xx -> dropBlockLoot(
                                             $$1xx,
                                             BlockPosArgument.getLoadedBlockPos($$1xx, "pos"),
                                             ItemArgument.getItem($$1xx, "tool").createItemStack(1, false),
                                             $$2
                                          )
                                       )
                                 ))
                              .then(
                                 Commands.literal("mainhand")
                                    .executes(
                                       $$1xx -> dropBlockLoot(
                                          $$1xx,
                                          BlockPosArgument.getLoadedBlockPos($$1xx, "pos"),
                                          getSourceHandItem((CommandSourceStack)$$1xx.getSource(), EquipmentSlot.MAINHAND),
                                          $$2
                                       )
                                    )
                              ))
                           .then(
                              Commands.literal("offhand")
                                 .executes(
                                    $$1xx -> dropBlockLoot(
                                       $$1xx,
                                       BlockPosArgument.getLoadedBlockPos($$1xx, "pos"),
                                       getSourceHandItem((CommandSourceStack)$$1xx.getSource(), EquipmentSlot.OFFHAND),
                                       $$2
                                    )
                                 )
                           )
                     )
               )
         )
      );
   }

   private static <T extends ArgumentBuilder<CommandSourceStack, T>> T addTargets(T $$0, LootCommand.TailProvider $$1) {
      return (T)$$0.then(
            ((LiteralArgumentBuilder)Commands.literal("replace")
                  .then(
                     Commands.literal("entity")
                        .then(
                           Commands.argument("entities", EntityArgument.entities())
                              .then(
                                 $$1.construct(
                                       Commands.argument("slot", SlotArgument.slot()),
                                       ($$0x, $$1x, $$2) -> entityReplace(
                                          EntityArgument.getEntities($$0x, "entities"), SlotArgument.getSlot($$0x, "slot"), $$1x.size(), $$1x, $$2
                                       )
                                    )
                                    .then(
                                       $$1.construct(
                                          Commands.argument("count", IntegerArgumentType.integer(0)),
                                          ($$0x, $$1x, $$2) -> entityReplace(
                                             EntityArgument.getEntities($$0x, "entities"),
                                             SlotArgument.getSlot($$0x, "slot"),
                                             IntegerArgumentType.getInteger($$0x, "count"),
                                             $$1x,
                                             $$2
                                          )
                                       )
                                    )
                              )
                        )
                  ))
               .then(
                  Commands.literal("block")
                     .then(
                        Commands.argument("targetPos", BlockPosArgument.blockPos())
                           .then(
                              $$1.construct(
                                    Commands.argument("slot", SlotArgument.slot()),
                                    ($$0x, $$1x, $$2) -> blockReplace(
                                       (CommandSourceStack)$$0x.getSource(),
                                       BlockPosArgument.getLoadedBlockPos($$0x, "targetPos"),
                                       SlotArgument.getSlot($$0x, "slot"),
                                       $$1x.size(),
                                       $$1x,
                                       $$2
                                    )
                                 )
                                 .then(
                                    $$1.construct(
                                       Commands.argument("count", IntegerArgumentType.integer(0)),
                                       ($$0x, $$1x, $$2) -> blockReplace(
                                          (CommandSourceStack)$$0x.getSource(),
                                          BlockPosArgument.getLoadedBlockPos($$0x, "targetPos"),
                                          IntegerArgumentType.getInteger($$0x, "slot"),
                                          IntegerArgumentType.getInteger($$0x, "count"),
                                          $$1x,
                                          $$2
                                       )
                                    )
                                 )
                           )
                     )
               )
         )
         .then(
            Commands.literal("insert")
               .then(
                  $$1.construct(
                     Commands.argument("targetPos", BlockPosArgument.blockPos()),
                     ($$0x, $$1x, $$2) -> blockDistribute(
                        (CommandSourceStack)$$0x.getSource(), BlockPosArgument.getLoadedBlockPos($$0x, "targetPos"), $$1x, $$2
                     )
                  )
               )
         )
         .then(
            Commands.literal("give")
               .then(
                  $$1.construct(
                     Commands.argument("players", EntityArgument.players()),
                     ($$0x, $$1x, $$2) -> playerGive(EntityArgument.getPlayers($$0x, "players"), $$1x, $$2)
                  )
               )
         )
         .then(
            Commands.literal("spawn")
               .then(
                  $$1.construct(
                     Commands.argument("targetPos", Vec3Argument.vec3()),
                     ($$0x, $$1x, $$2) -> dropInWorld((CommandSourceStack)$$0x.getSource(), Vec3Argument.getVec3($$0x, "targetPos"), $$1x, $$2)
                  )
               )
         );
   }

   private static Container getContainer(CommandSourceStack $$0, BlockPos $$1) throws CommandSyntaxException {
      BlockEntity $$2 = $$0.getLevel().getBlockEntity($$1);
      if (!($$2 instanceof Container)) {
         throw ItemCommands.ERROR_TARGET_NOT_A_CONTAINER.create($$1.getX(), $$1.getY(), $$1.getZ());
      } else {
         return (Container)$$2;
      }
   }

   private static int blockDistribute(CommandSourceStack $$0, BlockPos $$1, List<ItemStack> $$2, LootCommand.Callback $$3) throws CommandSyntaxException {
      Container $$4 = getContainer($$0, $$1);
      List<ItemStack> $$5 = Lists.newArrayListWithCapacity($$2.size());

      for (ItemStack $$6 : $$2) {
         if (distributeToContainer($$4, $$6.copy())) {
            $$4.setChanged();
            $$5.add($$6);
         }
      }

      $$3.accept($$5);
      return $$5.size();
   }

   private static boolean distributeToContainer(Container $$0, ItemStack $$1) {
      boolean $$2 = false;

      for (int $$3 = 0; $$3 < $$0.getContainerSize() && !$$1.isEmpty(); $$3++) {
         ItemStack $$4 = $$0.getItem($$3);
         if ($$0.canPlaceItem($$3, $$1)) {
            if ($$4.isEmpty()) {
               $$0.setItem($$3, $$1);
               $$2 = true;
               break;
            }

            if (canMergeItems($$4, $$1)) {
               int $$5 = $$1.getMaxStackSize() - $$4.getCount();
               int $$6 = Math.min($$1.getCount(), $$5);
               $$1.shrink($$6);
               $$4.grow($$6);
               $$2 = true;
            }
         }
      }

      return $$2;
   }

   private static int blockReplace(CommandSourceStack $$0, BlockPos $$1, int $$2, int $$3, List<ItemStack> $$4, LootCommand.Callback $$5) throws CommandSyntaxException {
      Container $$6 = getContainer($$0, $$1);
      int $$7 = $$6.getContainerSize();
      if ($$2 >= 0 && $$2 < $$7) {
         List<ItemStack> $$8 = Lists.newArrayListWithCapacity($$4.size());

         for (int $$9 = 0; $$9 < $$3; $$9++) {
            int $$10 = $$2 + $$9;
            ItemStack $$11 = $$9 < $$4.size() ? $$4.get($$9) : ItemStack.EMPTY;
            if ($$6.canPlaceItem($$10, $$11)) {
               $$6.setItem($$10, $$11);
               $$8.add($$11);
            }
         }

         $$5.accept($$8);
         return $$8.size();
      } else {
         throw ItemCommands.ERROR_TARGET_INAPPLICABLE_SLOT.create($$2);
      }
   }

   private static boolean canMergeItems(ItemStack $$0, ItemStack $$1) {
      return $$0.getCount() <= $$0.getMaxStackSize() && ItemStack.isSameItemSameComponents($$0, $$1);
   }

   private static int playerGive(Collection<ServerPlayer> $$0, List<ItemStack> $$1, LootCommand.Callback $$2) throws CommandSyntaxException {
      List<ItemStack> $$3 = Lists.newArrayListWithCapacity($$1.size());

      for (ItemStack $$4 : $$1) {
         for (ServerPlayer $$5 : $$0) {
            if ($$5.getInventory().add($$4.copy())) {
               $$3.add($$4);
            }
         }
      }

      $$2.accept($$3);
      return $$3.size();
   }

   private static void setSlots(Entity $$0, List<ItemStack> $$1, int $$2, int $$3, List<ItemStack> $$4) {
      for (int $$5 = 0; $$5 < $$3; $$5++) {
         ItemStack $$6 = $$5 < $$1.size() ? $$1.get($$5) : ItemStack.EMPTY;
         SlotAccess $$7 = $$0.getSlot($$2 + $$5);
         if ($$7 != null && $$7.set($$6.copy())) {
            $$4.add($$6);
         }
      }
   }

   private static int entityReplace(Collection<? extends Entity> $$0, int $$1, int $$2, List<ItemStack> $$3, LootCommand.Callback $$4) throws CommandSyntaxException {
      List<ItemStack> $$5 = Lists.newArrayListWithCapacity($$3.size());

      for (Entity $$6 : $$0) {
         if ($$6 instanceof ServerPlayer $$7) {
            setSlots($$6, $$3, $$1, $$2, $$5);
            $$7.containerMenu.broadcastChanges();
         } else {
            setSlots($$6, $$3, $$1, $$2, $$5);
         }
      }

      $$4.accept($$5);
      return $$5.size();
   }

   private static int dropInWorld(CommandSourceStack $$0, Vec3 $$1, List<ItemStack> $$2, LootCommand.Callback $$3) throws CommandSyntaxException {
      ServerLevel $$4 = $$0.getLevel();
      $$2.forEach($$2x -> {
         ItemEntity $$3x = new ItemEntity($$4, $$1.x, $$1.y, $$1.z, $$2x.copy());
         $$3x.setDefaultPickUpDelay();
         $$4.addFreshEntity($$3x);
      });
      $$3.accept($$2);
      return $$2.size();
   }

   private static void callback(CommandSourceStack $$0, List<ItemStack> $$1) {
      if ($$1.size() == 1) {
         ItemStack $$2 = $$1.get(0);
         $$0.sendSuccess(() -> Component.translatable("commands.drop.success.single", new Object[]{$$2.getCount(), $$2.getDisplayName()}), false);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.drop.success.multiple", new Object[]{$$1.size()}), false);
      }
   }

   private static void callback(CommandSourceStack $$0, List<ItemStack> $$1, ResourceKey<LootTable> $$2) {
      if ($$1.size() == 1) {
         ItemStack $$3 = $$1.get(0);
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.drop.success.single_with_table", new Object[]{$$3.getCount(), $$3.getDisplayName(), Component.translationArg($$2.identifier())}
            ),
            false
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable("commands.drop.success.multiple_with_table", new Object[]{$$1.size(), Component.translationArg($$2.identifier())}),
            false
         );
      }
   }

   private static ItemStack getSourceHandItem(CommandSourceStack $$0, EquipmentSlot $$1) throws CommandSyntaxException {
      Entity $$2 = $$0.getEntityOrException();
      if ($$2 instanceof LivingEntity) {
         return ((LivingEntity)$$2).getItemBySlot($$1);
      } else {
         throw ERROR_NO_HELD_ITEMS.create($$2.getDisplayName());
      }
   }

   private static int dropBlockLoot(CommandContext<CommandSourceStack> $$0, BlockPos $$1, ItemStack $$2, LootCommand.DropConsumer $$3) throws CommandSyntaxException {
      CommandSourceStack $$4 = (CommandSourceStack)$$0.getSource();
      ServerLevel $$5 = $$4.getLevel();
      BlockState $$6 = $$5.getBlockState($$1);
      BlockEntity $$7 = $$5.getBlockEntity($$1);
      Optional<ResourceKey<LootTable>> $$8 = $$6.getBlock().getLootTable();
      if ($$8.isEmpty()) {
         throw ERROR_NO_BLOCK_LOOT_TABLE.create($$6.getBlock().getName());
      } else {
         Builder $$9 = new Builder($$5)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf($$1))
            .withParameter(LootContextParams.BLOCK_STATE, $$6)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, $$7)
            .withOptionalParameter(LootContextParams.THIS_ENTITY, $$4.getEntity())
            .withParameter(LootContextParams.TOOL, $$2);
         List<ItemStack> $$10 = $$6.getDrops($$9);
         return $$3.accept($$0, $$10, $$2x -> callback($$4, $$2x, $$8.get()));
      }
   }

   private static int dropKillLoot(CommandContext<CommandSourceStack> $$0, Entity $$1, LootCommand.DropConsumer $$2) throws CommandSyntaxException {
      Optional<ResourceKey<LootTable>> $$3 = $$1.getLootTable();
      if ($$3.isEmpty()) {
         throw ERROR_NO_ENTITY_LOOT_TABLE.create($$1.getDisplayName());
      } else {
         CommandSourceStack $$4 = (CommandSourceStack)$$0.getSource();
         Builder $$5 = new Builder($$4.getLevel());
         Entity $$6 = $$4.getEntity();
         if ($$6 instanceof Player $$7) {
            $$5.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, $$7);
         }

         $$5.withParameter(LootContextParams.DAMAGE_SOURCE, $$1.damageSources().magic());
         $$5.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, $$6);
         $$5.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, $$6);
         $$5.withParameter(LootContextParams.THIS_ENTITY, $$1);
         $$5.withParameter(LootContextParams.ORIGIN, $$4.getPosition());
         LootParams $$8 = $$5.create(LootContextParamSets.ENTITY);
         LootTable $$9 = $$4.getServer().reloadableRegistries().getLootTable($$3.get());
         List<ItemStack> $$10 = $$9.getRandomItems($$8);
         return $$2.accept($$0, $$10, $$2x -> callback($$4, $$2x, $$3.get()));
      }
   }

   private static int dropChestLoot(CommandContext<CommandSourceStack> $$0, Holder<LootTable> $$1, LootCommand.DropConsumer $$2) throws CommandSyntaxException {
      CommandSourceStack $$3 = (CommandSourceStack)$$0.getSource();
      LootParams $$4 = new Builder($$3.getLevel())
         .withOptionalParameter(LootContextParams.THIS_ENTITY, $$3.getEntity())
         .withParameter(LootContextParams.ORIGIN, $$3.getPosition())
         .create(LootContextParamSets.CHEST);
      return drop($$0, $$1, $$4, $$2);
   }

   private static int dropFishingLoot(CommandContext<CommandSourceStack> $$0, Holder<LootTable> $$1, BlockPos $$2, ItemStack $$3, LootCommand.DropConsumer $$4) throws CommandSyntaxException {
      CommandSourceStack $$5 = (CommandSourceStack)$$0.getSource();
      LootParams $$6 = new Builder($$5.getLevel())
         .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf($$2))
         .withParameter(LootContextParams.TOOL, $$3)
         .withOptionalParameter(LootContextParams.THIS_ENTITY, $$5.getEntity())
         .create(LootContextParamSets.FISHING);
      return drop($$0, $$1, $$6, $$4);
   }

   private static int drop(CommandContext<CommandSourceStack> $$0, Holder<LootTable> $$1, LootParams $$2, LootCommand.DropConsumer $$3) throws CommandSyntaxException {
      CommandSourceStack $$4 = (CommandSourceStack)$$0.getSource();
      List<ItemStack> $$5 = ((LootTable)$$1.value()).getRandomItems($$2);
      return $$3.accept($$0, $$5, $$1x -> callback($$4, $$1x));
   }

   @FunctionalInterface
   interface Callback {
      void accept(List<ItemStack> var1) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface DropConsumer {
      int accept(CommandContext<CommandSourceStack> var1, List<ItemStack> var2, LootCommand.Callback var3) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface TailProvider {
      ArgumentBuilder<CommandSourceStack, ?> construct(ArgumentBuilder<CommandSourceStack, ?> var1, LootCommand.DropConsumer var2);
   }
}
