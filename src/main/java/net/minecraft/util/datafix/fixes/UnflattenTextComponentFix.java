package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import org.slf4j.Logger;

public class UnflattenTextComponentFix extends DataFix {
   private static final Logger LOGGER = LogUtils.getLogger();

   public UnflattenTextComponentFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<Pair<String, String>> $$0 = this.getInputSchema().getType(References.TEXT_COMPONENT);
      Type<?> $$1 = this.getOutputSchema().getType(References.TEXT_COMPONENT);
      return this.createFixer($$0, $$1);
   }

   private <T> TypeRewriteRule createFixer(Type<Pair<String, String>> $$0, Type<T> $$1) {
      return this.fixTypeEverywhere(
         "UnflattenTextComponentFix",
         $$0,
         $$1,
         $$1x -> $$2 -> net.minecraft.util.Util.readTypedOrThrow($$1, unflattenJson($$1x, (String)$$2.getSecond()), true).getValue()
      );
   }

   private static <T> Dynamic<T> unflattenJson(DynamicOps<T> $$0, String $$1) {
      try {
         JsonElement $$2 = net.minecraft.util.LenientJsonParser.parse($$1);
         if (!$$2.isJsonNull()) {
            return new Dynamic($$0, JsonOps.INSTANCE.convertTo($$0, $$2));
         }
      } catch (Exception var3) {
         LOGGER.error("Failed to unflatten text component json: {}", $$1, var3);
      }

      return new Dynamic($$0, $$0.createString($$1));
   }
}
