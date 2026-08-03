package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;

public class ItemListReport implements net.minecraft.data.DataProvider {
   private final net.minecraft.data.PackOutput output;
   private final CompletableFuture<Provider> registries;

   public ItemListReport(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
      this.output = $$0;
      this.registries = $$1;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      Path $$1 = this.output.getOutputFolder(net.minecraft.data.PackOutput.Target.REPORTS).resolve("items.json");
      return this.registries
         .thenCompose(
            $$2 -> {
               JsonObject $$3 = new JsonObject();
               RegistryOps<JsonElement> $$4 = $$2.createSerializationContext(JsonOps.INSTANCE);
               $$2.lookupOrThrow(Registries.ITEM)
                  .listElements()
                  .forEach(
                     $$2x -> {
                        JsonObject $$3x = new JsonObject();
                        $$3x.add(
                           "components",
                           (JsonElement)DataComponentMap.CODEC
                              .encodeStart($$4, ((Item)$$2x.value()).components())
                              .getOrThrow($$0xxx -> new IllegalStateException("Failed to encode components: " + $$0xxx))
                        );
                        $$3.add($$2x.getRegisteredName(), $$3x);
                     }
                  );
               return net.minecraft.data.DataProvider.saveStable($$0, $$3, $$1);
            }
         );
   }

   @Override
   public final String getName() {
      return "Item List";
   }
}
