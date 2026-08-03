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
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.LongArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.StringArgumentSerializer;
import net.minecraft.core.Registry;

public class ArgumentTypeInfos {
   private static final Map<Class<?>, ArgumentTypeInfo<?, ?>> BY_CLASS = Maps.newHashMap();

   private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(
      Registry<ArgumentTypeInfo<?, ?>> $$0, String $$1, Class<? extends A> $$2, ArgumentTypeInfo<A, T> $$3
   ) {
      BY_CLASS.put($$2, $$3);
      return (ArgumentTypeInfo<A, T>)Registry.register($$0, $$1, $$3);
   }

   public static ArgumentTypeInfo<?, ?> bootstrap(Registry<ArgumentTypeInfo<?, ?>> $$0) {
      register($$0, "brigadier:bool", BoolArgumentType.class, SingletonArgumentInfo.contextFree(BoolArgumentType::bool));
      register($$0, "brigadier:float", FloatArgumentType.class, new FloatArgumentInfo());
      register($$0, "brigadier:double", DoubleArgumentType.class, new DoubleArgumentInfo());
      register($$0, "brigadier:integer", IntegerArgumentType.class, new IntegerArgumentInfo());
      register($$0, "brigadier:long", LongArgumentType.class, new LongArgumentInfo());
      register($$0, "brigadier:string", StringArgumentType.class, new StringArgumentSerializer());
      register($$0, "entity", EntityArgument.class, new EntityArgument.Info());
      register($$0, "game_profile", GameProfileArgument.class, SingletonArgumentInfo.contextFree(GameProfileArgument::gameProfile));
      register($$0, "block_pos", BlockPosArgument.class, SingletonArgumentInfo.contextFree(BlockPosArgument::blockPos));
      register($$0, "column_pos", ColumnPosArgument.class, SingletonArgumentInfo.contextFree(ColumnPosArgument::columnPos));
      register($$0, "vec3", Vec3Argument.class, SingletonArgumentInfo.contextFree(Vec3Argument::vec3));
      register($$0, "vec2", Vec2Argument.class, SingletonArgumentInfo.contextFree(Vec2Argument::vec2));
      register($$0, "block_state", BlockStateArgument.class, SingletonArgumentInfo.contextAware(BlockStateArgument::block));
      register($$0, "block_predicate", BlockPredicateArgument.class, SingletonArgumentInfo.contextAware(BlockPredicateArgument::blockPredicate));
      register($$0, "item_stack", ItemArgument.class, SingletonArgumentInfo.contextAware(ItemArgument::item));
      register($$0, "item_predicate", ItemPredicateArgument.class, SingletonArgumentInfo.contextAware(ItemPredicateArgument::itemPredicate));
      register($$0, "color", ColorArgument.class, SingletonArgumentInfo.contextFree(ColorArgument::color));
      register($$0, "hex_color", HexColorArgument.class, SingletonArgumentInfo.contextFree(HexColorArgument::hexColor));
      register($$0, "component", ComponentArgument.class, SingletonArgumentInfo.contextAware(ComponentArgument::textComponent));
      register($$0, "style", StyleArgument.class, SingletonArgumentInfo.contextAware(StyleArgument::style));
      register($$0, "message", MessageArgument.class, SingletonArgumentInfo.contextFree(MessageArgument::message));
      register($$0, "nbt_compound_tag", CompoundTagArgument.class, SingletonArgumentInfo.contextFree(CompoundTagArgument::compoundTag));
      register($$0, "nbt_tag", NbtTagArgument.class, SingletonArgumentInfo.contextFree(NbtTagArgument::nbtTag));
      register($$0, "nbt_path", NbtPathArgument.class, SingletonArgumentInfo.contextFree(NbtPathArgument::nbtPath));
      register($$0, "objective", ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective));
      register($$0, "objective_criteria", ObjectiveCriteriaArgument.class, SingletonArgumentInfo.contextFree(ObjectiveCriteriaArgument::criteria));
      register($$0, "operation", OperationArgument.class, SingletonArgumentInfo.contextFree(OperationArgument::operation));
      register($$0, "particle", ParticleArgument.class, SingletonArgumentInfo.contextAware(ParticleArgument::particle));
      register($$0, "angle", AngleArgument.class, SingletonArgumentInfo.contextFree(AngleArgument::angle));
      register($$0, "rotation", RotationArgument.class, SingletonArgumentInfo.contextFree(RotationArgument::rotation));
      register($$0, "scoreboard_slot", ScoreboardSlotArgument.class, SingletonArgumentInfo.contextFree(ScoreboardSlotArgument::displaySlot));
      register($$0, "score_holder", ScoreHolderArgument.class, new ScoreHolderArgument.Info());
      register($$0, "swizzle", SwizzleArgument.class, SingletonArgumentInfo.contextFree(SwizzleArgument::swizzle));
      register($$0, "team", TeamArgument.class, SingletonArgumentInfo.contextFree(TeamArgument::team));
      register($$0, "item_slot", SlotArgument.class, SingletonArgumentInfo.contextFree(SlotArgument::slot));
      register($$0, "item_slots", SlotsArgument.class, SingletonArgumentInfo.contextFree(SlotsArgument::slots));
      register($$0, "resource_location", IdentifierArgument.class, SingletonArgumentInfo.contextFree(IdentifierArgument::id));
      register($$0, "function", FunctionArgument.class, SingletonArgumentInfo.contextFree(FunctionArgument::functions));
      register($$0, "entity_anchor", EntityAnchorArgument.class, SingletonArgumentInfo.contextFree(EntityAnchorArgument::anchor));
      register($$0, "int_range", RangeArgument.Ints.class, SingletonArgumentInfo.contextFree(RangeArgument::intRange));
      register($$0, "float_range", RangeArgument.Floats.class, SingletonArgumentInfo.contextFree(RangeArgument::floatRange));
      register($$0, "dimension", DimensionArgument.class, SingletonArgumentInfo.contextFree(DimensionArgument::dimension));
      register($$0, "gamemode", GameModeArgument.class, SingletonArgumentInfo.contextFree(GameModeArgument::gameMode));
      register($$0, "time", TimeArgument.class, new TimeArgument.Info());
      register($$0, "resource_or_tag", fixClassType(ResourceOrTagArgument.class), new ResourceOrTagArgument.Info());
      register($$0, "resource_or_tag_key", fixClassType(ResourceOrTagKeyArgument.class), new ResourceOrTagKeyArgument.Info());
      register($$0, "resource", fixClassType(ResourceArgument.class), new ResourceArgument.Info());
      register($$0, "resource_key", fixClassType(ResourceKeyArgument.class), new ResourceKeyArgument.Info());
      register($$0, "resource_selector", fixClassType(ResourceSelectorArgument.class), new ResourceSelectorArgument.Info());
      register($$0, "template_mirror", TemplateMirrorArgument.class, SingletonArgumentInfo.contextFree(TemplateMirrorArgument::templateMirror));
      register($$0, "template_rotation", TemplateRotationArgument.class, SingletonArgumentInfo.contextFree(TemplateRotationArgument::templateRotation));
      register($$0, "heightmap", HeightmapTypeArgument.class, SingletonArgumentInfo.contextFree(HeightmapTypeArgument::heightmap));
      register($$0, "loot_table", ResourceOrIdArgument.LootTableArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootTable));
      register($$0, "loot_predicate", ResourceOrIdArgument.LootPredicateArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootPredicate));
      register($$0, "loot_modifier", ResourceOrIdArgument.LootModifierArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::lootModifier));
      register($$0, "dialog", ResourceOrIdArgument.DialogArgument.class, SingletonArgumentInfo.contextAware(ResourceOrIdArgument::dialog));
      return register($$0, "uuid", UuidArgument.class, SingletonArgumentInfo.contextFree(UuidArgument::uuid));
   }

   private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> $$0) {
      return (Class<T>)$$0;
   }

   public static boolean isClassRecognized(Class<?> $$0) {
      return BY_CLASS.containsKey($$0);
   }

   public static <A extends ArgumentType<?>> ArgumentTypeInfo<A, ?> byClass(A $$0) {
      ArgumentTypeInfo<?, ?> $$1 = BY_CLASS.get($$0.getClass());
      if ($$1 == null) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "Unrecognized argument type %s (%s)", $$0, $$0.getClass()));
      } else {
         return (ArgumentTypeInfo<A, ?>)$$1;
      }
   }

   public static <A extends ArgumentType<?>> ArgumentTypeInfo.Template<A> unpack(A $$0) {
      return byClass($$0).unpack($$0);
   }
}
