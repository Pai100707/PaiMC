package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class NbtProviders {
   private static final Codec<NbtProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE
      .byNameCodec()
      .dispatch(NbtProvider::getType, LootNbtProviderType::codec);
   public static final Codec<NbtProvider> CODEC = Codec.lazyInitialized(
      () -> Codec.either(ContextNbtProvider.INLINE_CODEC, TYPED_CODEC)
         .xmap(Either::unwrap, $$0 -> $$0 instanceof ContextNbtProvider $$1 ? Either.left($$1) : Either.right($$0))
   );
   public static final LootNbtProviderType STORAGE = register("storage", StorageNbtProvider.CODEC);
   public static final LootNbtProviderType CONTEXT = register("context", ContextNbtProvider.MAP_CODEC);

   private static LootNbtProviderType register(String $$0, MapCodec<? extends NbtProvider> $$1) {
      return (LootNbtProviderType)Registry.register(
         BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, Identifier.withDefaultNamespace($$0), new LootNbtProviderType($$1)
      );
   }
}
