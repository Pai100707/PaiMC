package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractUUIDFix extends DataFix {
   protected TypeReference typeReference;

   public AbstractUUIDFix(Schema $$0, TypeReference $$1) {
      super($$0, false);
      this.typeReference = $$1;
   }

   protected Typed<?> updateNamedChoice(Typed<?> $$0, String $$1, Function<Dynamic<?>, Dynamic<?>> $$2) {
      Type<?> $$3 = this.getInputSchema().getChoiceType(this.typeReference, $$1);
      Type<?> $$4 = this.getOutputSchema().getChoiceType(this.typeReference, $$1);
      return $$0.updateTyped(DSL.namedChoice($$1, $$3), $$4, $$1x -> $$1x.update(DSL.remainderFinder(), $$2));
   }

   protected static Optional<Dynamic<?>> replaceUUIDString(Dynamic<?> $$0, String $$1, String $$2) {
      return createUUIDFromString($$0, $$1).map($$3 -> $$0.remove($$1).set($$2, $$3));
   }

   protected static Optional<Dynamic<?>> replaceUUIDMLTag(Dynamic<?> $$0, String $$1, String $$2) {
      return $$0.get($$1).result().flatMap(AbstractUUIDFix::createUUIDFromML).map($$3 -> $$0.remove($$1).set($$2, $$3));
   }

   protected static Optional<Dynamic<?>> replaceUUIDLeastMost(Dynamic<?> $$0, String $$1, String $$2) {
      String $$3 = $$1 + "Most";
      String $$4 = $$1 + "Least";
      return createUUIDFromLongs($$0, $$3, $$4).map($$4x -> $$0.remove($$3).remove($$4).set($$2, $$4x));
   }

   protected static Optional<Dynamic<?>> createUUIDFromString(Dynamic<?> $$0, String $$1) {
      return $$0.get($$1).result().flatMap($$1x -> {
         String $$2 = $$1x.asString(null);
         if ($$2 != null) {
            try {
               UUID $$3 = UUID.fromString($$2);
               return createUUIDTag($$0, $$3.getMostSignificantBits(), $$3.getLeastSignificantBits());
            } catch (IllegalArgumentException var4) {
            }
         }

         return Optional.empty();
      });
   }

   protected static Optional<Dynamic<?>> createUUIDFromML(Dynamic<?> $$0) {
      return createUUIDFromLongs($$0, "M", "L");
   }

   protected static Optional<Dynamic<?>> createUUIDFromLongs(Dynamic<?> $$0, String $$1, String $$2) {
      long $$3 = $$0.get($$1).asLong(0L);
      long $$4 = $$0.get($$2).asLong(0L);
      return $$3 != 0L && $$4 != 0L ? createUUIDTag($$0, $$3, $$4) : Optional.empty();
   }

   protected static Optional<Dynamic<?>> createUUIDTag(Dynamic<?> $$0, long $$1, long $$2) {
      return Optional.of($$0.createIntList(Arrays.stream(new int[]{(int)($$1 >> 32), (int)$$1, (int)($$2 >> 32), (int)$$2})));
   }
}
