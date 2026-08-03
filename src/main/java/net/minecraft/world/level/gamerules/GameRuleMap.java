package net.minecraft.world.level.gamerules;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jspecify.annotations.Nullable;

public final class GameRuleMap {
   public static final Codec<GameRuleMap> CODEC = Codec.dispatchedMap(BuiltInRegistries.GAME_RULE.byNameCodec(), GameRule::valueCodec)
      .xmap(GameRuleMap::ofTrusted, GameRuleMap::map);
   private final Reference2ObjectMap<GameRule<?>, Object> map;

   GameRuleMap(Reference2ObjectMap<GameRule<?>, Object> $$0) {
      this.map = $$0;
   }

   private static GameRuleMap ofTrusted(Map<GameRule<?>, Object> $$0) {
      return new GameRuleMap(new Reference2ObjectOpenHashMap($$0));
   }

   public static GameRuleMap of() {
      return new GameRuleMap(new Reference2ObjectOpenHashMap());
   }

   public static GameRuleMap of(Stream<GameRule<?>> $$0) {
      Reference2ObjectOpenHashMap<GameRule<?>, Object> $$1 = new Reference2ObjectOpenHashMap();
      $$0.forEach($$1x -> $$1.put($$1x, $$1x.defaultValue()));
      return new GameRuleMap($$1);
   }

   public static GameRuleMap copyOf(GameRuleMap $$0) {
      return new GameRuleMap(new Reference2ObjectOpenHashMap($$0.map));
   }

   public boolean has(GameRule<?> $$0) {
      return this.map.containsKey($$0);
   }

   @Nullable
   public <T> T get(GameRule<T> $$0) {
      return (T)this.map.get($$0);
   }

   public <T> void set(GameRule<T> $$0, T $$1) {
      this.map.put($$0, $$1);
   }

   @Nullable
   public <T> T remove(GameRule<T> $$0) {
      return (T)this.map.remove($$0);
   }

   public Set<GameRule<?>> keySet() {
      return this.map.keySet();
   }

   public int size() {
      return this.map.size();
   }

   @Override
   public String toString() {
      return this.map.toString();
   }

   public GameRuleMap withOther(GameRuleMap $$0) {
      GameRuleMap $$1 = copyOf(this);
      $$1.setFromIf($$0, $$0x -> true);
      return $$1;
   }

   public void setFromIf(GameRuleMap $$0, Predicate<GameRule<?>> $$1) {
      for (GameRule<?> $$2 : $$0.keySet()) {
         if ($$1.test($$2)) {
            setGameRule($$0, $$2, this);
         }
      }
   }

   private static <T> void setGameRule(GameRuleMap $$0, GameRule<T> $$1, GameRuleMap $$2) {
      $$2.set($$1, Objects.requireNonNull($$0.get($$1)));
   }

   private Reference2ObjectMap<GameRule<?>, Object> map() {
      return this.map;
   }

   @Override
   public boolean equals(Object $$0) {
      if ($$0 == this) {
         return true;
      } else if ($$0 != null && $$0.getClass() == this.getClass()) {
         GameRuleMap $$1 = (GameRuleMap)$$0;
         return Objects.equals(this.map, $$1.map);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.map);
   }

   public static class Builder {
      final Reference2ObjectMap<GameRule<?>, Object> map = new Reference2ObjectOpenHashMap();

      public <T> GameRuleMap.Builder set(GameRule<T> $$0, T $$1) {
         this.map.put($$0, $$1);
         return this;
      }

      public GameRuleMap build() {
         return new GameRuleMap(this.map);
      }
   }
}
