package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetWritableBookPagesFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetWritableBookPagesFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               WritableBookContent.PAGES_CODEC.fieldOf("pages").forGetter($$0x -> $$0x.pages), ListOperation.codec(100).forGetter($$0x -> $$0x.pageOperation)
            )
         )
         .apply($$0, SetWritableBookPagesFunction::new)
   );
   private final List<Filterable<String>> pages;
   private final ListOperation pageOperation;

   protected SetWritableBookPagesFunction(List<LootItemCondition> $$0, List<Filterable<String>> $$1, ListOperation $$2) {
      super($$0);
      this.pages = $$1;
      this.pageOperation = $$2;
   }

   @Override
   protected ItemStack run(ItemStack $$0, LootContext $$1) {
      $$0.update(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY, this::apply);
      return $$0;
   }

   public WritableBookContent apply(WritableBookContent $$0) {
      List<Filterable<String>> $$1 = this.pageOperation.apply($$0.pages(), this.pages, 100);
      return $$0.withReplacedPages($$1);
   }

   @Override
   public LootItemFunctionType<SetWritableBookPagesFunction> getType() {
      return LootItemFunctions.SET_WRITABLE_BOOK_PAGES;
   }
}
