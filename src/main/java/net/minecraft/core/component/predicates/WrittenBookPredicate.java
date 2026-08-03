package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

public record WrittenBookPredicate(
   Optional<CollectionPredicate<Filterable<Component>, WrittenBookPredicate.PagePredicate>> pages,
   Optional<String> author,
   Optional<String> title,
   Ints generation,
   Optional<Boolean> resolved
) implements SingleComponentItemPredicate<WrittenBookContent> {
   public static final Codec<WrittenBookPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            CollectionPredicate.codec(WrittenBookPredicate.PagePredicate.CODEC).optionalFieldOf("pages").forGetter(WrittenBookPredicate::pages),
            Codec.STRING.optionalFieldOf("author").forGetter(WrittenBookPredicate::author),
            Codec.STRING.optionalFieldOf("title").forGetter(WrittenBookPredicate::title),
            Ints.CODEC.optionalFieldOf("generation", Ints.ANY).forGetter(WrittenBookPredicate::generation),
            Codec.BOOL.optionalFieldOf("resolved").forGetter(WrittenBookPredicate::resolved)
         )
         .apply($$0, WrittenBookPredicate::new)
   );

   public DataComponentType<WrittenBookContent> componentType() {
      return DataComponents.WRITTEN_BOOK_CONTENT;
   }

   public boolean matches(WrittenBookContent $$0) {
      if (this.author.isPresent() && !this.author.get().equals($$0.author())) {
         return false;
      } else if (this.title.isPresent() && !this.title.get().equals($$0.title().raw())) {
         return false;
      } else if (!this.generation.matches($$0.generation())) {
         return false;
      } else {
         return this.resolved.isPresent() && this.resolved.get() != $$0.resolved() ? false : !this.pages.isPresent() || this.pages.get().test($$0.pages());
      }
   }

   public record PagePredicate(Component contents) implements Predicate<Filterable<Component>> {
      public static final Codec<WrittenBookPredicate.PagePredicate> CODEC = ComponentSerialization.CODEC
         .xmap(WrittenBookPredicate.PagePredicate::new, WrittenBookPredicate.PagePredicate::contents);

      public boolean test(Filterable<Component> $$0) {
         return ((Component)$$0.raw()).equals(this.contents);
      }
   }
}
