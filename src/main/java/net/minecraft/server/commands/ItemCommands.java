package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
   static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
      ($$0, $$1, $$2) -> Component.translatableEscape("commands.item.target.not_a_container", new Object[]{$$0, $$1, $$2})
   );
   static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
      ($$0, $$1, $$2) -> Component.translatableEscape("commands.item.source.not_a_container", new Object[]{$$0, $$1, $$2})
   );
   static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.item.target.no_such_slot", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.item.source.no_such_slot", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.item.target.no_changes", new Object[]{$$0})
   );
   private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.item.target.no_changed.known_item", new Object[]{$$0, $$1})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("item")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("replace")
                        .then(
                           Commands.literal("block")
                              .then(
                                 Commands.argument("pos", BlockPosArgument.blockPos())
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.argument("slot", SlotArgument.slot())
                                             .then(
                                                Commands.literal("with")
                                                   .then(
                                                      ((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item($$1))
                                                            .executes(
                                                               $$0x -> setBlockItem(
                                                                  (CommandSourceStack)$$0x.getSource(),
                                                                  BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                                  SlotArgument.getSlot($$0x, "slot"),
                                                                  ItemArgument.getItem($$0x, "item").createItemStack(1, false)
                                                               )
                                                            ))
                                                         .then(
                                                            Commands.argument("count", IntegerArgumentType.integer(1, 99))
                                                               .executes(
                                                                  $$0x -> setBlockItem(
                                                                     (CommandSourceStack)$$0x.getSource(),
                                                                     BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                                     SlotArgument.getSlot($$0x, "slot"),
                                                                     ItemArgument.getItem($$0x, "item")
                                                                        .createItemStack(IntegerArgumentType.getInteger($$0x, "count"), true)
                                                                  )
                                                               )
                                                         )
                                                   )
                                             ))
                                          .then(
                                             ((LiteralArgumentBuilder)Commands.literal("from")
                                                   .then(
                                                      Commands.literal("block")
                                                         .then(
                                                            Commands.argument("source", BlockPosArgument.blockPos())
                                                               .then(
                                                                  ((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot())
                                                                        .executes(
                                                                           $$0x -> blockToBlock(
                                                                              (CommandSourceStack)$$0x.getSource(),
                                                                              BlockPosArgument.getLoadedBlockPos($$0x, "source"),
                                                                              SlotArgument.getSlot($$0x, "sourceSlot"),
                                                                              BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                                              SlotArgument.getSlot($$0x, "slot")
                                                                           )
                                                                        ))
                                                                     .then(
                                                                        Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1))
                                                                           .executes(
                                                                              $$0x -> blockToBlock(
                                                                                 (CommandSourceStack)$$0x.getSource(),
                                                                                 BlockPosArgument.getLoadedBlockPos($$0x, "source"),
                                                                                 SlotArgument.getSlot($$0x, "sourceSlot"),
                                                                                 BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                                                 SlotArgument.getSlot($$0x, "slot"),
                                                                                 ResourceOrIdArgument.getLootModifier($$0x, "modifier")
                                                                              )
                                                                           )
                                                                     )
                                                               )
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal("entity")
                                                      .then(
                                                         Commands.argument("source", EntityArgument.entity())
                                                            .then(
                                                               ((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot())
                                                                     .executes(
                                                                        $$0x -> entityToBlock(
                                                                           (CommandSourceStack)$$0x.getSource(),
                                                                           EntityArgument.getEntity($$0x, "source"),
                                                                           SlotArgument.getSlot($$0x, "sourceSlot"),
                                                                           BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                                           SlotArgument.getSlot($$0x, "slot")
                                                                        )
                                                                     ))
                                                                  .then(
                                                                     Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1))
                                                                        .executes(
                                                                           $$0x -> entityToBlock(
                                                                              (CommandSourceStack)$$0x.getSource(),
                                                                              EntityArgument.getEntity($$0x, "source"),
                                                                              SlotArgument.getSlot($$0x, "sourceSlot"),
                                                                              BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                                              SlotArgument.getSlot($$0x, "slot"),
                                                                              ResourceOrIdArgument.getLootModifier($$0x, "modifier")
                                                                           )
                                                                        )
                                                                  )
                                                            )
                                                      )
                                                )
                                          )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("entity")
                           .then(
                              Commands.argument("targets", EntityArgument.entities())
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("slot", SlotArgument.slot())
                                          .then(
                                             Commands.literal("with")
                                                .then(
                                                   ((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item($$1))
                                                         .executes(
                                                            $$0x -> setEntityItem(
                                                               (CommandSourceStack)$$0x.getSource(),
                                                               EntityArgument.getEntities($$0x, "targets"),
                                                               SlotArgument.getSlot($$0x, "slot"),
                                                               ItemArgument.getItem($$0x, "item").createItemStack(1, false)
                                                            )
                                                         ))
                                                      .then(
                                                         Commands.argument("count", IntegerArgumentType.integer(1, 99))
                                                            .executes(
                                                               $$0x -> setEntityItem(
                                                                  (CommandSourceStack)$$0x.getSource(),
                                                                  EntityArgument.getEntities($$0x, "targets"),
                                                                  SlotArgument.getSlot($$0x, "slot"),
                                                                  ItemArgument.getItem($$0x, "item")
                                                                     .createItemStack(IntegerArgumentType.getInteger($$0x, "count"), true)
                                                               )
                                                            )
                                                      )
                                                )
                                          ))
                                       .then(
                                          ((LiteralArgumentBuilder)Commands.literal("from")
                                                .then(
                                                   Commands.literal("block")
                                                      .then(
                                                         Commands.argument("source", BlockPosArgument.blockPos())
                                                            .then(
                                                               ((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot())
                                                                     .executes(
                                                                        $$0x -> blockToEntities(
                                                                           (CommandSourceStack)$$0x.getSource(),
                                                                           BlockPosArgument.getLoadedBlockPos($$0x, "source"),
                                                                           SlotArgument.getSlot($$0x, "sourceSlot"),
                                                                           EntityArgument.getEntities($$0x, "targets"),
                                                                           SlotArgument.getSlot($$0x, "slot")
                                                                        )
                                                                     ))
                                                                  .then(
                                                                     Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1))
                                                                        .executes(
                                                                           $$0x -> blockToEntities(
                                                                              (CommandSourceStack)$$0x.getSource(),
                                                                              BlockPosArgument.getLoadedBlockPos($$0x, "source"),
                                                                              SlotArgument.getSlot($$0x, "sourceSlot"),
                                                                              EntityArgument.getEntities($$0x, "targets"),
                                                                              SlotArgument.getSlot($$0x, "slot"),
                                                                              ResourceOrIdArgument.getLootModifier($$0x, "modifier")
                                                                           )
                                                                        )
                                                                  )
                                                            )
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("entity")
                                                   .then(
                                                      Commands.argument("source", EntityArgument.entity())
                                                         .then(
                                                            ((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot())
                                                                  .executes(
                                                                     $$0x -> entityToEntities(
                                                                        (CommandSourceStack)$$0x.getSource(),
                                                                        EntityArgument.getEntity($$0x, "source"),
                                                                        SlotArgument.getSlot($$0x, "sourceSlot"),
                                                                        EntityArgument.getEntities($$0x, "targets"),
                                                                        SlotArgument.getSlot($$0x, "slot")
                                                                     )
                                                                  ))
                                                               .then(
                                                                  Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1))
                                                                     .executes(
                                                                        $$0x -> entityToEntities(
                                                                           (CommandSourceStack)$$0x.getSource(),
                                                                           EntityArgument.getEntity($$0x, "source"),
                                                                           SlotArgument.getSlot($$0x, "sourceSlot"),
                                                                           EntityArgument.getEntities($$0x, "targets"),
                                                                           SlotArgument.getSlot($$0x, "slot"),
                                                                           ResourceOrIdArgument.getLootModifier($$0x, "modifier")
                                                                        )
                                                                     )
                                                               )
                                                         )
                                                   )
                                             )
                                       )
                                 )
                           )
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("modify")
                     .then(
                        Commands.literal("block")
                           .then(
                              Commands.argument("pos", BlockPosArgument.blockPos())
                                 .then(
                                    Commands.argument("slot", SlotArgument.slot())
                                       .then(
                                          Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1))
                                             .executes(
                                                $$0x -> modifyBlockItem(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                   SlotArgument.getSlot($$0x, "slot"),
                                                   ResourceOrIdArgument.getLootModifier($$0x, "modifier")
                                                )
                                             )
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("entity")
                        .then(
                           Commands.argument("targets", EntityArgument.entities())
                              .then(
                                 Commands.argument("slot", SlotArgument.slot())
                                    .then(
                                       Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1))
                                          .executes(
                                             $$0x -> modifyEntityItem(
                                                (CommandSourceStack)$$0x.getSource(),
                                                EntityArgument.getEntities($$0x, "targets"),
                                                SlotArgument.getSlot($$0x, "slot"),
                                                ResourceOrIdArgument.getLootModifier($$0x, "modifier")
                                             )
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static int modifyBlockItem(CommandSourceStack $$0, BlockPos $$1, int $$2, Holder<LootItemFunction> $$3) throws CommandSyntaxException {
      Container $$4 = getContainer($$0, $$1, ERROR_TARGET_NOT_A_CONTAINER);
      if ($$2 >= 0 && $$2 < $$4.getContainerSize()) {
         ItemStack $$5 = applyModifier($$0, $$3, $$4.getItem($$2));
         $$4.setItem($$2, $$5);
         $$0.sendSuccess(
            () -> Component.translatable("commands.item.block.set.success", new Object[]{$$1.getX(), $$1.getY(), $$1.getZ(), $$5.getDisplayName()}), true
         );
         return 1;
      } else {
         throw ERROR_TARGET_INAPPLICABLE_SLOT.create($$2);
      }
   }

   private static int modifyEntityItem(CommandSourceStack $$0, Collection<? extends Entity> $$1, int $$2, Holder<LootItemFunction> $$3) throws CommandSyntaxException {
      Map<Entity, ItemStack> $$4 = Maps.newHashMapWithExpectedSize($$1.size());

      for (Entity $$5 : $$1) {
         SlotAccess $$6 = $$5.getSlot($$2);
         if ($$6 != null) {
            ItemStack $$7 = applyModifier($$0, $$3, $$6.get().copy());
            if ($$6.set($$7)) {
               $$4.put($$5, $$7);
               if ($$5 instanceof ServerPlayer $$8) {
                  $$8.containerMenu.broadcastChanges();
               }
            }
         }
      }

      if ($$4.isEmpty()) {
         throw ERROR_TARGET_NO_CHANGES.create($$2);
      } else {
         if ($$4.size() == 1) {
            Entry<Entity, ItemStack> $$9 = $$4.entrySet().iterator().next();
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.item.entity.set.success.single", new Object[]{$$9.getKey().getDisplayName(), $$9.getValue().getDisplayName()}
               ),
               true
            );
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", new Object[]{$$4.size()}), true);
         }

         return $$4.size();
      }
   }

   private static int setBlockItem(CommandSourceStack $$0, BlockPos $$1, int $$2, ItemStack $$3) throws CommandSyntaxException {
      Container $$4 = getContainer($$0, $$1, ERROR_TARGET_NOT_A_CONTAINER);
      if ($$2 >= 0 && $$2 < $$4.getContainerSize()) {
         $$4.setItem($$2, $$3);
         $$0.sendSuccess(
            () -> Component.translatable("commands.item.block.set.success", new Object[]{$$1.getX(), $$1.getY(), $$1.getZ(), $$3.getDisplayName()}), true
         );
         return 1;
      } else {
         throw ERROR_TARGET_INAPPLICABLE_SLOT.create($$2);
      }
   }

   static Container getContainer(CommandSourceStack $$0, BlockPos $$1, Dynamic3CommandExceptionType $$2) throws CommandSyntaxException {
      if ($$0.getLevel().getBlockEntity($$1) instanceof Container $$4) {
         return $$4;
      } else {
         throw $$2.create($$1.getX(), $$1.getY(), $$1.getZ());
      }
   }

   private static int setEntityItem(CommandSourceStack $$0, Collection<? extends Entity> $$1, int $$2, ItemStack $$3) throws CommandSyntaxException {
      List<Entity> $$4 = Lists.newArrayListWithCapacity($$1.size());

      for (Entity $$5 : $$1) {
         SlotAccess $$6 = $$5.getSlot($$2);
         if ($$6 != null && $$6.set($$3.copy())) {
            $$4.add($$5);
            if ($$5 instanceof ServerPlayer $$7) {
               $$7.containerMenu.broadcastChanges();
            }
         }
      }

      if ($$4.isEmpty()) {
         throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create($$3.getDisplayName(), $$2);
      } else {
         if ($$4.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable("commands.item.entity.set.success.single", new Object[]{$$4.getFirst().getDisplayName(), $$3.getDisplayName()}),
               true
            );
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", new Object[]{$$4.size(), $$3.getDisplayName()}), true);
         }

         return $$4.size();
      }
   }

   private static int blockToEntities(CommandSourceStack $$0, BlockPos $$1, int $$2, Collection<? extends Entity> $$3, int $$4) throws CommandSyntaxException {
      return setEntityItem($$0, $$3, $$4, getBlockItem($$0, $$1, $$2));
   }

   private static int blockToEntities(CommandSourceStack $$0, BlockPos $$1, int $$2, Collection<? extends Entity> $$3, int $$4, Holder<LootItemFunction> $$5) throws CommandSyntaxException {
      return setEntityItem($$0, $$3, $$4, applyModifier($$0, $$5, getBlockItem($$0, $$1, $$2)));
   }

   private static int blockToBlock(CommandSourceStack $$0, BlockPos $$1, int $$2, BlockPos $$3, int $$4) throws CommandSyntaxException {
      return setBlockItem($$0, $$3, $$4, getBlockItem($$0, $$1, $$2));
   }

   private static int blockToBlock(CommandSourceStack $$0, BlockPos $$1, int $$2, BlockPos $$3, int $$4, Holder<LootItemFunction> $$5) throws CommandSyntaxException {
      return setBlockItem($$0, $$3, $$4, applyModifier($$0, $$5, getBlockItem($$0, $$1, $$2)));
   }

   private static int entityToBlock(CommandSourceStack $$0, Entity $$1, int $$2, BlockPos $$3, int $$4) throws CommandSyntaxException {
      return setBlockItem($$0, $$3, $$4, getItemInSlot($$1, $$2));
   }

   private static int entityToBlock(CommandSourceStack $$0, Entity $$1, int $$2, BlockPos $$3, int $$4, Holder<LootItemFunction> $$5) throws CommandSyntaxException {
      return setBlockItem($$0, $$3, $$4, applyModifier($$0, $$5, getItemInSlot($$1, $$2)));
   }

   private static int entityToEntities(CommandSourceStack $$0, Entity $$1, int $$2, Collection<? extends Entity> $$3, int $$4) throws CommandSyntaxException {
      return setEntityItem($$0, $$3, $$4, getItemInSlot($$1, $$2));
   }

   private static int entityToEntities(CommandSourceStack $$0, Entity $$1, int $$2, Collection<? extends Entity> $$3, int $$4, Holder<LootItemFunction> $$5) throws CommandSyntaxException {
      return setEntityItem($$0, $$3, $$4, applyModifier($$0, $$5, getItemInSlot($$1, $$2)));
   }

   private static ItemStack applyModifier(CommandSourceStack $$0, Holder<LootItemFunction> $$1, ItemStack $$2) {
      ServerLevel $$3 = $$0.getLevel();
      LootParams $$4 = new Builder($$3)
         .withParameter(LootContextParams.ORIGIN, $$0.getPosition())
         .withOptionalParameter(LootContextParams.THIS_ENTITY, $$0.getEntity())
         .create(LootContextParamSets.COMMAND);
      LootContext $$5 = new net.minecraft.world.level.storage.loot.LootContext.Builder($$4).create(Optional.empty());
      $$5.pushVisitedElement(LootContext.createVisitedEntry((LootItemFunction)$$1.value()));
      ItemStack $$6 = (ItemStack)((LootItemFunction)$$1.value()).apply($$2, $$5);
      $$6.limitSize($$6.getMaxStackSize());
      return $$6;
   }

   private static ItemStack getItemInSlot(SlotProvider $$0, int $$1) throws CommandSyntaxException {
      SlotAccess $$2 = $$0.getSlot($$1);
      if ($$2 == null) {
         throw ERROR_SOURCE_INAPPLICABLE_SLOT.create($$1);
      } else {
         return $$2.get().copy();
      }
   }

   private static ItemStack getBlockItem(CommandSourceStack $$0, BlockPos $$1, int $$2) throws CommandSyntaxException {
      Container $$3 = getContainer($$0, $$1, ERROR_SOURCE_NOT_A_CONTAINER);
      return getItemInSlot($$3, $$2);
   }
}
