package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class AttributeCommand {
   private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.attribute.failed.entity", new Object[]{$$0})
   );
   private static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ATTRIBUTE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.attribute.failed.no_attribute", new Object[]{$$0, $$1})
   );
   private static final Dynamic3CommandExceptionType ERROR_NO_SUCH_MODIFIER = new Dynamic3CommandExceptionType(
      ($$0, $$1, $$2) -> Component.translatableEscape("commands.attribute.failed.no_modifier", new Object[]{$$1, $$0, $$2})
   );
   private static final Dynamic3CommandExceptionType ERROR_MODIFIER_ALREADY_PRESENT = new Dynamic3CommandExceptionType(
      ($$0, $$1, $$2) -> Component.translatableEscape("commands.attribute.failed.modifier_already_present", new Object[]{$$2, $$1, $$0})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("attribute").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("target", EntityArgument.entity())
                  .then(
                     ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("attribute", ResourceArgument.resource($$1, Registries.ATTRIBUTE))
                              .then(
                                 ((LiteralArgumentBuilder)Commands.literal("get")
                                       .executes(
                                          $$0x -> getAttributeValue(
                                             (CommandSourceStack)$$0x.getSource(),
                                             EntityArgument.getEntity($$0x, "target"),
                                             ResourceArgument.getAttribute($$0x, "attribute"),
                                             1.0
                                          )
                                       ))
                                    .then(
                                       Commands.argument("scale", DoubleArgumentType.doubleArg())
                                          .executes(
                                             $$0x -> getAttributeValue(
                                                (CommandSourceStack)$$0x.getSource(),
                                                EntityArgument.getEntity($$0x, "target"),
                                                ResourceArgument.getAttribute($$0x, "attribute"),
                                                DoubleArgumentType.getDouble($$0x, "scale")
                                             )
                                          )
                                    )
                              ))
                           .then(
                              ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("base")
                                       .then(
                                          Commands.literal("set")
                                             .then(
                                                Commands.argument("value", DoubleArgumentType.doubleArg())
                                                   .executes(
                                                      $$0x -> setAttributeBase(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         EntityArgument.getEntity($$0x, "target"),
                                                         ResourceArgument.getAttribute($$0x, "attribute"),
                                                         DoubleArgumentType.getDouble($$0x, "value")
                                                      )
                                                   )
                                             )
                                       ))
                                    .then(
                                       ((LiteralArgumentBuilder)Commands.literal("get")
                                             .executes(
                                                $$0x -> getAttributeBase(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   EntityArgument.getEntity($$0x, "target"),
                                                   ResourceArgument.getAttribute($$0x, "attribute"),
                                                   1.0
                                                )
                                             ))
                                          .then(
                                             Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                .executes(
                                                   $$0x -> getAttributeBase(
                                                      (CommandSourceStack)$$0x.getSource(),
                                                      EntityArgument.getEntity($$0x, "target"),
                                                      ResourceArgument.getAttribute($$0x, "attribute"),
                                                      DoubleArgumentType.getDouble($$0x, "scale")
                                                   )
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("reset")
                                       .executes(
                                          $$0x -> resetAttributeBase(
                                             (CommandSourceStack)$$0x.getSource(),
                                             EntityArgument.getEntity($$0x, "target"),
                                             ResourceArgument.getAttribute($$0x, "attribute")
                                          )
                                       )
                                 )
                           ))
                        .then(
                           ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("modifier")
                                    .then(
                                       Commands.literal("add")
                                          .then(
                                             Commands.argument("id", IdentifierArgument.id())
                                                .then(
                                                   ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                               "value", DoubleArgumentType.doubleArg()
                                                            )
                                                            .then(
                                                               Commands.literal("add_value")
                                                                  .executes(
                                                                     $$0x -> addModifier(
                                                                        (CommandSourceStack)$$0x.getSource(),
                                                                        EntityArgument.getEntity($$0x, "target"),
                                                                        ResourceArgument.getAttribute($$0x, "attribute"),
                                                                        IdentifierArgument.getId($$0x, "id"),
                                                                        DoubleArgumentType.getDouble($$0x, "value"),
                                                                        Operation.ADD_VALUE
                                                                     )
                                                                  )
                                                            ))
                                                         .then(
                                                            Commands.literal("add_multiplied_base")
                                                               .executes(
                                                                  $$0x -> addModifier(
                                                                     (CommandSourceStack)$$0x.getSource(),
                                                                     EntityArgument.getEntity($$0x, "target"),
                                                                     ResourceArgument.getAttribute($$0x, "attribute"),
                                                                     IdentifierArgument.getId($$0x, "id"),
                                                                     DoubleArgumentType.getDouble($$0x, "value"),
                                                                     Operation.ADD_MULTIPLIED_BASE
                                                                  )
                                                               )
                                                         ))
                                                      .then(
                                                         Commands.literal("add_multiplied_total")
                                                            .executes(
                                                               $$0x -> addModifier(
                                                                  (CommandSourceStack)$$0x.getSource(),
                                                                  EntityArgument.getEntity($$0x, "target"),
                                                                  ResourceArgument.getAttribute($$0x, "attribute"),
                                                                  IdentifierArgument.getId($$0x, "id"),
                                                                  DoubleArgumentType.getDouble($$0x, "value"),
                                                                  Operation.ADD_MULTIPLIED_TOTAL
                                                               )
                                                            )
                                                      )
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("remove")
                                       .then(
                                          Commands.argument("id", IdentifierArgument.id())
                                             .suggests(
                                                ($$0x, $$1x) -> SharedSuggestionProvider.suggestResource(
                                                   getAttributeModifiers(
                                                      EntityArgument.getEntity($$0x, "target"), ResourceArgument.getAttribute($$0x, "attribute")
                                                   ),
                                                   $$1x
                                                )
                                             )
                                             .executes(
                                                $$0x -> removeModifier(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   EntityArgument.getEntity($$0x, "target"),
                                                   ResourceArgument.getAttribute($$0x, "attribute"),
                                                   IdentifierArgument.getId($$0x, "id")
                                                )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("value")
                                    .then(
                                       Commands.literal("get")
                                          .then(
                                             ((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id())
                                                   .suggests(
                                                      ($$0x, $$1x) -> SharedSuggestionProvider.suggestResource(
                                                         getAttributeModifiers(
                                                            EntityArgument.getEntity($$0x, "target"), ResourceArgument.getAttribute($$0x, "attribute")
                                                         ),
                                                         $$1x
                                                      )
                                                   )
                                                   .executes(
                                                      $$0x -> getAttributeModifier(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         EntityArgument.getEntity($$0x, "target"),
                                                         ResourceArgument.getAttribute($$0x, "attribute"),
                                                         IdentifierArgument.getId($$0x, "id"),
                                                         1.0
                                                      )
                                                   ))
                                                .then(
                                                   Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                      .executes(
                                                         $$0x -> getAttributeModifier(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            EntityArgument.getEntity($$0x, "target"),
                                                            ResourceArgument.getAttribute($$0x, "attribute"),
                                                            IdentifierArgument.getId($$0x, "id"),
                                                            DoubleArgumentType.getDouble($$0x, "scale")
                                                         )
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static AttributeInstance getAttributeInstance(Entity $$0, Holder<Attribute> $$1) throws CommandSyntaxException {
      AttributeInstance $$2 = getLivingEntity($$0).getAttributes().getInstance($$1);
      if ($$2 == null) {
         throw ERROR_NO_SUCH_ATTRIBUTE.create($$0.getName(), getAttributeDescription($$1));
      } else {
         return $$2;
      }
   }

   private static LivingEntity getLivingEntity(Entity $$0) throws CommandSyntaxException {
      if (!($$0 instanceof LivingEntity)) {
         throw ERROR_NOT_LIVING_ENTITY.create($$0.getName());
      } else {
         return (LivingEntity)$$0;
      }
   }

   private static LivingEntity getEntityWithAttribute(Entity $$0, Holder<Attribute> $$1) throws CommandSyntaxException {
      LivingEntity $$2 = getLivingEntity($$0);
      if (!$$2.getAttributes().hasAttribute($$1)) {
         throw ERROR_NO_SUCH_ATTRIBUTE.create($$0.getName(), getAttributeDescription($$1));
      } else {
         return $$2;
      }
   }

   private static int getAttributeValue(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, double $$3) throws CommandSyntaxException {
      LivingEntity $$4 = getEntityWithAttribute($$1, $$2);
      double $$5 = $$4.getAttributeValue($$2);
      $$0.sendSuccess(
         () -> Component.translatable("commands.attribute.value.get.success", new Object[]{getAttributeDescription($$2), $$1.getName(), $$5}), false
      );
      return (int)($$5 * $$3);
   }

   private static int getAttributeBase(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, double $$3) throws CommandSyntaxException {
      LivingEntity $$4 = getEntityWithAttribute($$1, $$2);
      double $$5 = $$4.getAttributeBaseValue($$2);
      $$0.sendSuccess(
         () -> Component.translatable("commands.attribute.base_value.get.success", new Object[]{getAttributeDescription($$2), $$1.getName(), $$5}), false
      );
      return (int)($$5 * $$3);
   }

   private static int getAttributeModifier(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, Identifier $$3, double $$4) throws CommandSyntaxException {
      LivingEntity $$5 = getEntityWithAttribute($$1, $$2);
      AttributeMap $$6 = $$5.getAttributes();
      if (!$$6.hasModifier($$2, $$3)) {
         throw ERROR_NO_SUCH_MODIFIER.create($$1.getName(), getAttributeDescription($$2), $$3);
      } else {
         double $$7 = $$6.getModifierValue($$2, $$3);
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.attribute.modifier.value.get.success", new Object[]{Component.translationArg($$3), getAttributeDescription($$2), $$1.getName(), $$7}
            ),
            false
         );
         return (int)($$7 * $$4);
      }
   }

   private static Stream<Identifier> getAttributeModifiers(Entity $$0, Holder<Attribute> $$1) throws CommandSyntaxException {
      AttributeInstance $$2 = getAttributeInstance($$0, $$1);
      return $$2.getModifiers().stream().map(AttributeModifier::id);
   }

   private static int setAttributeBase(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, double $$3) throws CommandSyntaxException {
      getAttributeInstance($$1, $$2).setBaseValue($$3);
      $$0.sendSuccess(
         () -> Component.translatable("commands.attribute.base_value.set.success", new Object[]{getAttributeDescription($$2), $$1.getName(), $$3}), false
      );
      return 1;
   }

   private static int resetAttributeBase(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2) throws CommandSyntaxException {
      LivingEntity $$3 = getLivingEntity($$1);
      if (!$$3.getAttributes().resetBaseValue($$2)) {
         throw ERROR_NO_SUCH_ATTRIBUTE.create($$1.getName(), getAttributeDescription($$2));
      } else {
         double $$4 = $$3.getAttributeBaseValue($$2);
         $$0.sendSuccess(
            () -> Component.translatable("commands.attribute.base_value.reset.success", new Object[]{getAttributeDescription($$2), $$1.getName(), $$4}), false
         );
         return 1;
      }
   }

   private static int addModifier(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, Identifier $$3, double $$4, Operation $$5) throws CommandSyntaxException {
      AttributeInstance $$6 = getAttributeInstance($$1, $$2);
      AttributeModifier $$7 = new AttributeModifier($$3, $$4, $$5);
      if ($$6.hasModifier($$3)) {
         throw ERROR_MODIFIER_ALREADY_PRESENT.create($$1.getName(), getAttributeDescription($$2), $$3);
      } else {
         $$6.addPermanentModifier($$7);
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.attribute.modifier.add.success", new Object[]{Component.translationArg($$3), getAttributeDescription($$2), $$1.getName()}
            ),
            false
         );
         return 1;
      }
   }

   private static int removeModifier(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, Identifier $$3) throws CommandSyntaxException {
      AttributeInstance $$4 = getAttributeInstance($$1, $$2);
      if ($$4.removeModifier($$3)) {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.attribute.modifier.remove.success", new Object[]{Component.translationArg($$3), getAttributeDescription($$2), $$1.getName()}
            ),
            false
         );
         return 1;
      } else {
         throw ERROR_NO_SUCH_MODIFIER.create($$1.getName(), getAttributeDescription($$2), $$3);
      }
   }

   private static Component getAttributeDescription(Holder<Attribute> $$0) {
      return Component.translatable(((Attribute)$$0.value()).getDescriptionId());
   }
}
