package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class DropInvalidSignDataFix extends DataFix {
   private final String entityName;

   public DropInvalidSignDataFix(Schema $$0, String $$1) {
      super($$0, false);
      this.entityName = $$1;
   }

   private <T> Dynamic<T> fix(Dynamic<T> $$0) {
      $$0 = $$0.update("front_text", DropInvalidSignDataFix::fixText);
      $$0 = $$0.update("back_text", DropInvalidSignDataFix::fixText);

      for (String $$1 : BlockEntitySignDoubleSidedEditableTextFix.FIELDS_TO_DROP) {
         $$0 = $$0.remove($$1);
      }

      return $$0;
   }

   private static <T> Dynamic<T> fixText(Dynamic<T> $$0) {
      Optional<Stream<Dynamic<T>>> $$1 = $$0.get("filtered_messages").asStreamOpt().result();
      if ($$1.isEmpty()) {
         return $$0;
      } else {
         Dynamic<T> $$2 = LegacyComponentDataFixUtils.createEmptyComponent($$0.getOps());
         List<Dynamic<T>> $$3 = $$0.get("messages").asStreamOpt().result().orElse(Stream.of()).toList();
         List<Dynamic<T>> $$4 = Streams.mapWithIndex($$1.get(), ($$2x, $$3x) -> {
            Dynamic<T> $$4x = $$3x < $$3.size() ? $$3.get((int)$$3x) : $$2;
            return $$2x.equals($$2) ? $$4x : $$2x;
         }).toList();
         return $$4.equals($$3) ? $$0.remove("filtered_messages") : $$0.set("filtered_messages", $$0.createList($$4.stream()));
      }
   }

   public TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.BLOCK_ENTITY);
      Type<?> $$1 = this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, this.entityName);
      OpticFinder<?> $$2 = DSL.namedChoice(this.entityName, $$1);
      return this.fixTypeEverywhereTyped(
         "DropInvalidSignDataFix for " + this.entityName,
         $$0,
         $$2x -> $$2x.updateTyped(
            $$2,
            $$1,
            $$1xx -> {
               boolean $$2xx = ((Dynamic)$$1xx.get(DSL.remainderFinder())).get("_filtered_correct").asBoolean(false);
               return $$2xx
                  ? $$1xx.update(DSL.remainderFinder(), $$0xxx -> $$0xxx.remove("_filtered_correct"))
                  : net.minecraft.util.Util.writeAndReadTypedOrThrow($$1xx, $$1, this::fix);
            }
         )
      );
   }
}
