package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantCommand {
   private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.enchant.failed.entity", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.enchant.failed.itemless", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.enchant.failed.incompatible", new Object[]{$$0})
   );
   private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.enchant.failed.level", new Object[]{$$0, $$1})
   );
   private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(Component.translatable("commands.enchant.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("enchant").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("targets", EntityArgument.entities())
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("enchantment", ResourceArgument.resource($$1, Registries.ENCHANTMENT))
                           .executes(
                              $$0x -> enchant(
                                 (CommandSourceStack)$$0x.getSource(),
                                 EntityArgument.getEntities($$0x, "targets"),
                                 ResourceArgument.getEnchantment($$0x, "enchantment"),
                                 1
                              )
                           ))
                        .then(
                           Commands.argument("level", IntegerArgumentType.integer(0))
                              .executes(
                                 $$0x -> enchant(
                                    (CommandSourceStack)$$0x.getSource(),
                                    EntityArgument.getEntities($$0x, "targets"),
                                    ResourceArgument.getEnchantment($$0x, "enchantment"),
                                    IntegerArgumentType.getInteger($$0x, "level")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int enchant(CommandSourceStack $$0, Collection<? extends Entity> $$1, Holder<Enchantment> $$2, int $$3) throws CommandSyntaxException {
      Enchantment $$4 = (Enchantment)$$2.value();
      if ($$3 > $$4.getMaxLevel()) {
         throw ERROR_LEVEL_TOO_HIGH.create($$3, $$4.getMaxLevel());
      } else {
         int $$5 = 0;

         for (Entity $$6 : $$1) {
            if ($$6 instanceof LivingEntity $$7) {
               ItemStack $$8 = $$7.getMainHandItem();
               if (!$$8.isEmpty()) {
                  if ($$4.canEnchant($$8) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantmentsForCrafting($$8).keySet(), $$2)) {
                     $$8.enchant($$2, $$3);
                     $$5++;
                  } else if ($$1.size() == 1) {
                     throw ERROR_INCOMPATIBLE.create($$8.getHoverName().getString());
                  }
               } else if ($$1.size() == 1) {
                  throw ERROR_NO_ITEM.create($$7.getName().getString());
               }
            } else if ($$1.size() == 1) {
               throw ERROR_NOT_LIVING_ENTITY.create($$6.getName().getString());
            }
         }

         if ($$5 == 0) {
            throw ERROR_NOTHING_HAPPENED.create();
         } else {
            if ($$1.size() == 1) {
               $$0.sendSuccess(
                  () -> Component.translatable(
                     "commands.enchant.success.single", new Object[]{Enchantment.getFullname($$2, $$3), $$1.iterator().next().getDisplayName()}
                  ),
                  true
               );
            } else {
               $$0.sendSuccess(
                  () -> Component.translatable("commands.enchant.success.multiple", new Object[]{Enchantment.getFullname($$2, $$3), $$1.size()}), true
               );
            }

            return $$5;
         }
      }
   }
}
