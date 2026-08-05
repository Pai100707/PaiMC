/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.LongArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 */
package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Locale;
import java.util.Map;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.HexColorArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.commands.arguments.ResourceSelectorArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.SlotsArgument;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.LongArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.core.Registry;

public class ArgumentTypeInfos {
    private static final Map<Class<?>, ArgumentTypeInfo<?, ?>> BY_CLASS = Maps.newHashMap();

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> $$0, String $$1, Class<? extends A> $$2, ArgumentTypeInfo<A, T> $$3) {
        BY_CLASS.put($$2, $$3);
        return Registry.register($$0, $$1, $$3);
    }

    public static ArgumentTypeInfo<?, ?> bootstrap(Registry<ArgumentTypeInfo<?, ?>> $$0) {
        ArgumentTypeInfos.register($$0, "brigadier:bool", BoolArgumentType.class, SingletonArgumentInfo.contextFree(BoolArgumentType::bool));
        ArgumentTypeInfos.register($$0, "brigadier:float", FloatArgumentType.class, new FloatArgumentInfo());
        ArgumentTypeInfos.register($$0, "brigadier:double", DoubleArgumentType.class, new DoubleArgumentInfo());
        ArgumentTypeInfos.register($$0, "brigadier:integer", IntegerArgumentType.class, new IntegerArgumentInfo());
        ArgumentTypeInfos.register($$0, "brigadier:long", LongArgumentType.class, new LongArgumentInfo());
        ArgumentTypeInfos.register($$0, "brigadier:string", StringArgumentType.class, new StringArgumentSerializer());
        ArgumentTypeInfos.register($$0, "entity", EntityArgument.class, new EntityArgument.Info());
        ArgumentTypeInfos.register($$0, "game_profile", GameProfileArgument.class, SingletonArgumentInfo.contextFree(GameProfileArgument::gameProfile));
        ArgumentTypeInfos.register($$0, "block_pos", BlockPosArgument.class, SingletonArgumentInfo.contextFree(BlockPosArgument::blockPos));
        ArgumentTypeInfos.register($$0, "column_pos", ColumnPosArgument.class, SingletonArgumentInfo.contextFree(ColumnPosArgument::columnPos));
        ArgumentTypeInfos.register($$0, "vec3", Vec3Argument.class, SingletonArgumentInfo.contextFree(Vec3Argument::vec3));
        ArgumentTypeInfos.register($$0, "vec2", Vec2Argument.class, SingletonArgumentInfo.contextFree(Vec2Argument::vec2));
        ArgumentTypeInfos.register($$0, "block_state", BlockStateArgument.class, SingletonArgumentInfo.contextAware(BlockStateArgument::block));
        ArgumentTypeInfos.register($$0, "block_predicate", BlockPredicateArgument.class, SingletonArgumentInfo.contextAware(BlockPredicateArgument::blockPredicate));
        ArgumentTypeInfos.register($$0, "item_stack", ItemArgument.class, SingletonArgumentInfo.contextAware(ItemArgument::item));
        ArgumentTypeInfos.register($$0, "item_predicate", ItemPredicateArgument.class, SingletonArgumentInfo.contextAware(ItemPredicateArgument::itemPredicate));
        ArgumentTypeInfos.register($$0, "color", ColorArgument.class, SingletonArgumentInfo.contextFree(ColorArgument::color));
        ArgumentTypeInfos.register($$0, "hex_color", HexColorArgument.class, SingletonArgumentInfo.contextFree(HexColorArgument::hexColor));
        ArgumentTypeInfos.register($$0, "component", ComponentArgument.class, SingletonArgumentInfo.contextAware(ComponentArgument::textComponent));
        ArgumentTypeInfos.register($$0, "style", StyleArgument.class, SingletonArgumentInfo.contextAware(StyleArgument::style));
        ArgumentTypeInfos.register($$0, "message", MessageArgument.class, SingletonArgumentInfo.contextFree(MessageArgument::message));
        ArgumentTypeInfos.register($$0, "nbt_compound_tag", CompoundTagArgument.class, SingletonArgumentInfo.contextFree(CompoundTagArgument::compoundTag));
        ArgumentTypeInfos.register($$0, "nbt_tag", NbtTagArgument.class, SingletonArgumentInfo.contextFree(NbtTagArgument::nbtTag));
        ArgumentTypeInfos.register($$0, "nbt_path", NbtPathArgument.class, SingletonArgumentInfo.contextFree(NbtPathArgument::nbtPath));
        ArgumentTypeInfos.register($$0, "objective", ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective));
        ArgumentTypeInfos.register($$0, "objective_criteria", ObjectiveCriteriaArgument.class, SingletonArgumentInfo.contextFree(ObjectiveCriteriaArgument::criteria));
        ArgumentTypeInfos.register($$0, "operation", OperationArgument.class, SingletonArgumentInfo.contextFree(OperationArgument::operation));
        ArgumentTypeInfos.register($$0, "particle", ParticleArgument.class, SingletonArgumentInfo.contextAware(ParticleArgument::particle));
        ArgumentTypeInfos.register($$0, "angle", AngleArgument.class, SingletonArgumentInfo.contextFree(AngleArgument::angle));
        ArgumentTypeInfos.register($$0, "rotation", RotationArgument.class, SingletonArgumentInfo.contextFree(RotationArgument::rotation));
        ArgumentTypeInfos.register($$0, "scoreboard_slot", ScoreboardSlotArgument.class, SingletonArgumentInfo.contextFree(ScoreboardSlotArgument::displaySlot));
        ArgumentTypeInfos.register($$0, "score_holder", ScoreHolderArgument.class, new ScoreHolderArgument.Info());
        ArgumentTypeInfos.register($$0, "swizzle", SwizzleArgument.class, SingletonArgumentInfo.contextFree(SwizzleArgument::swizzle));
        ArgumentTypeInfos.register($$0, "team", TeamArgument.class, SingletonArgumentInfo.contextFree(TeamArgument::team));
        ArgumentTypeInfos.register($$0, "item_slot", SlotArgument.class, SingletonArgumentInfo.contextFree(SlotArgument::slot));
        ArgumentTypeInfos.register($$0, "item_slots", SlotsArgument.class, SingletonArgumentInfo.contextFree(SlotsArgument::slots));
        ArgumentTypeInfos.register($$0, "resource_location", IdentifierArgument.class, SingletonArgumentInfo.contextFree(IdentifierArgument::id));
        ArgumentTypeInfos.register($$0, "function", FunctionArgument.class, SingletonArgumentInfo.contextFree(FunctionArgument::functions));
        ArgumentTypeInfos.register($$0, "entity_anchor", EntityAnchorArgument.class, SingletonArgumentInfo.contextFree(EntityAnchorArgument::anchor));
        ArgumentTypeInfos.register($$0, "int_range", RangeArgument.Ints.class, SingletonArgumentInfo.contextFree(RangeArgument::intRange));
        ArgumentTypeInfos.register($$0, "float_range", RangeArgument.Floats.class, SingletonArgumentInfo.contextFree(RangeArgument::floatRange));
        ArgumentTypeInfos.register($$0, "dimension", DimensionArgument.class, SingletonArgumentInfo.contextFree(DimensionArgument::dimension));
        ArgumentTypeInfos.register($$0, "gamemode", GameModeArgument.class, SingletonArgumentInfo.contextFree(GameModeArgument::gameMode));
        ArgumentTypeInfos.register($$0, "time", TimeArgument.class, new TimeArgument.Info());
        ArgumentTypeInfos.register($$0, "resource_or_tag", ArgumentTypeInfos.fixClassType(ResourceOrTagArgument.class), new ResourceOrTagArgument.Info());
        ArgumentTypeInfos.register($$0, "resource_or_tag_key", ArgumentTypeInfos.fixClassType(ResourceOrTagKeyArgument.class), new ResourceOrTagKeyArgument.Info());
        ArgumentTypeInfos.register($$0, "resource", ArgumentTypeInfos.fixClassType(ResourceArgument.class), new ResourceArgument.Info());
        ArgumentTypeInfos.register($$0, "resource_key", ArgumentTypeInfos.fixClassType(ResourceKeyArgument.class), new ResourceKeyArgument.Info());
        ArgumentTypeInfos.register($$0, "resource_selector", ArgumentTypeInfos.fixClassType(ResourceSelectorArgument.class), new ResourceSelectorArgument.Info());
        ArgumentTypeInfos.register($$0, "template_mirror", TemplateMirrorArgument.class, SingletonArgumentInfo.contextFree(TemplateMirrorArgument::templateMirror));
        ArgumentTypeInfos.register($$0, "template_rotation", TemplateRotationArgument.class, SingletonArgumentInfo.contextFree(TemplateRotationArgument::templateRotation));
        ArgumentTypeInfos.register($$0, "heightmap", HeightmapTypeArgument.class, SingletonArgumentInfo.contextFree(HeightmapTypeArgument::heightmap));
        ArgumentTypeInfos.register($$0, "loot_table", ResourceOrIdArgument.LootTableArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootTable));
        ArgumentTypeInfos.register($$0, "loot_predicate", ResourceOrIdArgument.LootPredicateArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootPredicate));
        ArgumentTypeInfos.register($$0, "loot_modifier", ResourceOrIdArgument.LootModifierArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootModifier));
        ArgumentTypeInfos.register($$0, "dialog", ResourceOrIdArgument.DialogArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::dialog));
        return ArgumentTypeInfos.register($$0, "uuid", UuidArgument.class, SingletonArgumentInfo.contextFree(UuidArgument::uuid));
    }

    private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> $$0) {
        return $$0;
    }

    public static boolean isClassRecognized(Class<?> $$0) {
        return BY_CLASS.containsKey($$0);
    }

    public static <A extends ArgumentType<?>> ArgumentTypeInfo<A, ?> byClass(A $$0) {
        ArgumentTypeInfo<?, ?> $$1 = BY_CLASS.get($$0.getClass());
        if ($$1 == null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Unrecognized argument type %s (%s)", $$0, $$0.getClass()));
        }
        return $$1;
    }

    public static <A extends ArgumentType<?>> ArgumentTypeInfo.Template<A> unpack(A $$0) {
        return ArgumentTypeInfos.byClass($$0).unpack($$0);
    }
}

