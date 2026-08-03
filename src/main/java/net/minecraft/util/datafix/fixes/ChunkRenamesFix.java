package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;

public class ChunkRenamesFix extends DataFix {
   public ChunkRenamesFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> $$1 = $$0.findField("Level");
      OpticFinder<?> $$2 = $$1.type().findField("Structures");
      Type<?> $$3 = this.getOutputSchema().getType(References.CHUNK);
      Type<?> $$4 = $$3.findFieldType("structures");
      return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", $$0, $$3, $$3x -> {
         Typed<?> $$4x = $$3x.getTyped($$1);
         Typed<?> $$5 = appendChunkName($$4x);
         $$5 = $$5.set(DSL.remainderFinder(), mergeRemainders($$3x, (Dynamic)$$4x.get(DSL.remainderFinder())));
         $$5 = renameField($$5, "TileEntities", "block_entities");
         $$5 = renameField($$5, "TileTicks", "block_ticks");
         $$5 = renameField($$5, "Entities", "entities");
         $$5 = renameField($$5, "Sections", "sections");
         $$5 = $$5.updateTyped($$2, $$4, $$0xx -> renameField($$0xx, "Starts", "starts"));
         $$5 = renameField($$5, "Structures", "structures");
         return $$5.update(DSL.remainderFinder(), $$0xx -> $$0xx.remove("Level"));
      });
   }

   private static Typed<?> renameField(Typed<?> $$0, String $$1, String $$2) {
      return renameFieldHelper($$0, $$1, $$2, $$0.getType().findFieldType($$1)).update(DSL.remainderFinder(), $$1x -> $$1x.remove($$1));
   }

   private static <A> Typed<?> renameFieldHelper(Typed<?> $$0, String $$1, String $$2, Type<A> $$3) {
      Type<Either<A, com.mojang.datafixers.util.Unit>> $$4 = DSL.optional(DSL.field($$1, $$3));
      Type<Either<A, com.mojang.datafixers.util.Unit>> $$5 = DSL.optional(DSL.field($$2, $$3));
      return $$0.update($$4.finder(), $$5, Function.identity());
   }

   private static <A> Typed<Pair<String, A>> appendChunkName(Typed<A> $$0) {
      return new Typed(DSL.named("chunk", $$0.getType()), $$0.getOps(), Pair.of("chunk", $$0.getValue()));
   }

   private static <T> Dynamic<T> mergeRemainders(Typed<?> $$0, Dynamic<T> $$1) {
      DynamicOps<T> $$2 = $$1.getOps();
      Dynamic<T> $$3 = ((Dynamic)$$0.get(DSL.remainderFinder())).convert($$2);
      DataResult<T> $$4 = $$2.getMap($$1.getValue()).flatMap($$2x -> $$2.mergeToMap($$3.getValue(), $$2x));
      return $$4.result().map($$1x -> new Dynamic($$2, $$1x)).orElse($$1);
   }
}
