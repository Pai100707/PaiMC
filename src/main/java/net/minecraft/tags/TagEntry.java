package net.minecraft.tags;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ExtraCodecs.TagOrElementLocation;

public class TagEntry {
   private static final Codec<net.minecraft.tags.TagEntry> FULL_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.TAG_OR_ELEMENT_ID.fieldOf("id").forGetter(net.minecraft.tags.TagEntry::elementOrTag),
            Codec.BOOL.optionalFieldOf("required", true).forGetter($$0x -> $$0x.required)
         )
         .apply($$0, net.minecraft.tags.TagEntry::new)
   );
   public static final Codec<net.minecraft.tags.TagEntry> CODEC = Codec.either(ExtraCodecs.TAG_OR_ELEMENT_ID, FULL_CODEC)
      .xmap(
         $$0 -> (net.minecraft.tags.TagEntry)$$0.map($$0x -> new net.minecraft.tags.TagEntry($$0x, true), $$0x -> $$0x),
         $$0 -> $$0.required ? Either.left($$0.elementOrTag()) : Either.right($$0)
      );
   private final Identifier id;
   private final boolean tag;
   private final boolean required;

   private TagEntry(Identifier $$0, boolean $$1, boolean $$2) {
      this.id = $$0;
      this.tag = $$1;
      this.required = $$2;
   }

   private TagEntry(TagOrElementLocation $$0, boolean $$1) {
      this.id = $$0.id();
      this.tag = $$0.tag();
      this.required = $$1;
   }

   private TagOrElementLocation elementOrTag() {
      return new TagOrElementLocation(this.id, this.tag);
   }

   public static net.minecraft.tags.TagEntry element(Identifier $$0) {
      return new net.minecraft.tags.TagEntry($$0, false, true);
   }

   public static net.minecraft.tags.TagEntry optionalElement(Identifier $$0) {
      return new net.minecraft.tags.TagEntry($$0, false, false);
   }

   public static net.minecraft.tags.TagEntry tag(Identifier $$0) {
      return new net.minecraft.tags.TagEntry($$0, true, true);
   }

   public static net.minecraft.tags.TagEntry optionalTag(Identifier $$0) {
      return new net.minecraft.tags.TagEntry($$0, true, false);
   }

   public <T> boolean build(net.minecraft.tags.TagEntry.Lookup<T> $$0, Consumer<T> $$1) {
      if (this.tag) {
         Collection<T> $$2 = $$0.tag(this.id);
         if ($$2 == null) {
            return !this.required;
         }

         $$2.forEach($$1);
      } else {
         T $$3 = $$0.element(this.id, this.required);
         if ($$3 == null) {
            return !this.required;
         }

         $$1.accept($$3);
      }

      return true;
   }

   public void visitRequiredDependencies(Consumer<Identifier> $$0) {
      if (this.tag && this.required) {
         $$0.accept(this.id);
      }
   }

   public void visitOptionalDependencies(Consumer<Identifier> $$0) {
      if (this.tag && !this.required) {
         $$0.accept(this.id);
      }
   }

   public boolean verifyIfPresent(Predicate<Identifier> $$0, Predicate<Identifier> $$1) {
      return !this.required || (this.tag ? $$1 : $$0).test(this.id);
   }

   @Override
   public String toString() {
      StringBuilder $$0 = new StringBuilder();
      if (this.tag) {
         $$0.append('#');
      }

      $$0.append(this.id);
      if (!this.required) {
         $$0.append('?');
      }

      return $$0.toString();
   }

   public interface Lookup<T> {
      
      T element(Identifier var1, boolean var2);

      
      Collection<T> tag(Identifier var1);
   }
}
