package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import java.util.stream.Stream;

public class PoiTypeRenameFix extends AbstractPoiSectionFix {
   private final Function<String, String> renamer;

   public PoiTypeRenameFix(Schema $$0, String $$1, Function<String, String> $$2) {
      super($$0, $$1);
      this.renamer = $$2;
   }

   @Override
   protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> $$0) {
      return $$0.map(
         $$0x -> $$0x.update("type", $$0xx -> (Dynamic)DataFixUtils.orElse($$0xx.asString().map(this.renamer).map($$0xx::createString).result(), $$0xx))
      );
   }
}
