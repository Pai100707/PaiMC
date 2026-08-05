/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterables
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
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
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.PrimitiveTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.commands.data.StorageDataAccessor;
import net.minecraft.util.Mth;

public class DataCommands {
    private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType((Message)Component.translatable("commands.data.merge.failed"));
    private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.data.get.invalid", $$0));
    private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.data.get.unknown", $$0));
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType((Message)Component.translatable("commands.data.get.multiple"));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.data.modify.expected_object", $$0));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.data.modify.expected_value", $$0));
    private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.data.modify.invalid_substring", $$0, $$1));
    public static final List<Function<String, DataProvider>> ALL_PROVIDERS = ImmutableList.of(EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER);
    public static final List<DataProvider> TARGET_PROVIDERS = (List)ALL_PROVIDERS.stream().map($$0 -> (DataProvider)$$0.apply("target")).collect(ImmutableList.toImmutableList());
    public static final List<DataProvider> SOURCE_PROVIDERS = (List)ALL_PROVIDERS.stream().map($$0 -> (DataProvider)$$0.apply("source")).collect(ImmutableList.toImmutableList());

    public static void register(CommandDispatcher<CommandSourceStack> $$0) {
        LiteralArgumentBuilder $$1 = (LiteralArgumentBuilder)Commands.literal("data").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (DataProvider $$2 : TARGET_PROVIDERS) {
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$1.then($$2.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("merge"), $$12 -> $$12.then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes($$1 -> DataCommands.mergeData((CommandSourceStack)$$1.getSource(), $$2.access((CommandContext<CommandSourceStack>)$$1), CompoundTagArgument.getCompoundTag($$1, "nbt"))))))).then($$2.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("get"), $$12 -> $$12.executes($$1 -> DataCommands.getData((CommandSourceStack)$$1.getSource(), $$2.access((CommandContext<CommandSourceStack>)$$1))).then(((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).executes($$1 -> DataCommands.getData((CommandSourceStack)$$1.getSource(), $$2.access((CommandContext<CommandSourceStack>)$$1), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$1, "path")))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes($$1 -> DataCommands.getNumeric((CommandSourceStack)$$1.getSource(), $$2.access((CommandContext<CommandSourceStack>)$$1), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$1, "path"), DoubleArgumentType.getDouble((CommandContext)$$1, (String)"scale")))))))).then($$2.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("remove"), $$12 -> $$12.then(Commands.argument("path", NbtPathArgument.nbtPath()).executes($$1 -> DataCommands.removeData((CommandSourceStack)$$1.getSource(), $$2.access((CommandContext<CommandSourceStack>)$$1), NbtPathArgument.getPath((CommandContext<CommandSourceStack>)$$1, "path"))))))).then(DataCommands.decorateModification(($$02, $$12) -> $$02.then(Commands.literal("insert").then(Commands.argument("index", IntegerArgumentType.integer()).then($$12.create(($$0, $$1, $$2, $$3) -> $$2.insert(IntegerArgumentType.getInteger((CommandContext)$$0, (String)"index"), $$1, $$3))))).then(Commands.literal("prepend").then($$12.create(($$0, $$1, $$2, $$3) -> $$2.insert(0, $$1, $$3)))).then(Commands.literal("append").then($$12.create(($$0, $$1, $$2, $$3) -> $$2.insert(-1, $$1, $$3)))).then(Commands.literal("set").then($$12.create(($$0, $$1, $$2, $$3) -> $$2.set($$1, (Tag)Iterables.getLast((Iterable)$$3))))).then(Commands.literal("merge").then($$12.create(($$0, $$1, $$2, $$3) -> {
                CompoundTag $$4 = new CompoundTag();
                for (Tag $$5 : $$3) {
                    if (NbtPathArgument.NbtPath.isTooDeep($$5, 0)) {
                        throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                    }
                    if ($$5 instanceof CompoundTag) {
                        CompoundTag $$6 = (CompoundTag)$$5;
                        $$4.merge($$6);
                        continue;
                    }
                    throw ERROR_EXPECTED_OBJECT.create((Object)$$5);
                }
                List<Tag> $$7 = $$2.getOrCreate($$1, CompoundTag::new);
                int $$8 = 0;
                for (Tag $$9 : $$7) {
                    void $$11;
                    if (!($$9 instanceof CompoundTag)) {
                        throw ERROR_EXPECTED_OBJECT.create((Object)$$9);
                    }
                    CompoundTag $$10 = (CompoundTag)$$9;
                    CompoundTag $$12 = $$11.copy();
                    $$11.merge($$4);
                    $$8 += $$12.equals($$11) ? 0 : 1;
                }
                return $$8;
            })))));
        }
        $$0.register($$1);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String getAsText(Tag $$0) throws CommandSyntaxException {
        Tag tag = $$0;
        Objects.requireNonNull(tag);
        Tag tag2 = tag;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{StringTag.class, PrimitiveTag.class}, (Object)tag2, n)) {
            case 0: {
                String string;
                StringTag stringTag = (StringTag)tag2;
                try {
                    String string2;
                    String $$1;
                    string = $$1 = (string2 = stringTag.value());
                    return string;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: {
                PrimitiveTag $$2 = (PrimitiveTag)tag2;
                String string = $$2.toString();
                return string;
            }
        }
        throw ERROR_EXPECTED_VALUE.create((Object)$$0);
    }

    private static List<Tag> stringifyTagList(List<Tag> $$0, StringProcessor $$1) throws CommandSyntaxException {
        ArrayList<Tag> $$2 = new ArrayList<Tag>($$0.size());
        for (Tag $$3 : $$0) {
            String $$4 = DataCommands.getAsText($$3);
            $$2.add(StringTag.valueOf($$1.process($$4)));
        }
        return $$2;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataManipulatorDecorator> $$0) {
        LiteralArgumentBuilder<CommandSourceStack> $$1 = Commands.literal("modify");
        for (DataProvider $$2 : TARGET_PROVIDERS) {
            $$2.wrap((ArgumentBuilder<CommandSourceStack, ?>)$$1, $$22 -> {
                RequiredArgumentBuilder<CommandSourceStack, NbtPathArgument.NbtPath> $$3 = Commands.argument("targetPath", NbtPathArgument.nbtPath());
                for (DataProvider $$4 : SOURCE_PROVIDERS) {
                    $$0.accept((ArgumentBuilder<CommandSourceStack, ?>)$$3, $$2 -> $$4.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("from"), $$32 -> $$32.executes($$3 -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)$$3, $$2, $$2, DataCommands.getSingletonSource((CommandContext<CommandSourceStack>)$$3, $$4))).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes($$3 -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)$$3, $$2, $$2, DataCommands.resolveSourcePath((CommandContext<CommandSourceStack>)$$3, $$4))))));
                    $$0.accept((ArgumentBuilder<CommandSourceStack, ?>)$$3, $$2 -> $$4.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("string"), $$32 -> $$32.executes($$3 -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)$$3, $$2, $$2, DataCommands.stringifyTagList(DataCommands.getSingletonSource((CommandContext<CommandSourceStack>)$$3, $$4), $$0 -> $$0))).then(((RequiredArgumentBuilder)Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes($$3 -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)$$3, $$2, $$2, DataCommands.stringifyTagList(DataCommands.resolveSourcePath((CommandContext<CommandSourceStack>)$$3, $$4), $$0 -> $$0)))).then(((RequiredArgumentBuilder)Commands.argument("start", IntegerArgumentType.integer()).executes($$3 -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)$$3, $$2, $$2, DataCommands.stringifyTagList(DataCommands.resolveSourcePath((CommandContext<CommandSourceStack>)$$3, $$4), $$1 -> DataCommands.substring($$1, IntegerArgumentType.getInteger((CommandContext)$$3, (String)"start")))))).then(Commands.argument("end", IntegerArgumentType.integer()).executes($$3 -> DataCommands.manipulateData((CommandContext<CommandSourceStack>)$$3, $$2, $$2, DataCommands.stringifyTagList(DataCommands.resolveSourcePath((CommandContext<CommandSourceStack>)$$3, $$4), $$1 -> DataCommands.substring($$1, IntegerArgumentType.getInteger((CommandContext)$$3, (String)"start"), IntegerArgumentType.getInteger((CommandContext)$$3, (String)"end"))))))))));
                }
                $$0.accept((ArgumentBuilder<CommandSourceStack, ?>)$$3, $$1 -> Commands.literal("value").then(Commands.argument("value", NbtTagArgument.nbtTag()).executes($$2 -> {
                    List<Tag> $$3 = Collections.singletonList(NbtTagArgument.getNbtTag($$2, "value"));
                    return DataCommands.manipulateData((CommandContext<CommandSourceStack>)$$2, $$2, $$1, $$3);
                })));
                return $$22.then($$3);
            });
        }
        return $$1;
    }

    private static String validatedSubstring(String $$0, int $$1, int $$2) throws CommandSyntaxException {
        if ($$1 < 0 || $$2 > $$0.length() || $$1 > $$2) {
            throw ERROR_INVALID_SUBSTRING.create((Object)$$1, (Object)$$2);
        }
        return $$0.substring($$1, $$2);
    }

    private static String substring(String $$0, int $$1, int $$2) throws CommandSyntaxException {
        int $$3 = $$0.length();
        int $$4 = DataCommands.getOffset($$1, $$3);
        int $$5 = DataCommands.getOffset($$2, $$3);
        return DataCommands.validatedSubstring($$0, $$4, $$5);
    }

    private static String substring(String $$0, int $$1) throws CommandSyntaxException {
        int $$2 = $$0.length();
        return DataCommands.validatedSubstring($$0, DataCommands.getOffset($$1, $$2), $$2);
    }

    private static int getOffset(int $$0, int $$1) {
        return $$0 >= 0 ? $$0 : $$1 + $$0;
    }

    private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> $$0, DataProvider $$1) throws CommandSyntaxException {
        DataAccessor $$2 = $$1.access($$0);
        return Collections.singletonList($$2.getData());
    }

    private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> $$0, DataProvider $$1) throws CommandSyntaxException {
        DataAccessor $$2 = $$1.access($$0);
        NbtPathArgument.NbtPath $$3 = NbtPathArgument.getPath($$0, "sourcePath");
        return $$3.get($$2.getData());
    }

    private static int manipulateData(CommandContext<CommandSourceStack> $$0, DataProvider $$1, DataManipulator $$2, List<Tag> $$3) throws CommandSyntaxException {
        DataAccessor $$4 = $$1.access($$0);
        NbtPathArgument.NbtPath $$5 = NbtPathArgument.getPath($$0, "targetPath");
        CompoundTag $$6 = $$4.getData();
        int $$7 = $$2.modify($$0, $$6, $$5, $$3);
        if ($$7 == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        $$4.setData($$6);
        ((CommandSourceStack)$$0.getSource()).sendSuccess(() -> $$4.getModifiedSuccess(), true);
        return $$7;
    }

    private static int removeData(CommandSourceStack $$0, DataAccessor $$1, NbtPathArgument.NbtPath $$2) throws CommandSyntaxException {
        CompoundTag $$3 = $$1.getData();
        int $$4 = $$2.remove($$3);
        if ($$4 == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        $$1.setData($$3);
        $$0.sendSuccess(() -> $$1.getModifiedSuccess(), true);
        return $$4;
    }

    public static Tag getSingleTag(NbtPathArgument.NbtPath $$0, DataAccessor $$1) throws CommandSyntaxException {
        List<Tag> $$2 = $$0.get($$1.getData());
        Iterator $$3 = $$2.iterator();
        Tag $$4 = (Tag)$$3.next();
        if ($$3.hasNext()) {
            throw ERROR_MULTIPLE_TAGS.create();
        }
        return $$4;
    }

    /*
     * Loose catch block
     */
    private static int getData(CommandSourceStack $$0, DataAccessor $$1, NbtPathArgument.NbtPath $$2) throws CommandSyntaxException {
        Tag $$3;
        Tag tag = $$3 = DataCommands.getSingleTag($$2, $$1);
        Objects.requireNonNull(tag);
        Tag tag2 = tag;
        int n = 0;
        int $$9 = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{NumericTag.class, CollectionTag.class, CompoundTag.class, StringTag.class, EndTag.class}, (Object)tag2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                NumericTag $$4 = (NumericTag)tag2;
                yield Mth.floor($$4.doubleValue());
            }
            case 1 -> {
                CollectionTag $$5 = (CollectionTag)tag2;
                yield $$5.size();
            }
            case 2 -> {
                CompoundTag $$6 = (CompoundTag)tag2;
                yield $$6.size();
            }
            case 3 -> {
                String var12_11;
                StringTag var10_10 = (StringTag)tag2;
                String $$7 = var12_11 = var10_10.value();
                yield $$7.length();
            }
            case 4 -> {
                EndTag $$8 = (EndTag)tag2;
                throw ERROR_GET_NON_EXISTENT.create((Object)$$2.toString());
            }
        };
        $$0.sendSuccess(() -> $$3.getPrintSuccess($$3), false);
        return $$9;
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    private static int getNumeric(CommandSourceStack $$0, DataAccessor $$1, NbtPathArgument.NbtPath $$2, double $$3) throws CommandSyntaxException {
        Tag $$4 = DataCommands.getSingleTag($$2, $$1);
        if (!($$4 instanceof NumericTag)) {
            throw ERROR_GET_NOT_NUMBER.create((Object)$$2.toString());
        }
        int $$5 = Mth.floor(((NumericTag)$$4).doubleValue() * $$3);
        $$0.sendSuccess(() -> $$1.getPrintSuccess($$2, $$3, $$5), false);
        return $$5;
    }

    private static int getData(CommandSourceStack $$0, DataAccessor $$1) throws CommandSyntaxException {
        CompoundTag $$2 = $$1.getData();
        $$0.sendSuccess(() -> $$1.getPrintSuccess($$2), false);
        return 1;
    }

    private static int mergeData(CommandSourceStack $$0, DataAccessor $$1, CompoundTag $$2) throws CommandSyntaxException {
        CompoundTag $$3 = $$1.getData();
        if (NbtPathArgument.NbtPath.isTooDeep($$2, 0)) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
        }
        CompoundTag $$4 = $$3.copy().merge($$2);
        if ($$3.equals($$4)) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        $$1.setData($$4);
        $$0.sendSuccess(() -> $$1.getModifiedSuccess(), true);
        return 1;
    }

    public static interface DataProvider {
        public DataAccessor access(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> var1, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> var2);
    }

    @FunctionalInterface
    static interface StringProcessor {
        public String process(String var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface DataManipulator {
        public int modify(CommandContext<CommandSourceStack> var1, CompoundTag var2, NbtPathArgument.NbtPath var3, List<Tag> var4) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface DataManipulatorDecorator {
        public ArgumentBuilder<CommandSourceStack, ?> create(DataManipulator var1);
    }
}

