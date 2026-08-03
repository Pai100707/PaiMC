package net.minecraft.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.DataComponentPatch.SplitResult;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HashedPatchMap(Map<DataComponentType<?>, Integer> addedComponents, Set<DataComponentType<?>> removedComponents) {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, net.minecraft.network.HashedPatchMap> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE), ByteBufCodecs.INT, 256),
      net.minecraft.network.HashedPatchMap::addedComponents,
      ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE), 256),
      net.minecraft.network.HashedPatchMap::removedComponents,
      net.minecraft.network.HashedPatchMap::new
   );

   public static net.minecraft.network.HashedPatchMap create(DataComponentPatch $$0, net.minecraft.network.HashedPatchMap.HashGenerator $$1) {
      SplitResult $$2 = $$0.split();
      Map<DataComponentType<?>, Integer> $$3 = new IdentityHashMap<>($$2.added().size());
      $$2.added().forEach($$2x -> $$3.put($$2x.type(), $$1.apply($$2x)));
      return new net.minecraft.network.HashedPatchMap($$3, $$2.removed());
   }

   public boolean matches(DataComponentPatch $$0, net.minecraft.network.HashedPatchMap.HashGenerator $$1) {
      SplitResult $$2 = $$0.split();
      if (!$$2.removed().equals(this.removedComponents)) {
         return false;
      } else if (this.addedComponents.size() != $$2.added().size()) {
         return false;
      } else {
         for (TypedDataComponent<?> $$3 : $$2.added()) {
            Integer $$4 = this.addedComponents.get($$3.type());
            if ($$4 == null) {
               return false;
            }

            Integer $$5 = $$1.apply($$3);
            if (!$$5.equals($$4)) {
               return false;
            }
         }

         return true;
      }
   }

   @FunctionalInterface
   public interface HashGenerator extends Function<TypedDataComponent<?>, Integer> {
   }
}
