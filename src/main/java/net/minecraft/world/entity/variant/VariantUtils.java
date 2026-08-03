package net.minecraft.world.entity.variant;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class VariantUtils {
   public static final String TAG_VARIANT = "variant";

   public static <T> Holder<T> getDefaultOrAny(RegistryAccess $$0, ResourceKey<T> $$1) {
      Registry<T> $$2 = $$0.lookupOrThrow($$1.registryKey());
      return (Holder<T>)$$2.get($$1).or($$2::getAny).orElseThrow();
   }

   public static <T> Holder<T> getAny(RegistryAccess $$0, ResourceKey<? extends Registry<T>> $$1) {
      return (Holder<T>)$$0.lookupOrThrow($$1).getAny().orElseThrow();
   }

   public static <T> void writeVariant(ValueOutput $$0, Holder<T> $$1) {
      $$1.unwrapKey().ifPresent($$1x -> $$0.store("variant", Identifier.CODEC, $$1x.identifier()));
   }

   public static <T> Optional<Holder<T>> readVariant(ValueInput $$0, ResourceKey<? extends Registry<T>> $$1) {
      return $$0.read("variant", Identifier.CODEC).map($$1x -> ResourceKey.create($$1, $$1x)).flatMap($$0.lookup()::get);
   }

   public static <T extends PriorityProvider<SpawnContext, ?>> Optional<Reference<T>> selectVariantToSpawn(SpawnContext $$0, ResourceKey<Registry<T>> $$1) {
      ServerLevelAccessor $$2 = $$0.level();
      Stream<Reference<T>> $$3 = $$2.registryAccess().lookupOrThrow($$1).listElements();
      return PriorityProvider.pick($$3, Holder::value, $$2.getRandom(), $$0);
   }
}
