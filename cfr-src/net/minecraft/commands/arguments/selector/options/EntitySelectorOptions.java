/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public class EntitySelectorOptions {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Option> OPTIONS = Maps.newHashMap();
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.entity.options.unknown", $$0));
    public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.entity.options.inapplicable", $$0));
    public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.options.distance.negative"));
    public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.options.level.negative"));
    public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.options.limit.toosmall"));
    public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.entity.options.sort.irreversible", $$0));
    public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.entity.options.mode.invalid", $$0));
    public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.entity.options.type.invalid", $$0));

    private static void register(String $$0, Modifier $$1, Predicate<EntitySelectorParser> $$2, Component $$3) {
        OPTIONS.put($$0, new Option($$1, $$2, $$3));
    }

    public static void bootStrap() {
        if (!OPTIONS.isEmpty()) {
            return;
        }
        EntitySelectorOptions.register("name", $$0 -> {
            int $$1 = $$0.getReader().getCursor();
            boolean $$22 = $$0.shouldInvertValue();
            String $$3 = $$0.getReader().readString();
            if ($$0.hasNameNotEquals() && !$$22) {
                $$0.getReader().setCursor($$1);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext((ImmutableStringReader)$$0.getReader(), (Object)"name");
            }
            if ($$22) {
                $$0.setHasNameNotEquals(true);
            } else {
                $$0.setHasNameEquals(true);
            }
            $$0.addPredicate($$2 -> $$2.getPlainTextName().equals($$3) != $$22);
        }, $$0 -> !$$0.hasNameEquals(), Component.translatable("argument.entity.options.name.description"));
        EntitySelectorOptions.register("distance", $$0 -> {
            int $$1 = $$0.getReader().getCursor();
            MinMaxBounds.Doubles $$2 = MinMaxBounds.Doubles.fromReader($$0.getReader());
            if ($$2.min().isPresent() && (Double)$$2.min().get() < 0.0 || $$2.max().isPresent() && (Double)$$2.max().get() < 0.0) {
                $$0.getReader().setCursor($$1);
                throw ERROR_RANGE_NEGATIVE.createWithContext((ImmutableStringReader)$$0.getReader());
            }
            $$0.setDistance($$2);
            $$0.setWorldLimited();
        }, $$0 -> $$0.getDistance() == null, Component.translatable("argument.entity.options.distance.description"));
        EntitySelectorOptions.register("level", $$0 -> {
            int $$1 = $$0.getReader().getCursor();
            MinMaxBounds.Ints $$2 = MinMaxBounds.Ints.fromReader($$0.getReader());
            if ($$2.min().isPresent() && (Integer)$$2.min().get() < 0 || $$2.max().isPresent() && (Integer)$$2.max().get() < 0) {
                $$0.getReader().setCursor($$1);
                throw ERROR_LEVEL_NEGATIVE.createWithContext((ImmutableStringReader)$$0.getReader());
            }
            $$0.setLevel($$2);
            $$0.setIncludesEntities(false);
        }, $$0 -> $$0.getLevel() == null, Component.translatable("argument.entity.options.level.description"));
        EntitySelectorOptions.register("x", $$0 -> {
            $$0.setWorldLimited();
            $$0.setX($$0.getReader().readDouble());
        }, $$0 -> $$0.getX() == null, Component.translatable("argument.entity.options.x.description"));
        EntitySelectorOptions.register("y", $$0 -> {
            $$0.setWorldLimited();
            $$0.setY($$0.getReader().readDouble());
        }, $$0 -> $$0.getY() == null, Component.translatable("argument.entity.options.y.description"));
        EntitySelectorOptions.register("z", $$0 -> {
            $$0.setWorldLimited();
            $$0.setZ($$0.getReader().readDouble());
        }, $$0 -> $$0.getZ() == null, Component.translatable("argument.entity.options.z.description"));
        EntitySelectorOptions.register("dx", $$0 -> {
            $$0.setWorldLimited();
            $$0.setDeltaX($$0.getReader().readDouble());
        }, $$0 -> $$0.getDeltaX() == null, Component.translatable("argument.entity.options.dx.description"));
        EntitySelectorOptions.register("dy", $$0 -> {
            $$0.setWorldLimited();
            $$0.setDeltaY($$0.getReader().readDouble());
        }, $$0 -> $$0.getDeltaY() == null, Component.translatable("argument.entity.options.dy.description"));
        EntitySelectorOptions.register("dz", $$0 -> {
            $$0.setWorldLimited();
            $$0.setDeltaZ($$0.getReader().readDouble());
        }, $$0 -> $$0.getDeltaZ() == null, Component.translatable("argument.entity.options.dz.description"));
        EntitySelectorOptions.register("x_rotation", $$0 -> $$0.setRotX(MinMaxBounds.FloatDegrees.fromReader($$0.getReader())), $$0 -> $$0.getRotX() == null, Component.translatable("argument.entity.options.x_rotation.description"));
        EntitySelectorOptions.register("y_rotation", $$0 -> $$0.setRotY(MinMaxBounds.FloatDegrees.fromReader($$0.getReader())), $$0 -> $$0.getRotY() == null, Component.translatable("argument.entity.options.y_rotation.description"));
        EntitySelectorOptions.register("limit", $$0 -> {
            int $$1 = $$0.getReader().getCursor();
            int $$2 = $$0.getReader().readInt();
            if ($$2 < 1) {
                $$0.getReader().setCursor($$1);
                throw ERROR_LIMIT_TOO_SMALL.createWithContext((ImmutableStringReader)$$0.getReader());
            }
            $$0.setMaxResults($$2);
            $$0.setLimited(true);
        }, $$0 -> !$$0.isCurrentEntity() && !$$0.isLimited(), Component.translatable("argument.entity.options.limit.description"));
        EntitySelectorOptions.register("sort", $$02 -> {
            int $$12 = $$02.getReader().getCursor();
            String $$2 = $$02.getReader().readUnquotedString();
            $$02.setSuggestions(($$0, $$1) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), $$0));
            $$02.setOrder(switch ($$2) {
                case "nearest" -> EntitySelectorParser.ORDER_NEAREST;
                case "furthest" -> EntitySelectorParser.ORDER_FURTHEST;
                case "random" -> EntitySelectorParser.ORDER_RANDOM;
                case "arbitrary" -> EntitySelector.ORDER_ARBITRARY;
                default -> {
                    $$02.getReader().setCursor($$12);
                    throw ERROR_SORT_UNKNOWN.createWithContext((ImmutableStringReader)$$02.getReader(), (Object)$$2);
                }
            });
            $$02.setSorted(true);
        }, $$0 -> !$$0.isCurrentEntity() && !$$0.isSorted(), Component.translatable("argument.entity.options.sort.description"));
        EntitySelectorOptions.register("gamemode", $$0 -> {
            $$0.setSuggestions(($$1, $$2) -> {
                String $$3 = $$1.getRemaining().toLowerCase(Locale.ROOT);
                boolean $$4 = !$$0.hasGamemodeNotEquals();
                boolean $$5 = true;
                if (!$$3.isEmpty()) {
                    if ($$3.charAt(0) == '!') {
                        $$4 = false;
                        $$3 = $$3.substring(1);
                    } else {
                        $$5 = false;
                    }
                }
                for (GameType $$6 : GameType.values()) {
                    if (!$$6.getName().toLowerCase(Locale.ROOT).startsWith($$3)) continue;
                    if ($$5) {
                        $$1.suggest("!" + $$6.getName());
                    }
                    if (!$$4) continue;
                    $$1.suggest($$6.getName());
                }
                return $$1.buildFuture();
            });
            int $$12 = $$0.getReader().getCursor();
            boolean $$22 = $$0.shouldInvertValue();
            if ($$0.hasGamemodeNotEquals() && !$$22) {
                $$0.getReader().setCursor($$12);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext((ImmutableStringReader)$$0.getReader(), (Object)"gamemode");
            }
            String $$3 = $$0.getReader().readUnquotedString();
            GameType $$4 = GameType.byName($$3, null);
            if ($$4 == null) {
                $$0.getReader().setCursor($$12);
                throw ERROR_GAME_MODE_INVALID.createWithContext((ImmutableStringReader)$$0.getReader(), (Object)$$3);
            }
            $$0.setIncludesEntities(false);
            $$0.addPredicate($$2 -> {
                if ($$2 instanceof ServerPlayer) {
                    ServerPlayer $$3 = (ServerPlayer)$$2;
                    GameType $$4 = $$3.gameMode();
                    return $$4 == $$4 ^ $$22;
                }
                return false;
            });
            if ($$22) {
                $$0.setHasGamemodeNotEquals(true);
            } else {
                $$0.setHasGamemodeEquals(true);
            }
        }, $$0 -> !$$0.hasGamemodeEquals(), Component.translatable("argument.entity.options.gamemode.description"));
        EntitySelectorOptions.register("team", $$0 -> {
            boolean $$1 = $$0.shouldInvertValue();
            String $$22 = $$0.getReader().readUnquotedString();
            $$0.addPredicate($$2 -> {
                PlayerTeam $$3 = $$2.getTeam();
                String $$4 = $$3 == null ? "" : ((Team)$$3).getName();
                return $$4.equals($$22) != $$1;
            });
            if ($$1) {
                $$0.setHasTeamNotEquals(true);
            } else {
                $$0.setHasTeamEquals(true);
            }
        }, $$0 -> !$$0.hasTeamEquals(), Component.translatable("argument.entity.options.team.description"));
        EntitySelectorOptions.register("type", $$0 -> {
            $$0.setSuggestions(($$1, $$2) -> {
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), $$1, String.valueOf('!'));
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTags().map($$0 -> $$0.key().location()), $$1, "!#");
                if (!$$0.isTypeLimitedInversely()) {
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), $$1);
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTags().map($$0 -> $$0.key().location()), $$1, String.valueOf('#'));
                }
                return $$1.buildFuture();
            });
            int $$12 = $$0.getReader().getCursor();
            boolean $$22 = $$0.shouldInvertValue();
            if ($$0.isTypeLimitedInversely() && !$$22) {
                $$0.getReader().setCursor($$12);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext((ImmutableStringReader)$$0.getReader(), (Object)"type");
            }
            if ($$22) {
                $$0.setTypeLimitedInversely();
            }
            if ($$0.isTag()) {
                TagKey<EntityType<?>> $$3 = TagKey.create(Registries.ENTITY_TYPE, Identifier.read($$0.getReader()));
                $$0.addPredicate($$2 -> $$2.getType().is($$3) != $$22);
            } else {
                Identifier $$4 = Identifier.read($$0.getReader());
                EntityType $$5 = (EntityType)BuiltInRegistries.ENTITY_TYPE.getOptional($$4).orElseThrow(() -> {
                    $$0.getReader().setCursor($$12);
                    return ERROR_ENTITY_TYPE_INVALID.createWithContext((ImmutableStringReader)$$0.getReader(), (Object)$$4.toString());
                });
                if (Objects.equals(EntityType.PLAYER, $$5) && !$$22) {
                    $$0.setIncludesEntities(false);
                }
                $$0.addPredicate($$2 -> Objects.equals($$5, $$2.getType()) != $$22);
                if (!$$22) {
                    $$0.limitToType($$5);
                }
            }
        }, $$0 -> !$$0.isTypeLimited(), Component.translatable("argument.entity.options.type.description"));
        EntitySelectorOptions.register("tag", $$0 -> {
            boolean $$1 = $$0.shouldInvertValue();
            String $$22 = $$0.getReader().readUnquotedString();
            $$0.addPredicate($$2 -> {
                if ("".equals($$22)) {
                    return $$2.getTags().isEmpty() != $$1;
                }
                return $$2.getTags().contains($$22) != $$1;
            });
        }, $$0 -> true, Component.translatable("argument.entity.options.tag.description"));
        EntitySelectorOptions.register("nbt", $$0 -> {
            boolean $$1 = $$0.shouldInvertValue();
            CompoundTag $$22 = TagParser.parseCompoundAsArgument($$0.getReader());
            $$0.addPredicate($$2 -> {
                try (ProblemReporter.ScopedCollector $$3 = new ProblemReporter.ScopedCollector($$2.problemPath(), LOGGER);){
                    ServerPlayer $$5;
                    ItemStack $$6;
                    TagValueOutput $$4 = TagValueOutput.createWithContext($$3, $$2.registryAccess());
                    $$2.saveWithoutId($$4);
                    if ($$2 instanceof ServerPlayer && !($$6 = ($$5 = (ServerPlayer)$$2).getInventory().getSelectedItem()).isEmpty()) {
                        $$4.store("SelectedItem", ItemStack.CODEC, $$6);
                    }
                    boolean bl = NbtUtils.compareNbt($$22, $$4.buildResult(), true) != $$1;
                    return bl;
                }
            });
        }, $$0 -> true, Component.translatable("argument.entity.options.nbt.description"));
        EntitySelectorOptions.register("scores", $$0 -> {
            StringReader $$12 = $$0.getReader();
            HashMap $$2 = Maps.newHashMap();
            $$12.expect('{');
            $$12.skipWhitespace();
            while ($$12.canRead() && $$12.peek() != '}') {
                $$12.skipWhitespace();
                String $$3 = $$12.readUnquotedString();
                $$12.skipWhitespace();
                $$12.expect('=');
                $$12.skipWhitespace();
                MinMaxBounds.Ints $$4 = MinMaxBounds.Ints.fromReader($$12);
                $$2.put($$3, $$4);
                $$12.skipWhitespace();
                if (!$$12.canRead() || $$12.peek() != ',') continue;
                $$12.skip();
            }
            $$12.expect('}');
            if (!$$2.isEmpty()) {
                $$0.addPredicate($$1 -> {
                    ServerScoreboard $$2 = $$1.level().getServer().getScoreboard();
                    for (Map.Entry $$3 : $$2.entrySet()) {
                        Objective $$4 = $$2.getObjective((String)$$3.getKey());
                        if ($$4 == null) {
                            return false;
                        }
                        ReadOnlyScoreInfo $$5 = $$2.getPlayerScoreInfo((ScoreHolder)$$1, $$4);
                        if ($$5 == null) {
                            return false;
                        }
                        if (((MinMaxBounds.Ints)$$3.getValue()).matches($$5.value())) continue;
                        return false;
                    }
                    return true;
                });
            }
            $$0.setHasScores(true);
        }, $$0 -> !$$0.hasScores(), Component.translatable("argument.entity.options.scores.description"));
        EntitySelectorOptions.register("advancements", $$0 -> {
            StringReader $$12 = $$0.getReader();
            HashMap $$2 = Maps.newHashMap();
            $$12.expect('{');
            $$12.skipWhitespace();
            while ($$12.canRead() && $$12.peek() != '}') {
                $$12.skipWhitespace();
                Identifier $$3 = Identifier.read($$12);
                $$12.skipWhitespace();
                $$12.expect('=');
                $$12.skipWhitespace();
                if ($$12.canRead() && $$12.peek() == '{') {
                    HashMap $$4 = Maps.newHashMap();
                    $$12.skipWhitespace();
                    $$12.expect('{');
                    $$12.skipWhitespace();
                    while ($$12.canRead() && $$12.peek() != '}') {
                        $$12.skipWhitespace();
                        String $$5 = $$12.readUnquotedString();
                        $$12.skipWhitespace();
                        $$12.expect('=');
                        $$12.skipWhitespace();
                        boolean $$6 = $$12.readBoolean();
                        $$4.put($$5, $$1 -> $$1.isDone() == $$6);
                        $$12.skipWhitespace();
                        if (!$$12.canRead() || $$12.peek() != ',') continue;
                        $$12.skip();
                    }
                    $$12.skipWhitespace();
                    $$12.expect('}');
                    $$12.skipWhitespace();
                    $$2.put($$3, $$1 -> {
                        for (Map.Entry $$2 : $$4.entrySet()) {
                            CriterionProgress $$3 = $$1.getCriterion((String)$$2.getKey());
                            if ($$3 != null && ((Predicate)$$2.getValue()).test($$3)) continue;
                            return false;
                        }
                        return true;
                    });
                } else {
                    boolean $$7 = $$12.readBoolean();
                    $$2.put($$3, $$1 -> $$1.isDone() == $$7);
                }
                $$12.skipWhitespace();
                if (!$$12.canRead() || $$12.peek() != ',') continue;
                $$12.skip();
            }
            $$12.expect('}');
            if (!$$2.isEmpty()) {
                $$0.addPredicate($$1 -> {
                    void $$3;
                    if (!($$1 instanceof ServerPlayer)) {
                        return false;
                    }
                    ServerPlayer $$2 = (ServerPlayer)$$1;
                    PlayerAdvancements $$4 = $$3.getAdvancements();
                    ServerAdvancementManager $$5 = $$3.level().getServer().getAdvancements();
                    for (Map.Entry $$6 : $$2.entrySet()) {
                        AdvancementHolder $$7 = $$5.get((Identifier)$$6.getKey());
                        if ($$7 != null && ((Predicate)$$6.getValue()).test($$4.getOrStartProgress($$7))) continue;
                        return false;
                    }
                    return true;
                });
                $$0.setIncludesEntities(false);
            }
            $$0.setHasAdvancements(true);
        }, $$0 -> !$$0.hasAdvancements(), Component.translatable("argument.entity.options.advancements.description"));
        EntitySelectorOptions.register("predicate", $$0 -> {
            boolean $$1 = $$0.shouldInvertValue();
            ResourceKey<LootItemCondition> $$22 = ResourceKey.create(Registries.PREDICATE, Identifier.read($$0.getReader()));
            $$0.addPredicate($$2 -> {
                void $$5;
                Level $$3 = $$2.level();
                if (!($$3 instanceof ServerLevel)) {
                    return false;
                }
                ServerLevel $$4 = (ServerLevel)$$3;
                Optional<LootItemCondition> $$6 = $$5.getServer().reloadableRegistries().lookup().get($$22).map(Holder::value);
                if ($$6.isEmpty()) {
                    return false;
                }
                LootParams $$7 = new LootParams.Builder((ServerLevel)$$5).withParameter(LootContextParams.THIS_ENTITY, $$2).withParameter(LootContextParams.ORIGIN, $$2.position()).create(LootContextParamSets.SELECTOR);
                LootContext $$8 = new LootContext.Builder($$7).create(Optional.empty());
                $$8.pushVisitedElement(LootContext.createVisitedEntry($$6.get()));
                return $$1 ^ $$6.get().test($$8);
            });
        }, $$0 -> true, Component.translatable("argument.entity.options.predicate.description"));
    }

    public static Modifier get(EntitySelectorParser $$0, String $$1, int $$2) throws CommandSyntaxException {
        Option $$3 = OPTIONS.get($$1);
        if ($$3 != null) {
            if ($$3.canUse.test($$0)) {
                return $$3.modifier;
            }
            throw ERROR_INAPPLICABLE_OPTION.createWithContext((ImmutableStringReader)$$0.getReader(), (Object)$$1);
        }
        $$0.getReader().setCursor($$2);
        throw ERROR_UNKNOWN_OPTION.createWithContext((ImmutableStringReader)$$0.getReader(), (Object)$$1);
    }

    public static void suggestNames(EntitySelectorParser $$0, SuggestionsBuilder $$1) {
        String $$2 = $$1.getRemaining().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Option> $$3 : OPTIONS.entrySet()) {
            if (!$$3.getValue().canUse.test($$0) || !$$3.getKey().toLowerCase(Locale.ROOT).startsWith($$2)) continue;
            $$1.suggest($$3.getKey() + "=", (Message)$$3.getValue().description);
        }
    }

    static final class Option
    extends Record {
        final Modifier modifier;
        final Predicate<EntitySelectorParser> canUse;
        final Component description;

        Option(Modifier $$0, Predicate<EntitySelectorParser> $$1, Component $$2) {
            this.modifier = $$0;
            this.canUse = $$1;
            this.description = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Option.class, "modifier;canUse;description", "modifier", "canUse", "description"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Option.class, "modifier;canUse;description", "modifier", "canUse", "description"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Option.class, "modifier;canUse;description", "modifier", "canUse", "description"}, this, $$0);
        }

        public Modifier modifier() {
            return this.modifier;
        }

        public Predicate<EntitySelectorParser> canUse() {
            return this.canUse;
        }

        public Component description() {
            return this.description;
        }
    }

    @FunctionalInterface
    public static interface Modifier {
        public void handle(EntitySelectorParser var1) throws CommandSyntaxException;
    }
}

