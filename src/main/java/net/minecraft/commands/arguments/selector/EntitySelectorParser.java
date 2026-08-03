package net.minecraft.commands.arguments.selector;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.MinMaxBounds.FloatDegrees;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSetSupplier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntitySelectorParser {
   public static final char SYNTAX_SELECTOR_START = '@';
   private static final char SYNTAX_OPTIONS_START = '[';
   private static final char SYNTAX_OPTIONS_END = ']';
   public static final char SYNTAX_OPTIONS_KEY_VALUE_SEPARATOR = '=';
   private static final char SYNTAX_OPTIONS_SEPARATOR = ',';
   public static final char SYNTAX_NOT = '!';
   public static final char SYNTAX_TAG = '#';
   private static final char SELECTOR_NEAREST_PLAYER = 'p';
   private static final char SELECTOR_ALL_PLAYERS = 'a';
   private static final char SELECTOR_RANDOM_PLAYERS = 'r';
   private static final char SELECTOR_CURRENT_ENTITY = 's';
   private static final char SELECTOR_ALL_ENTITIES = 'e';
   private static final char SELECTOR_NEAREST_ENTITY = 'n';
   public static final SimpleCommandExceptionType ERROR_INVALID_NAME_OR_UUID = new SimpleCommandExceptionType(Component.translatable("argument.entity.invalid"));
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_SELECTOR_TYPE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.entity.selector.unknown", new Object[]{$$0})
   );
   public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(
      Component.translatable("argument.entity.selector.not_allowed")
   );
   public static final SimpleCommandExceptionType ERROR_MISSING_SELECTOR_TYPE = new SimpleCommandExceptionType(
      Component.translatable("argument.entity.selector.missing")
   );
   public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_OPTIONS = new SimpleCommandExceptionType(
      Component.translatable("argument.entity.options.unterminated")
   );
   public static final DynamicCommandExceptionType ERROR_EXPECTED_OPTION_VALUE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.entity.options.valueless", new Object[]{$$0})
   );
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_NEAREST = ($$0, $$1) -> $$1.sort(
      ($$1x, $$2) -> Doubles.compare($$1x.distanceToSqr($$0), $$2.distanceToSqr($$0))
   );
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_FURTHEST = ($$0, $$1) -> $$1.sort(
      ($$1x, $$2) -> Doubles.compare($$2.distanceToSqr($$0), $$1x.distanceToSqr($$0))
   );
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_RANDOM = ($$0, $$1) -> Collections.shuffle($$1);
   public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = ($$0, $$1) -> $$0.buildFuture();
   private final StringReader reader;
   private final boolean allowSelectors;
   private int maxResults;
   private boolean includesEntities;
   private boolean worldLimited;
   @Nullable
   private net.minecraft.advancements.criterion.MinMaxBounds.Doubles distance;
   @Nullable
   private Ints level;
   @Nullable
   private Double x;
   @Nullable
   private Double y;
   @Nullable
   private Double z;
   @Nullable
   private Double deltaX;
   @Nullable
   private Double deltaY;
   @Nullable
   private Double deltaZ;
   @Nullable
   private FloatDegrees rotX;
   @Nullable
   private FloatDegrees rotY;
   private final List<Predicate<Entity>> predicates = new ArrayList<>();
   private BiConsumer<Vec3, List<? extends Entity>> order = EntitySelector.ORDER_ARBITRARY;
   private boolean currentEntity;
   @Nullable
   private String playerName;
   private int startPosition;
   @Nullable
   private UUID entityUUID;
   private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;
   private boolean hasNameEquals;
   private boolean hasNameNotEquals;
   private boolean isLimited;
   private boolean isSorted;
   private boolean hasGamemodeEquals;
   private boolean hasGamemodeNotEquals;
   private boolean hasTeamEquals;
   private boolean hasTeamNotEquals;
   @Nullable
   private EntityType<?> type;
   private boolean typeInverse;
   private boolean hasScores;
   private boolean hasAdvancements;
   private boolean usesSelectors;

   public EntitySelectorParser(StringReader $$0, boolean $$1) {
      this.reader = $$0;
      this.allowSelectors = $$1;
   }

   public static <S> boolean allowSelectors(S $$0) {
      return $$0 instanceof PermissionSetSupplier $$1 && $$1.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS);
   }

   @Deprecated
   public static boolean allowSelectors(PermissionSetSupplier $$0) {
      return $$0.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS);
   }

   public EntitySelector getSelector() {
      AABB $$2;
      if (this.deltaX == null && this.deltaY == null && this.deltaZ == null) {
         if (this.distance != null && this.distance.max().isPresent()) {
            double $$1 = (Double)this.distance.max().get();
            $$2 = new AABB(-$$1, -$$1, -$$1, $$1 + 1.0, $$1 + 1.0, $$1 + 1.0);
         } else {
            $$2 = null;
         }
      } else {
         $$2 = this.createAabb(this.deltaX == null ? 0.0 : this.deltaX, this.deltaY == null ? 0.0 : this.deltaY, this.deltaZ == null ? 0.0 : this.deltaZ);
      }

      Function<Vec3, Vec3> $$4;
      if (this.x == null && this.y == null && this.z == null) {
         $$4 = $$0 -> $$0;
      } else {
         $$4 = $$0 -> new Vec3(this.x == null ? $$0.x : this.x, this.y == null ? $$0.y : this.y, this.z == null ? $$0.z : this.z);
      }

      return new EntitySelector(
         this.maxResults,
         this.includesEntities,
         this.worldLimited,
         List.copyOf(this.predicates),
         this.distance,
         $$4,
         $$2,
         this.order,
         this.currentEntity,
         this.playerName,
         this.entityUUID,
         this.type,
         this.usesSelectors
      );
   }

   private AABB createAabb(double $$0, double $$1, double $$2) {
      boolean $$3 = $$0 < 0.0;
      boolean $$4 = $$1 < 0.0;
      boolean $$5 = $$2 < 0.0;
      double $$6 = $$3 ? $$0 : 0.0;
      double $$7 = $$4 ? $$1 : 0.0;
      double $$8 = $$5 ? $$2 : 0.0;
      double $$9 = ($$3 ? 0.0 : $$0) + 1.0;
      double $$10 = ($$4 ? 0.0 : $$1) + 1.0;
      double $$11 = ($$5 ? 0.0 : $$2) + 1.0;
      return new AABB($$6, $$7, $$8, $$9, $$10, $$11);
   }

   private void finalizePredicates() {
      if (this.rotX != null) {
         this.predicates.add(this.createRotationPredicate(this.rotX, Entity::getXRot));
      }

      if (this.rotY != null) {
         this.predicates.add(this.createRotationPredicate(this.rotY, Entity::getYRot));
      }

      if (this.level != null) {
         this.predicates.add($$0 -> $$0 instanceof ServerPlayer $$1 && this.level.matches($$1.experienceLevel));
      }
   }

   private Predicate<Entity> createRotationPredicate(FloatDegrees $$0, ToFloatFunction<Entity> $$1) {
      float $$2 = Mth.wrapDegrees($$0.min().orElse(0.0F));
      float $$3 = Mth.wrapDegrees($$0.max().orElse(359.0F));
      return $$3x -> {
         float $$4 = Mth.wrapDegrees($$1.applyAsFloat($$3x));
         return $$2 > $$3 ? $$4 >= $$2 || $$4 <= $$3 : $$4 >= $$2 && $$4 <= $$3;
      };
   }

   protected void parseSelector() throws CommandSyntaxException {
      this.usesSelectors = true;
      this.suggestions = this::suggestSelector;
      if (!this.reader.canRead()) {
         throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(this.reader);
      } else {
         int $$0 = this.reader.getCursor();
         char $$1 = this.reader.read();

         if (switch ($$1) {
            case 'a' -> {
               this.maxResults = Integer.MAX_VALUE;
               this.includesEntities = false;
               this.order = EntitySelector.ORDER_ARBITRARY;
               this.limitToType(EntityType.PLAYER);
               yield false;
            }
            default -> {
               this.reader.setCursor($$0);
               throw ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(this.reader, "@" + $$1);
            }
            case 'e' -> {
               this.maxResults = Integer.MAX_VALUE;
               this.includesEntities = true;
               this.order = EntitySelector.ORDER_ARBITRARY;
               yield true;
            }
            case 'n' -> {
               this.maxResults = 1;
               this.includesEntities = true;
               this.order = ORDER_NEAREST;
               yield true;
            }
            case 'p' -> {
               this.maxResults = 1;
               this.includesEntities = false;
               this.order = ORDER_NEAREST;
               this.limitToType(EntityType.PLAYER);
               yield false;
            }
            case 'r' -> {
               this.maxResults = 1;
               this.includesEntities = false;
               this.order = ORDER_RANDOM;
               this.limitToType(EntityType.PLAYER);
               yield false;
            }
            case 's' -> {
               this.maxResults = 1;
               this.includesEntities = true;
               this.currentEntity = true;
               yield false;
            }
         }) {
            this.predicates.add(Entity::isAlive);
         }

         this.suggestions = this::suggestOpenOptions;
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.reader.skip();
            this.suggestions = this::suggestOptionsKeyOrClose;
            this.parseOptions();
         }
      }
   }

   protected void parseNameOrUUID() throws CommandSyntaxException {
      if (this.reader.canRead()) {
         this.suggestions = this::suggestName;
      }

      int $$0 = this.reader.getCursor();
      String $$1 = this.reader.readString();

      try {
         this.entityUUID = UUID.fromString($$1);
         this.includesEntities = true;
      } catch (IllegalArgumentException var4) {
         if ($$1.isEmpty() || $$1.length() > 16) {
            this.reader.setCursor($$0);
            throw ERROR_INVALID_NAME_OR_UUID.createWithContext(this.reader);
         }

         this.includesEntities = false;
         this.playerName = $$1;
      }

      this.maxResults = 1;
   }

   protected void parseOptions() throws CommandSyntaxException {
      this.suggestions = this::suggestOptionsKey;
      this.reader.skipWhitespace();

      while (this.reader.canRead() && this.reader.peek() != ']') {
         this.reader.skipWhitespace();
         int $$0 = this.reader.getCursor();
         String $$1 = this.reader.readString();
         EntitySelectorOptions.Modifier $$2 = EntitySelectorOptions.get(this, $$1, $$0);
         this.reader.skipWhitespace();
         if (!this.reader.canRead() || this.reader.peek() != '=') {
            this.reader.setCursor($$0);
            throw ERROR_EXPECTED_OPTION_VALUE.createWithContext(this.reader, $$1);
         }

         this.reader.skip();
         this.reader.skipWhitespace();
         this.suggestions = SUGGEST_NOTHING;
         $$2.handle(this);
         this.reader.skipWhitespace();
         this.suggestions = this::suggestOptionsNextOrClose;
         if (this.reader.canRead()) {
            if (this.reader.peek() != ',') {
               if (this.reader.peek() != ']') {
                  throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
               }
               break;
            }

            this.reader.skip();
            this.suggestions = this::suggestOptionsKey;
         }
      }

      if (this.reader.canRead()) {
         this.reader.skip();
         this.suggestions = SUGGEST_NOTHING;
      } else {
         throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
      }
   }

   public boolean shouldInvertValue() {
      this.reader.skipWhitespace();
      if (this.reader.canRead() && this.reader.peek() == '!') {
         this.reader.skip();
         this.reader.skipWhitespace();
         return true;
      } else {
         return false;
      }
   }

   public boolean isTag() {
      this.reader.skipWhitespace();
      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.reader.skip();
         this.reader.skipWhitespace();
         return true;
      } else {
         return false;
      }
   }

   public StringReader getReader() {
      return this.reader;
   }

   public void addPredicate(Predicate<Entity> $$0) {
      this.predicates.add($$0);
   }

   public void setWorldLimited() {
      this.worldLimited = true;
   }

   @Nullable
   public net.minecraft.advancements.criterion.MinMaxBounds.Doubles getDistance() {
      return this.distance;
   }

   public void setDistance(net.minecraft.advancements.criterion.MinMaxBounds.Doubles $$0) {
      this.distance = $$0;
   }

   @Nullable
   public Ints getLevel() {
      return this.level;
   }

   public void setLevel(Ints $$0) {
      this.level = $$0;
   }

   @Nullable
   public FloatDegrees getRotX() {
      return this.rotX;
   }

   public void setRotX(FloatDegrees $$0) {
      this.rotX = $$0;
   }

   @Nullable
   public FloatDegrees getRotY() {
      return this.rotY;
   }

   public void setRotY(FloatDegrees $$0) {
      this.rotY = $$0;
   }

   @Nullable
   public Double getX() {
      return this.x;
   }

   @Nullable
   public Double getY() {
      return this.y;
   }

   @Nullable
   public Double getZ() {
      return this.z;
   }

   public void setX(double $$0) {
      this.x = $$0;
   }

   public void setY(double $$0) {
      this.y = $$0;
   }

   public void setZ(double $$0) {
      this.z = $$0;
   }

   public void setDeltaX(double $$0) {
      this.deltaX = $$0;
   }

   public void setDeltaY(double $$0) {
      this.deltaY = $$0;
   }

   public void setDeltaZ(double $$0) {
      this.deltaZ = $$0;
   }

   @Nullable
   public Double getDeltaX() {
      return this.deltaX;
   }

   @Nullable
   public Double getDeltaY() {
      return this.deltaY;
   }

   @Nullable
   public Double getDeltaZ() {
      return this.deltaZ;
   }

   public void setMaxResults(int $$0) {
      this.maxResults = $$0;
   }

   public void setIncludesEntities(boolean $$0) {
      this.includesEntities = $$0;
   }

   public BiConsumer<Vec3, List<? extends Entity>> getOrder() {
      return this.order;
   }

   public void setOrder(BiConsumer<Vec3, List<? extends Entity>> $$0) {
      this.order = $$0;
   }

   public EntitySelector parse() throws CommandSyntaxException {
      this.startPosition = this.reader.getCursor();
      this.suggestions = this::suggestNameOrSelector;
      if (this.reader.canRead() && this.reader.peek() == '@') {
         if (!this.allowSelectors) {
            throw ERROR_SELECTORS_NOT_ALLOWED.createWithContext(this.reader);
         }

         this.reader.skip();
         this.parseSelector();
      } else {
         this.parseNameOrUUID();
      }

      this.finalizePredicates();
      return this.getSelector();
   }

   private static void fillSelectorSuggestions(SuggestionsBuilder $$0) {
      $$0.suggest("@p", Component.translatable("argument.entity.selector.nearestPlayer"));
      $$0.suggest("@a", Component.translatable("argument.entity.selector.allPlayers"));
      $$0.suggest("@r", Component.translatable("argument.entity.selector.randomPlayer"));
      $$0.suggest("@s", Component.translatable("argument.entity.selector.self"));
      $$0.suggest("@e", Component.translatable("argument.entity.selector.allEntities"));
      $$0.suggest("@n", Component.translatable("argument.entity.selector.nearestEntity"));
   }

   private CompletableFuture<Suggestions> suggestNameOrSelector(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      $$1.accept($$0);
      if (this.allowSelectors) {
         fillSelectorSuggestions($$0);
      }

      return $$0.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestName(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      SuggestionsBuilder $$2 = $$0.createOffset(this.startPosition);
      $$1.accept($$2);
      return $$0.add($$2).buildFuture();
   }

   private CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      SuggestionsBuilder $$2 = $$0.createOffset($$0.getStart() - 1);
      fillSelectorSuggestions($$2);
      $$0.add($$2);
      return $$0.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      $$0.suggest(String.valueOf('['));
      return $$0.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      $$0.suggest(String.valueOf(']'));
      EntitySelectorOptions.suggestNames(this, $$0);
      return $$0.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsKey(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      EntitySelectorOptions.suggestNames(this, $$0);
      return $$0.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsNextOrClose(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      $$0.suggest(String.valueOf(','));
      $$0.suggest(String.valueOf(']'));
      return $$0.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      $$0.suggest(String.valueOf('='));
      return $$0.buildFuture();
   }

   public boolean isCurrentEntity() {
      return this.currentEntity;
   }

   public void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> $$0) {
      this.suggestions = $$0;
   }

   public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder $$0, Consumer<SuggestionsBuilder> $$1) {
      return this.suggestions.apply($$0.createOffset(this.reader.getCursor()), $$1);
   }

   public boolean hasNameEquals() {
      return this.hasNameEquals;
   }

   public void setHasNameEquals(boolean $$0) {
      this.hasNameEquals = $$0;
   }

   public boolean hasNameNotEquals() {
      return this.hasNameNotEquals;
   }

   public void setHasNameNotEquals(boolean $$0) {
      this.hasNameNotEquals = $$0;
   }

   public boolean isLimited() {
      return this.isLimited;
   }

   public void setLimited(boolean $$0) {
      this.isLimited = $$0;
   }

   public boolean isSorted() {
      return this.isSorted;
   }

   public void setSorted(boolean $$0) {
      this.isSorted = $$0;
   }

   public boolean hasGamemodeEquals() {
      return this.hasGamemodeEquals;
   }

   public void setHasGamemodeEquals(boolean $$0) {
      this.hasGamemodeEquals = $$0;
   }

   public boolean hasGamemodeNotEquals() {
      return this.hasGamemodeNotEquals;
   }

   public void setHasGamemodeNotEquals(boolean $$0) {
      this.hasGamemodeNotEquals = $$0;
   }

   public boolean hasTeamEquals() {
      return this.hasTeamEquals;
   }

   public void setHasTeamEquals(boolean $$0) {
      this.hasTeamEquals = $$0;
   }

   public boolean hasTeamNotEquals() {
      return this.hasTeamNotEquals;
   }

   public void setHasTeamNotEquals(boolean $$0) {
      this.hasTeamNotEquals = $$0;
   }

   public void limitToType(EntityType<?> $$0) {
      this.type = $$0;
   }

   public void setTypeLimitedInversely() {
      this.typeInverse = true;
   }

   public boolean isTypeLimited() {
      return this.type != null;
   }

   public boolean isTypeLimitedInversely() {
      return this.typeInverse;
   }

   public boolean hasScores() {
      return this.hasScores;
   }

   public void setHasScores(boolean $$0) {
      this.hasScores = $$0;
   }

   public boolean hasAdvancements() {
      return this.hasAdvancements;
   }

   public void setHasAdvancements(boolean $$0) {
      this.hasAdvancements = $$0;
   }
}
