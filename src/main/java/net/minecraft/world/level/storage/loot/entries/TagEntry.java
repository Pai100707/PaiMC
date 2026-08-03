package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry extends LootPoolSingletonContainer {
   public static final MapCodec<TagEntry> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(TagKey.codec(Registries.ITEM).fieldOf("name").forGetter($$0x -> $$0x.tag), Codec.BOOL.fieldOf("expand").forGetter($$0x -> $$0x.expand))
         .and(singletonFields($$0))
         .apply($$0, TagEntry::new)
   );
   private final TagKey<Item> tag;
   private final boolean expand;

   private TagEntry(TagKey<Item> $$0, boolean $$1, int $$2, int $$3, List<LootItemCondition> $$4, List<LootItemFunction> $$5) {
      super($$2, $$3, $$4, $$5);
      this.tag = $$0;
      this.expand = $$1;
   }

   @Override
   public LootPoolEntryType getType() {
      return LootPoolEntries.TAG;
   }

   @Override
   public void createItemStack(Consumer<ItemStack> $$0, LootContext $$1) {
      BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).forEach($$1x -> $$0.accept(new ItemStack($$1x)));
   }

   private boolean expandTag(LootContext $$0, Consumer<LootPoolEntry> $$1) {
      if (!this.canRun($$0)) {
         return false;
      } else {
         for (final Holder<Item> $$2 : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
            $$1.accept(new LootPoolSingletonContainer.EntryBase() {
               @Override
               public void createItemStack(Consumer<ItemStack> $$0, LootContext $$1x) {
                  $$0.accept(new ItemStack($$2));
               }
            });
         }

         return true;
      }
   }

   @Override
   public boolean expand(LootContext $$0, Consumer<LootPoolEntry> $$1) {
      return this.expand ? this.expandTag($$0, $$1) : super.expand($$0, $$1);
   }

   public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> $$0) {
      return simpleBuilder(($$1, $$2, $$3, $$4) -> new TagEntry($$0, false, $$1, $$2, $$3, $$4));
   }

   public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> $$0) {
      return simpleBuilder(($$1, $$2, $$3, $$4) -> new TagEntry($$0, true, $$1, $$2, $$3, $$4));
   }
}
