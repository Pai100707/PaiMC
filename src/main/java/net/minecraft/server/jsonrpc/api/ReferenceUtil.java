package net.minecraft.server.jsonrpc.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.net.URI;
import java.net.URISyntaxException;

public class ReferenceUtil {
   public static final Codec<URI> REFERENCE_CODEC = Codec.STRING.comapFlatMap($$0 -> {
      try {
         return DataResult.success(new URI($$0));
      } catch (URISyntaxException var2) {
         return DataResult.error(var2::getMessage);
      }
   }, URI::toString);

   public static URI createLocalReference(String $$0) {
      return URI.create("#/components/schemas/" + $$0);
   }
}
