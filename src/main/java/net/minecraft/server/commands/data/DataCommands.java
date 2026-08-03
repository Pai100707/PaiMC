package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.PrimitiveTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DataCommands {
   private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.data.merge.failed"));
   private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.data.get.invalid", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.data.get.unknown", new Object[]{$$0})
   );
   private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(Component.translatable("commands.data.get.multiple"));
   private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.data.modify.expected_object", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.data.modify.expected_value", new Object[]{$$0})
   );
   private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.data.modify.invalid_substring", new Object[]{$$0, $$1})
   );
   public static final List<Function<String, DataCommands.DataProvider>> ALL_PROVIDERS = ImmutableList.of(
      EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER
   );
   public static final List<DataCommands.DataProvider> TARGET_PROVIDERS = ALL_PROVIDERS.stream()
      .map($$0 -> $$0.apply("target"))
      .collect(ImmutableList.toImmutableList());
   public static final List<DataCommands.DataProvider> SOURCE_PROVIDERS = ALL_PROVIDERS.stream()
      .map($$0 -> $$0.apply("source"))
      .collect(ImmutableList.toImmutableList());

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      LiteralArgumentBuilder<CommandSourceStack> $$1 = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("data")
         .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

      for (DataCommands.DataProvider $$2 : TARGET_PROVIDERS) {
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$1.then(
                     $$2.wrap(
                        Commands.literal("merge"),
                        $$1x -> $$1x.then(
                           Commands.argument("nbt", CompoundTagArgument.compoundTag())
                              .executes(
                                 $$1xx -> mergeData((CommandSourceStack)$$1xx.getSource(), $$2.access($$1xx), CompoundTagArgument.getCompoundTag($$1xx, "nbt"))
                              )
                        )
                     )
                  ))
                  .then(
                     $$2.wrap(
                        Commands.literal("get"),
                        $$1x -> $$1x.executes($$1xx -> getData((CommandSourceStack)$$1xx.getSource(), $$2.access($$1xx)))
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath())
                                    .executes(
                                       $$1xx -> getData((CommandSourceStack)$$1xx.getSource(), $$2.access($$1xx), NbtPathArgument.getPath($$1xx, "path"))
                                    ))
                                 .then(
                                    Commands.argument("scale", DoubleArgumentType.doubleArg())
                                       .executes(
                                          $$1xx -> getNumeric(
                                             (CommandSourceStack)$$1xx.getSource(),
                                             $$2.access($$1xx),
                                             NbtPathArgument.getPath($$1xx, "path"),
                                             DoubleArgumentType.getDouble($$1xx, "scale")
                                          )
                                       )
                                 )
                           )
                     )
                  ))
               .then(
                  $$2.wrap(
                     Commands.literal("remove"),
                     $$1x -> $$1x.then(
                        Commands.argument("path", NbtPathArgument.nbtPath())
                           .executes($$1xx -> removeData((CommandSourceStack)$$1xx.getSource(), $$2.access($$1xx), NbtPathArgument.getPath($$1xx, "path")))
                     )
                  )
               ))
            .then(
               decorateModification(
                  ($$0x, $$1x) -> $$0x.then(
                        Commands.literal("insert")
                           .then(
                              Commands.argument("index", IntegerArgumentType.integer())
                                 .then($$1x.create(($$0xx, $$1xx, $$2x, $$3) -> $$2x.insert(IntegerArgumentType.getInteger($$0xx, "index"), $$1xx, $$3)))
                           )
                     )
                     .then(Commands.literal("prepend").then($$1x.create(($$0xx, $$1xx, $$2x, $$3) -> $$2x.insert(0, $$1xx, $$3))))
                     .then(Commands.literal("append").then($$1x.create(($$0xx, $$1xx, $$2x, $$3) -> $$2x.insert(-1, $$1xx, $$3))))
                     .then(Commands.literal("set").then($$1x.create(($$0xx, $$1xx, $$2x, $$3) -> $$2x.set($$1xx, (Tag)Iterables.getLast($$3)))))
                     .then(Commands.literal("merge").then($$1x.create(($$0xx, $$1xx, $$2x, $$3) -> {
                        CompoundTag $$4 = new CompoundTag();

                        for (Tag $$5 : $$3) {
                           if (NbtPath.isTooDeep($$5, 0)) {
                              throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                           }

                           if (!($$5 instanceof CompoundTag $$6)) {
                              throw ERROR_EXPECTED_OBJECT.create($$5);
                           }

                           $$4.merge($$6);
                        }

                        Collection<Tag> $$7 = $$2x.getOrCreate($$1xx, CompoundTag::new);
                        int $$8 = 0;

                        for (Tag $$9 : $$7) {
                           if (!($$9 instanceof CompoundTag $$10)) {
                              throw ERROR_EXPECTED_OBJECT.create($$9);
                           }

                           CompoundTag $$12 = $$10.copy();
                           $$10.merge($$4);
                           $$8 += $$12.equals($$10) ? 0 : 1;
                        }

                        return $$8;
                     })))
               )
            );
      }

      $$0.register($$1);
   }

   private static String getAsText(Tag $$0) throws CommandSyntaxException {
      return switch ($$0) {
         case StringTag var3 -> {
            StringTag var8 = var3;

            try {
               var9 = var8.value();
            } catch (Throwable var6) {
               throw new MatchException(var6.toString(), var6);
            }

            String var7 = var9;
            yield var7;
         }
         case PrimitiveTag $$2 -> $$2.toString();
         default -> throw ERROR_EXPECTED_VALUE.create($$0);
      };
   }

   private static List<Tag> stringifyTagList(List<Tag> $$0, DataCommands.StringProcessor $$1) throws CommandSyntaxException {
      List<Tag> $$2 = new ArrayList<>($$0.size());

      for (Tag $$3 : $$0) {
         String $$4 = getAsText($$3);
         $$2.add(StringTag.valueOf($$1.process($$4)));
      }

      return $$2;
   }

   private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(
      BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataCommands.DataManipulatorDecorator> $$0
   ) {
      LiteralArgumentBuilder<CommandSourceStack> $$1 = Commands.literal("modify");

      for (DataCommands.DataProvider $$2 : TARGET_PROVIDERS) {
         $$2.wrap(
            $$1,
            $$2x -> {
               ArgumentBuilder<CommandSourceStack, ?> $$3 = Commands.argument("targetPath", NbtPathArgument.nbtPath());

               for (DataCommands.DataProvider $$4 : SOURCE_PROVIDERS) {
                  $$0.accept(
                     $$3,
                     $$2xx -> $$4.wrap(
                        Commands.literal("from"),
                        $$3x -> $$3x.executes($$3xx -> manipulateData($$3xx, $$2, $$2xx, getSingletonSource($$3xx, $$4)))
                           .then(
                              Commands.argument("sourcePath", NbtPathArgument.nbtPath())
                                 .executes($$3xx -> manipulateData($$3xx, $$2, $$2xx, resolveSourcePath($$3xx, $$4)))
                           )
                     )
                  );
                  $$0.accept(
                     $$3,
                     $$2xx -> $$4.wrap(
                        Commands.literal("string"),
                        $$3x -> $$3x.executes(
                              $$3xx -> manipulateData($$3xx, $$2, $$2xx, stringifyTagList(getSingletonSource($$3xx, $$4), $$0xxxxx -> $$0xxxxx))
                           )
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("sourcePath", NbtPathArgument.nbtPath())
                                    .executes($$3xx -> manipulateData($$3xx, $$2, $$2xx, stringifyTagList(resolveSourcePath($$3xx, $$4), $$0xxxxx -> $$0xxxxx))))
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("start", IntegerArgumentType.integer())
                                          .executes(
                                             $$3xx -> manipulateData(
                                                $$3xx,
                                                $$2,
                                                $$2xx,
                                                stringifyTagList(
                                                   resolveSourcePath($$3xx, $$4),
                                                   $$1xxxxx -> substring($$1xxxxx, IntegerArgumentType.getInteger($$3xx, "start"))
                                                )
                                             )
                                          ))
                                       .then(
                                          Commands.argument("end", IntegerArgumentType.integer())
                                             .executes(
                                                $$3xx -> manipulateData(
                                                   $$3xx,
                                                   $$2,
                                                   $$2xx,
                                                   stringifyTagList(
                                                      resolveSourcePath($$3xx, $$4),
                                                      $$1xxxxx -> substring(
                                                         $$1xxxxx, IntegerArgumentType.getInteger($$3xx, "start"), IntegerArgumentType.getInteger($$3xx, "end")
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

               $$0.accept($$3, $$1xx -> Commands.literal("value").then(Commands.argument("value", NbtTagArgument.nbtTag()).executes($$2xx -> {
                  List<Tag> $$3x = Collections.singletonList(NbtTagArgument.getNbtTag($$2xx, "value"));
                  return manipulateData($$2xx, $$2, $$1xx, $$3x);
               })));
               return $$2x.then($$3);
            }
         );
      }

      return $$1;
   }

   private static String validatedSubstring(String $$0, int $$1, int $$2) throws CommandSyntaxException {
      if ($$1 >= 0 && $$2 <= $$0.length() && $$1 <= $$2) {
         return $$0.substring($$1, $$2);
      } else {
         throw ERROR_INVALID_SUBSTRING.create($$1, $$2);
      }
   }

   private static String substring(String $$0, int $$1, int $$2) throws CommandSyntaxException {
      int $$3 = $$0.length();
      int $$4 = getOffset($$1, $$3);
      int $$5 = getOffset($$2, $$3);
      return validatedSubstring($$0, $$4, $$5);
   }

   private static String substring(String $$0, int $$1) throws CommandSyntaxException {
      int $$2 = $$0.length();
      return validatedSubstring($$0, getOffset($$1, $$2), $$2);
   }

   private static int getOffset(int $$0, int $$1) {
      return $$0 >= 0 ? $$0 : $$1 + $$0;
   }

   private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> $$0, DataCommands.DataProvider $$1) throws CommandSyntaxException {
      DataAccessor $$2 = $$1.access($$0);
      return Collections.singletonList($$2.getData());
   }

   private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> $$0, DataCommands.DataProvider $$1) throws CommandSyntaxException {
      DataAccessor $$2 = $$1.access($$0);
      NbtPath $$3 = NbtPathArgument.getPath($$0, "sourcePath");
      return $$3.get($$2.getData());
   }

   private static int manipulateData(CommandContext<CommandSourceStack> $$0, DataCommands.DataProvider $$1, DataCommands.DataManipulator $$2, List<Tag> $$3) throws CommandSyntaxException {
      DataAccessor $$4 = $$1.access($$0);
      NbtPath $$5 = NbtPathArgument.getPath($$0, "targetPath");
      CompoundTag $$6 = $$4.getData();
      int $$7 = $$2.modify($$0, $$6, $$5, $$3);
      if ($$7 == 0) {
         throw ERROR_MERGE_UNCHANGED.create();
      } else {
         $$4.setData($$6);
         ((CommandSourceStack)$$0.getSource()).sendSuccess(() -> $$4.getModifiedSuccess(), true);
         return $$7;
      }
   }

   private static int removeData(CommandSourceStack $$0, DataAccessor $$1, NbtPath $$2) throws CommandSyntaxException {
      CompoundTag $$3 = $$1.getData();
      int $$4 = $$2.remove($$3);
      if ($$4 == 0) {
         throw ERROR_MERGE_UNCHANGED.create();
      } else {
         $$1.setData($$3);
         $$0.sendSuccess(() -> $$1.getModifiedSuccess(), true);
         return $$4;
      }
   }

   public static Tag getSingleTag(NbtPath $$0, DataAccessor $$1) throws CommandSyntaxException {
      Collection<Tag> $$2 = $$0.get($$1.getData());
      Iterator<Tag> $$3 = $$2.iterator();
      Tag $$4 = $$3.next();
      if ($$3.hasNext()) {
         throw ERROR_MULTIPLE_TAGS.create();
      } else {
         return $$4;
      }
   }

   private static int getData(CommandSourceStack $$0, DataAccessor $$1, NbtPath $$2) throws CommandSyntaxException {
      Tag $$3 = getSingleTag($$2, $$1);

      int $$9 = switch ($$3) {
         case NumericTag $$4 -> Mth.floor($$4.doubleValue());
         case CollectionTag $$5 -> $$5.size();
         case CompoundTag $$6 -> $$6.size();
         case StringTag var10 -> {
            StringTag var10000 = var10;

            try {
               var15 = var10000.value();
            } catch (Throwable var13) {
               throw new MatchException(var13.toString(), var13);
            }

            String var14 = var15;
            yield var14.length();
         }
         case EndTag $$8 -> throw ERROR_GET_NON_EXISTENT.create($$2.toString());
         default -> throw new MatchException(null, null);
      };
      $$0.sendSuccess(() -> $$1.getPrintSuccess($$3), false);
      return $$9;
   }

   private static int getNumeric(CommandSourceStack $$0, DataAccessor $$1, NbtPath $$2, double $$3) throws CommandSyntaxException {
      Tag $$4 = getSingleTag($$2, $$1);
      if (!($$4 instanceof NumericTag)) {
         throw ERROR_GET_NOT_NUMBER.create($$2.toString());
      } else {
         int $$5 = Mth.floor(((NumericTag)$$4).doubleValue() * $$3);
         $$0.sendSuccess(() -> $$1.getPrintSuccess($$2, $$3, $$5), false);
         return $$5;
      }
   }

   private static int getData(CommandSourceStack $$0, DataAccessor $$1) throws CommandSyntaxException {
      CompoundTag $$2 = $$1.getData();
      $$0.sendSuccess(() -> $$1.getPrintSuccess($$2), false);
      return 1;
   }

   private static int mergeData(CommandSourceStack $$0, DataAccessor $$1, CompoundTag $$2) throws CommandSyntaxException {
      CompoundTag $$3 = $$1.getData();
      if (NbtPath.isTooDeep($$2, 0)) {
         throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
      } else {
         CompoundTag $$4 = $$3.copy().merge($$2);
         if ($$3.equals($$4)) {
            throw ERROR_MERGE_UNCHANGED.create();
         } else {
            $$1.setData($$4);
            $$0.sendSuccess(() -> $$1.getModifiedSuccess(), true);
            return 1;
         }
      }
   }

   @FunctionalInterface
   interface DataManipulator {
      int modify(CommandContext<CommandSourceStack> var1, CompoundTag var2, NbtPath var3, List<Tag> var4) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface DataManipulatorDecorator {
      ArgumentBuilder<CommandSourceStack, ?> create(DataCommands.DataManipulator var1);
   }

   public interface DataProvider {
      DataAccessor access(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

      ArgumentBuilder<CommandSourceStack, ?> wrap(
         ArgumentBuilder<CommandSourceStack, ?> var1, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> var2
      );
   }

   @FunctionalInterface
   interface StringProcessor {
      String process(String var1) throws CommandSyntaxException;
   }
}
