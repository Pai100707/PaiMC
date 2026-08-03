package net.minecraft.advancements.criterion;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;

public record CollectionPredicate<T, P extends Predicate<T>>(
   Optional<CollectionContentsPredicate<T, P>> contains, Optional<CollectionCountsPredicate<T, P>> counts, Optional<MinMaxBounds.Ints> size
) implements Predicate<Iterable<T>> {
   public static <T, P extends Predicate<T>> Codec<CollectionPredicate<T, P>> codec(Codec<P> $$0) {
      return RecordCodecBuilder.create(
         $$1 -> $$1.group(
               CollectionContentsPredicate.codec($$0).optionalFieldOf("contains").forGetter(CollectionPredicate::contains),
               CollectionCountsPredicate.codec($$0).optionalFieldOf("count").forGetter(CollectionPredicate::counts),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("size").forGetter(CollectionPredicate::size)
            )
            .apply($$1, CollectionPredicate::new)
      );
   }

   public boolean test(Iterable<T> $$0) {
      if (this.contains.isPresent() && !this.contains.get().test($$0)) {
         return false;
      } else {
         return this.counts.isPresent() && !this.counts.get().test($$0) ? false : !this.size.isPresent() || this.size.get().matches(Iterables.size($$0));
      }
   }
}
