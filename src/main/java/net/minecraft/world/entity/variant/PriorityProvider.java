package net.minecraft.world.entity.variant;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

public interface PriorityProvider<Context, Condition extends PriorityProvider.SelectorCondition<Context>> {
   List<PriorityProvider.Selector<Context, Condition>> selectors();

   static <C, T> Stream<T> select(Stream<T> $$0, Function<T, PriorityProvider<C, ?>> $$1, C $$2) {
      List<PriorityProvider.UnpackedEntry<C, T>> $$3 = new ArrayList<>();
      $$0.forEach(
         $$2x -> {
            PriorityProvider<C, ?> $$3x = $$1.apply((T)$$2x);

            for (PriorityProvider.Selector<C, ?> $$4x : $$3x.selectors()) {
               $$3.add(
                  new PriorityProvider.UnpackedEntry<>(
                     (T)$$2x,
                     $$4x.priority(),
                     (PriorityProvider.SelectorCondition<C>)DataFixUtils.orElseGet($$4x.condition(), PriorityProvider.SelectorCondition::alwaysTrue)
                  )
               );
            }
         }
      );
      $$3.sort(PriorityProvider.UnpackedEntry.HIGHEST_PRIORITY_FIRST);
      Iterator<PriorityProvider.UnpackedEntry<C, T>> $$4 = $$3.iterator();
      int $$5 = Integer.MIN_VALUE;

      while ($$4.hasNext()) {
         PriorityProvider.UnpackedEntry<C, T> $$6 = $$4.next();
         if ($$6.priority < $$5) {
            $$4.remove();
         } else if ($$6.condition.test($$2)) {
            $$5 = $$6.priority;
         } else {
            $$4.remove();
         }
      }

      return $$3.stream().map(PriorityProvider.UnpackedEntry::entry);
   }

   static <C, T> Optional<T> pick(Stream<T> $$0, Function<T, PriorityProvider<C, ?>> $$1, RandomSource $$2, C $$3) {
      List<T> $$4 = select($$0, $$1, $$3).toList();
      return Util.getRandomSafe($$4, $$2);
   }

   static <Context, Condition extends PriorityProvider.SelectorCondition<Context>> List<PriorityProvider.Selector<Context, Condition>> single(
      Condition $$0, int $$1
   ) {
      return List.of(new PriorityProvider.Selector<>($$0, $$1));
   }

   static <Context, Condition extends PriorityProvider.SelectorCondition<Context>> List<PriorityProvider.Selector<Context, Condition>> alwaysTrue(int $$0) {
      return List.of(new PriorityProvider.Selector<>(Optional.empty(), $$0));
   }

   public record Selector<Context, Condition extends PriorityProvider.SelectorCondition<Context>>(Optional<Condition> condition, int priority) {
      public Selector(Condition $$0, int $$1) {
         this(Optional.of($$0), $$1);
      }

      public Selector(int $$0) {
         this(Optional.empty(), $$0);
      }

      public static <Context, Condition extends PriorityProvider.SelectorCondition<Context>> Codec<PriorityProvider.Selector<Context, Condition>> codec(
         Codec<Condition> $$0
      ) {
         return RecordCodecBuilder.create(
            $$1 -> $$1.group(
                  $$0.optionalFieldOf("condition").forGetter(PriorityProvider.Selector::condition),
                  Codec.INT.fieldOf("priority").forGetter(PriorityProvider.Selector::priority)
               )
               .apply($$1, PriorityProvider.Selector::new)
         );
      }
   }

   @FunctionalInterface
   public interface SelectorCondition<C> extends Predicate<C> {
      static <C> PriorityProvider.SelectorCondition<C> alwaysTrue() {
         return $$0 -> true;
      }
   }

   public record UnpackedEntry<C, T>(T entry, int priority, PriorityProvider.SelectorCondition<C> condition) {
      public static final Comparator<PriorityProvider.UnpackedEntry<?, ?>> HIGHEST_PRIORITY_FIRST = Comparator.comparingInt(
            PriorityProvider.UnpackedEntry::priority
         )
         .reversed();
   }
}
