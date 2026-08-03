package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

public class TagBuilder {
   private final List<net.minecraft.tags.TagEntry> entries = new ArrayList<>();

   public static net.minecraft.tags.TagBuilder create() {
      return new net.minecraft.tags.TagBuilder();
   }

   public List<net.minecraft.tags.TagEntry> build() {
      return List.copyOf(this.entries);
   }

   public net.minecraft.tags.TagBuilder add(net.minecraft.tags.TagEntry $$0) {
      this.entries.add($$0);
      return this;
   }

   public net.minecraft.tags.TagBuilder addElement(Identifier $$0) {
      return this.add(net.minecraft.tags.TagEntry.element($$0));
   }

   public net.minecraft.tags.TagBuilder addOptionalElement(Identifier $$0) {
      return this.add(net.minecraft.tags.TagEntry.optionalElement($$0));
   }

   public net.minecraft.tags.TagBuilder addTag(Identifier $$0) {
      return this.add(net.minecraft.tags.TagEntry.tag($$0));
   }

   public net.minecraft.tags.TagBuilder addOptionalTag(Identifier $$0) {
      return this.add(net.minecraft.tags.TagEntry.optionalTag($$0));
   }
}
