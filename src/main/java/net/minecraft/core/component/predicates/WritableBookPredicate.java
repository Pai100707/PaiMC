package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WritableBookContent;

public record WritableBookPredicate(Optional<CollectionPredicate<Filterable<String>, WritableBookPredicate.PagePredicate>> pages)
   implements SingleComponentItemPredicate<WritableBookContent> {
   public static final Codec<WritableBookPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(CollectionPredicate.codec(WritableBookPredicate.PagePredicate.CODEC).optionalFieldOf("pages").forGetter(WritableBookPredicate::pages))
         .apply($$0, WritableBookPredicate::new)
   );

   public DataComponentType<WritableBookContent> componentType() {
      return DataComponents.WRITABLE_BOOK_CONTENT;
   }

   public boolean matches(WritableBookContent $$0) {
      return !this.pages.isPresent() || this.pages.get().test($$0.pages());
   }

   public record PagePredicate(String contents) implements Predicate<Filterable<String>> {
      public static final Codec<WritableBookPredicate.PagePredicate> CODEC = Codec.STRING
         .xmap(WritableBookPredicate.PagePredicate::new, WritableBookPredicate.PagePredicate::contents);

      public boolean test(Filterable<String> $$0) {
         return ((String)$$0.raw()).equals(this.contents);
      }
   }
}
