package net.minecraft.world.level.storage.loot.functions;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetWrittenBookPagesFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetWrittenBookPagesFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               WrittenBookContent.PAGES_CODEC.fieldOf("pages").forGetter($$0x -> $$0x.pages),
               ListOperation.UNLIMITED_CODEC.forGetter($$0x -> $$0x.pageOperation)
            )
         )
         .apply($$0, SetWrittenBookPagesFunction::new)
   );
   private final List<Filterable<Component>> pages;
   private final ListOperation pageOperation;

   protected SetWrittenBookPagesFunction(List<LootItemCondition> $$0, List<Filterable<Component>> $$1, ListOperation $$2) {
      super($$0);
      this.pages = $$1;
      this.pageOperation = $$2;
   }

   @Override
   protected ItemStack run(ItemStack $$0, LootContext $$1) {
      $$0.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, this::apply);
      return $$0;
   }

   @VisibleForTesting
   public WrittenBookContent apply(WrittenBookContent $$0) {
      List<Filterable<Component>> $$1 = this.pageOperation.apply($$0.pages(), this.pages);
      return $$0.withReplacedPages($$1);
   }

   @Override
   public LootItemFunctionType<SetWrittenBookPagesFunction> getType() {
      return LootItemFunctions.SET_WRITTEN_BOOK_PAGES;
   }
}
