package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.advancements.criterion.MinMaxBounds.FloatDegrees;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public class EntitySelectorOptions {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<String, EntitySelectorOptions.Option> OPTIONS = Maps.newHashMap();
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.entity.options.unknown", new Object[]{$$0})
   );
   public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.entity.options.inapplicable", new Object[]{$$0})
   );
   public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType(
      Component.translatable("argument.entity.options.distance.negative")
   );
   public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType(
      Component.translatable("argument.entity.options.level.negative")
   );
   public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(
      Component.translatable("argument.entity.options.limit.toosmall")
   );
   public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.entity.options.sort.irreversible", new Object[]{$$0})
   );
   public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.entity.options.mode.invalid", new Object[]{$$0})
   );
   public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.entity.options.type.invalid", new Object[]{$$0})
   );

   private static void register(String $$0, EntitySelectorOptions.Modifier $$1, Predicate<EntitySelectorParser> $$2, Component $$3) {
      OPTIONS.put($$0, new EntitySelectorOptions.Option($$1, $$2, $$3));
   }

   public static void bootStrap() {
      if (OPTIONS.isEmpty()) {
         register("name", $$0 -> {
            int $$1 = $$0.getReader().getCursor();
            boolean $$2 = $$0.shouldInvertValue();
            String $$3 = $$0.getReader().readString();
            if ($$0.hasNameNotEquals() && !$$2) {
               $$0.getReader().setCursor($$1);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext($$0.getReader(), "name");
            } else {
               if ($$2) {
                  $$0.setHasNameNotEquals(true);
               } else {
                  $$0.setHasNameEquals(true);
               }

               $$0.addPredicate($$2x -> $$2x.getPlainTextName().equals($$3) != $$2);
            }
         }, $$0 -> !$$0.hasNameEquals(), Component.translatable("argument.entity.options.name.description"));
         register("distance", $$0 -> {
            int $$1 = $$0.getReader().getCursor();
            Doubles $$2 = Doubles.fromReader($$0.getReader());
            if ((!$$2.min().isPresent() || !((Double)$$2.min().get() < 0.0)) && (!$$2.max().isPresent() || !((Double)$$2.max().get() < 0.0))) {
               $$0.setDistance($$2);
               $$0.setWorldLimited();
            } else {
               $$0.getReader().setCursor($$1);
               throw ERROR_RANGE_NEGATIVE.createWithContext($$0.getReader());
            }
         }, $$0 -> $$0.getDistance() == null, Component.translatable("argument.entity.options.distance.description"));
         register("level", $$0 -> {
            int $$1 = $$0.getReader().getCursor();
            Ints $$2 = Ints.fromReader($$0.getReader());
            if ((!$$2.min().isPresent() || (Integer)$$2.min().get() >= 0) && (!$$2.max().isPresent() || (Integer)$$2.max().get() >= 0)) {
               $$0.setLevel($$2);
               $$0.setIncludesEntities(false);
            } else {
               $$0.getReader().setCursor($$1);
               throw ERROR_LEVEL_NEGATIVE.createWithContext($$0.getReader());
            }
         }, $$0 -> $$0.getLevel() == null, Component.translatable("argument.entity.options.level.description"));
         register("x", $$0 -> {
            $$0.setWorldLimited();
            $$0.setX($$0.getReader().readDouble());
         }, $$0 -> $$0.getX() == null, Component.translatable("argument.entity.options.x.description"));
         register("y", $$0 -> {
            $$0.setWorldLimited();
            $$0.setY($$0.getReader().readDouble());
         }, $$0 -> $$0.getY() == null, Component.translatable("argument.entity.options.y.description"));
         register("z", $$0 -> {
            $$0.setWorldLimited();
            $$0.setZ($$0.getReader().readDouble());
         }, $$0 -> $$0.getZ() == null, Component.translatable("argument.entity.options.z.description"));
         register("dx", $$0 -> {
            $$0.setWorldLimited();
            $$0.setDeltaX($$0.getReader().readDouble());
         }, $$0 -> $$0.getDeltaX() == null, Component.translatable("argument.entity.options.dx.description"));
         register("dy", $$0 -> {
            $$0.setWorldLimited();
            $$0.setDeltaY($$0.getReader().readDouble());
         }, $$0 -> $$0.getDeltaY() == null, Component.translatable("argument.entity.options.dy.description"));
         register("dz", $$0 -> {
            $$0.setWorldLimited();
            $$0.setDeltaZ($$0.getReader().readDouble());
         }, $$0 -> $$0.getDeltaZ() == null, Component.translatable("argument.entity.options.dz.description"));
         register(
            "x_rotation",
            $$0 -> $$0.setRotX(FloatDegrees.fromReader($$0.getReader())),
            $$0 -> $$0.getRotX() == null,
            Component.translatable("argument.entity.options.x_rotation.description")
         );
         register(
            "y_rotation",
            $$0 -> $$0.setRotY(FloatDegrees.fromReader($$0.getReader())),
            $$0 -> $$0.getRotY() == null,
            Component.translatable("argument.entity.options.y_rotation.description")
         );
         register("limit", $$0 -> {
            int $$1 = $$0.getReader().getCursor();
            int $$2 = $$0.getReader().readInt();
            if ($$2 < 1) {
               $$0.getReader().setCursor($$1);
               throw ERROR_LIMIT_TOO_SMALL.createWithContext($$0.getReader());
            } else {
               $$0.setMaxResults($$2);
               $$0.setLimited(true);
            }
         }, $$0 -> !$$0.isCurrentEntity() && !$$0.isLimited(), Component.translatable("argument.entity.options.limit.description"));
         register(
            "sort",
            $$0 -> {
               int $$1 = $$0.getReader().getCursor();
               String $$2 = $$0.getReader().readUnquotedString();
               $$0.setSuggestions(
                  ($$0x, $$1x) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), $$0x)
               );

               $$0.setOrder(switch ($$2) {
                  case "nearest" -> EntitySelectorParser.ORDER_NEAREST;
                  case "furthest" -> EntitySelectorParser.ORDER_FURTHEST;
                  case "random" -> EntitySelectorParser.ORDER_RANDOM;
                  case "arbitrary" -> EntitySelector.ORDER_ARBITRARY;
                  default -> {
                     $$0.getReader().setCursor($$1);
                     throw ERROR_SORT_UNKNOWN.createWithContext($$0.getReader(), $$2);
                  }
               });
               $$0.setSorted(true);
            },
            $$0 -> !$$0.isCurrentEntity() && !$$0.isSorted(),
            Component.translatable("argument.entity.options.sort.description")
         );
         register("gamemode", $$0 -> {
            $$0.setSuggestions(($$1x, $$2x) -> {
               String $$3x = $$1x.getRemaining().toLowerCase(Locale.ROOT);
               boolean $$4x = !$$0.hasGamemodeNotEquals();
               boolean $$5 = true;
               if (!$$3x.isEmpty()) {
                  if ($$3x.charAt(0) == '!') {
                     $$4x = false;
                     $$3x = $$3x.substring(1);
                  } else {
                     $$5 = false;
                  }
               }

               for (GameType $$6 : GameType.values()) {
                  if ($$6.getName().toLowerCase(Locale.ROOT).startsWith($$3x)) {
                     if ($$5) {
                        $$1x.suggest("!" + $$6.getName());
                     }

                     if ($$4x) {
                        $$1x.suggest($$6.getName());
                     }
                  }
               }

               return $$1x.buildFuture();
            });
            int $$1 = $$0.getReader().getCursor();
            boolean $$2 = $$0.shouldInvertValue();
            if ($$0.hasGamemodeNotEquals() && !$$2) {
               $$0.getReader().setCursor($$1);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext($$0.getReader(), "gamemode");
            } else {
               String $$3 = $$0.getReader().readUnquotedString();
               GameType $$4 = GameType.byName($$3, null);
               if ($$4 == null) {
                  $$0.getReader().setCursor($$1);
                  throw ERROR_GAME_MODE_INVALID.createWithContext($$0.getReader(), $$3);
               } else {
                  $$0.setIncludesEntities(false);
                  $$0.addPredicate($$2x -> {
                     if ($$2x instanceof ServerPlayer $$3x) {
                        GameType $$4x = $$3x.gameMode();
                        return $$4x == $$4 ^ $$2;
                     } else {
                        return false;
                     }
                  });
                  if ($$2) {
                     $$0.setHasGamemodeNotEquals(true);
                  } else {
                     $$0.setHasGamemodeEquals(true);
                  }
               }
            }
         }, $$0 -> !$$0.hasGamemodeEquals(), Component.translatable("argument.entity.options.gamemode.description"));
         register("team", $$0 -> {
            boolean $$1 = $$0.shouldInvertValue();
            String $$2 = $$0.getReader().readUnquotedString();
            $$0.addPredicate($$2x -> {
               Team $$3 = $$2x.getTeam();
               String $$4 = $$3 == null ? "" : $$3.getName();
               return $$4.equals($$2) != $$1;
            });
            if ($$1) {
               $$0.setHasTeamNotEquals(true);
            } else {
               $$0.setHasTeamEquals(true);
            }
         }, $$0 -> !$$0.hasTeamEquals(), Component.translatable("argument.entity.options.team.description"));
         register(
            "type",
            $$0 -> {
               $$0.setSuggestions(
                  ($$1x, $$2x) -> {
                     net.minecraft.commands.SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), $$1x, String.valueOf('!'));
                     net.minecraft.commands.SharedSuggestionProvider.suggestResource(
                        BuiltInRegistries.ENTITY_TYPE.getTags().map($$0xx -> $$0xx.key().location()), $$1x, "!#"
                     );
                     if (!$$0.isTypeLimitedInversely()) {
                        net.minecraft.commands.SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), $$1x);
                        net.minecraft.commands.SharedSuggestionProvider.suggestResource(
                           BuiltInRegistries.ENTITY_TYPE.getTags().map($$0xx -> $$0xx.key().location()), $$1x, String.valueOf('#')
                        );
                     }

                     return $$1x.buildFuture();
                  }
               );
               int $$1 = $$0.getReader().getCursor();
               boolean $$2 = $$0.shouldInvertValue();
               if ($$0.isTypeLimitedInversely() && !$$2) {
                  $$0.getReader().setCursor($$1);
                  throw ERROR_INAPPLICABLE_OPTION.createWithContext($$0.getReader(), "type");
               } else {
                  if ($$2) {
                     $$0.setTypeLimitedInversely();
                  }

                  if ($$0.isTag()) {
                     TagKey<EntityType<?>> $$3 = TagKey.create(Registries.ENTITY_TYPE, Identifier.read($$0.getReader()));
                     $$0.addPredicate($$2x -> $$2x.getType().is($$3) != $$2);
                  } else {
                     Identifier $$4 = Identifier.read($$0.getReader());
                     EntityType<?> $$5 = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getOptional($$4).orElseThrow(() -> {
                        $$0.getReader().setCursor($$1);
                        return ERROR_ENTITY_TYPE_INVALID.createWithContext($$0.getReader(), $$4.toString());
                     });
                     if (Objects.equals(EntityType.PLAYER, $$5) && !$$2) {
                        $$0.setIncludesEntities(false);
                     }

                     $$0.addPredicate($$2x -> Objects.equals($$5, $$2x.getType()) != $$2);
                     if (!$$2) {
                        $$0.limitToType($$5);
                     }
                  }
               }
            },
            $$0 -> !$$0.isTypeLimited(),
            Component.translatable("argument.entity.options.type.description")
         );
         register("tag", $$0 -> {
            boolean $$1 = $$0.shouldInvertValue();
            String $$2 = $$0.getReader().readUnquotedString();
            $$0.addPredicate($$2x -> "".equals($$2) ? $$2x.getTags().isEmpty() != $$1 : $$2x.getTags().contains($$2) != $$1);
         }, $$0 -> true, Component.translatable("argument.entity.options.tag.description"));
         register("nbt", $$0 -> {
            boolean $$1 = $$0.shouldInvertValue();
            CompoundTag $$2 = TagParser.parseCompoundAsArgument($$0.getReader());
            $$0.addPredicate($$2x -> {
               ScopedCollector $$3 = new ScopedCollector($$2x.problemPath(), LOGGER);

               boolean var9;
               try {
                  TagValueOutput $$4 = TagValueOutput.createWithContext($$3, $$2x.registryAccess());
                  $$2x.saveWithoutId($$4);
                  if ($$2x instanceof ServerPlayer $$5) {
                     ItemStack $$6 = $$5.getInventory().getSelectedItem();
                     if (!$$6.isEmpty()) {
                        $$4.store("SelectedItem", ItemStack.CODEC, $$6);
                     }
                  }

                  var9 = NbtUtils.compareNbt($$2, $$4.buildResult(), true) != $$1;
               } catch (Throwable var8) {
                  try {
                     $$3.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }

                  throw var8;
               }

               $$3.close();
               return var9;
            });
         }, $$0 -> true, Component.translatable("argument.entity.options.nbt.description"));
         register("scores", $$0 -> {
            StringReader $$1 = $$0.getReader();
            Map<String, Ints> $$2 = Maps.newHashMap();
            $$1.expect('{');
            $$1.skipWhitespace();

            while ($$1.canRead() && $$1.peek() != '}') {
               $$1.skipWhitespace();
               String $$3 = $$1.readUnquotedString();
               $$1.skipWhitespace();
               $$1.expect('=');
               $$1.skipWhitespace();
               Ints $$4 = Ints.fromReader($$1);
               $$2.put($$3, $$4);
               $$1.skipWhitespace();
               if ($$1.canRead() && $$1.peek() == ',') {
                  $$1.skip();
               }
            }

            $$1.expect('}');
            if (!$$2.isEmpty()) {
               $$0.addPredicate($$1x -> {
                  Scoreboard $$2x = $$1x.level().getServer().getScoreboard();

                  for (Entry<String, Ints> $$3x : $$2.entrySet()) {
                     Objective $$4x = $$2x.getObjective($$3x.getKey());
                     if ($$4x == null) {
                        return false;
                     }

                     ReadOnlyScoreInfo $$5 = $$2x.getPlayerScoreInfo($$1x, $$4x);
                     if ($$5 == null) {
                        return false;
                     }

                     if (!$$3x.getValue().matches($$5.value())) {
                        return false;
                     }
                  }

                  return true;
               });
            }

            $$0.setHasScores(true);
         }, $$0 -> !$$0.hasScores(), Component.translatable("argument.entity.options.scores.description"));
         register("advancements", $$0 -> {
            StringReader $$1 = $$0.getReader();
            Map<Identifier, Predicate<AdvancementProgress>> $$2 = Maps.newHashMap();
            $$1.expect('{');
            $$1.skipWhitespace();

            while ($$1.canRead() && $$1.peek() != '}') {
               $$1.skipWhitespace();
               Identifier $$3 = Identifier.read($$1);
               $$1.skipWhitespace();
               $$1.expect('=');
               $$1.skipWhitespace();
               if ($$1.canRead() && $$1.peek() == '{') {
                  Map<String, Predicate<CriterionProgress>> $$4 = Maps.newHashMap();
                  $$1.skipWhitespace();
                  $$1.expect('{');
                  $$1.skipWhitespace();

                  while ($$1.canRead() && $$1.peek() != '}') {
                     $$1.skipWhitespace();
                     String $$5 = $$1.readUnquotedString();
                     $$1.skipWhitespace();
                     $$1.expect('=');
                     $$1.skipWhitespace();
                     boolean $$6 = $$1.readBoolean();
                     $$4.put($$5, $$1x -> $$1x.isDone() == $$6);
                     $$1.skipWhitespace();
                     if ($$1.canRead() && $$1.peek() == ',') {
                        $$1.skip();
                     }
                  }

                  $$1.skipWhitespace();
                  $$1.expect('}');
                  $$1.skipWhitespace();
                  $$2.put($$3, $$1x -> {
                     for (Entry<String, Predicate<CriterionProgress>> $$2x : $$4.entrySet()) {
                        CriterionProgress $$3x = $$1x.getCriterion($$2x.getKey());
                        if ($$3x == null || !$$2x.getValue().test($$3x)) {
                           return false;
                        }
                     }

                     return true;
                  });
               } else {
                  boolean $$7 = $$1.readBoolean();
                  $$2.put($$3, $$1x -> $$1x.isDone() == $$7);
               }

               $$1.skipWhitespace();
               if ($$1.canRead() && $$1.peek() == ',') {
                  $$1.skip();
               }
            }

            $$1.expect('}');
            if (!$$2.isEmpty()) {
               $$0.addPredicate($$1x -> {
                  if (!($$1x instanceof ServerPlayer $$2x)) {
                     return false;
                  } else {
                     PlayerAdvancements $$4 = $$2x.getAdvancements();
                     ServerAdvancementManager $$5x = $$2x.level().getServer().getAdvancements();

                     for (Entry<Identifier, Predicate<AdvancementProgress>> $$6x : $$2.entrySet()) {
                        AdvancementHolder $$7x = $$5x.get($$6x.getKey());
                        if ($$7x == null || !$$6x.getValue().test($$4.getOrStartProgress($$7x))) {
                           return false;
                        }
                     }

                     return true;
                  }
               });
               $$0.setIncludesEntities(false);
            }

            $$0.setHasAdvancements(true);
         }, $$0 -> !$$0.hasAdvancements(), Component.translatable("argument.entity.options.advancements.description"));
         register(
            "predicate",
            $$0 -> {
               boolean $$1 = $$0.shouldInvertValue();
               ResourceKey<LootItemCondition> $$2 = ResourceKey.create(Registries.PREDICATE, Identifier.read($$0.getReader()));
               $$0.addPredicate(
                  $$2x -> {
                     if ($$2x.level() instanceof ServerLevel $$4) {
                        Optional<LootItemCondition> $$6 = $$4.getServer().reloadableRegistries().lookup().get($$2).map(Holder::value);
                        if ($$6.isEmpty()) {
                           return false;
                        } else {
                           LootParams $$7 = new Builder($$4)
                              .withParameter(LootContextParams.THIS_ENTITY, $$2x)
                              .withParameter(LootContextParams.ORIGIN, $$2x.position())
                              .create(LootContextParamSets.SELECTOR);
                           LootContext $$8 = new net.minecraft.world.level.storage.loot.LootContext.Builder($$7).create(Optional.empty());
                           $$8.pushVisitedElement(LootContext.createVisitedEntry($$6.get()));
                           return $$1 ^ $$6.get().test($$8);
                        }
                     } else {
                        return false;
                     }
                  }
               );
            },
            $$0 -> true,
            Component.translatable("argument.entity.options.predicate.description")
         );
      }
   }

   public static EntitySelectorOptions.Modifier get(EntitySelectorParser $$0, String $$1, int $$2) throws CommandSyntaxException {
      EntitySelectorOptions.Option $$3 = OPTIONS.get($$1);
      if ($$3 != null) {
         if ($$3.canUse.test($$0)) {
            return $$3.modifier;
         } else {
            throw ERROR_INAPPLICABLE_OPTION.createWithContext($$0.getReader(), $$1);
         }
      } else {
         $$0.getReader().setCursor($$2);
         throw ERROR_UNKNOWN_OPTION.createWithContext($$0.getReader(), $$1);
      }
   }

   public static void suggestNames(EntitySelectorParser $$0, SuggestionsBuilder $$1) {
      String $$2 = $$1.getRemaining().toLowerCase(Locale.ROOT);

      for (Entry<String, EntitySelectorOptions.Option> $$3 : OPTIONS.entrySet()) {
         if ($$3.getValue().canUse.test($$0) && $$3.getKey().toLowerCase(Locale.ROOT).startsWith($$2)) {
            $$1.suggest($$3.getKey() + "=", $$3.getValue().description);
         }
      }
   }

   @FunctionalInterface
   public interface Modifier {
      void handle(EntitySelectorParser var1) throws CommandSyntaxException;
   }

   record Option(EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> canUse, Component description) {
   }
}
