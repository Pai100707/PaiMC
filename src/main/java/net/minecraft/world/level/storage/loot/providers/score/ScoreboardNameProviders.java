package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ScoreboardNameProviders {
   private static final Codec<ScoreboardNameProvider> TYPED_CODEC = BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE
      .byNameCodec()
      .dispatch(ScoreboardNameProvider::getType, LootScoreProviderType::codec);
   public static final Codec<ScoreboardNameProvider> CODEC = Codec.lazyInitialized(
      () -> Codec.either(ContextScoreboardNameProvider.INLINE_CODEC, TYPED_CODEC)
         .xmap(Either::unwrap, $$0 -> $$0 instanceof ContextScoreboardNameProvider $$1 ? Either.left($$1) : Either.right($$0))
   );
   public static final LootScoreProviderType FIXED = register("fixed", FixedScoreboardNameProvider.CODEC);
   public static final LootScoreProviderType CONTEXT = register("context", ContextScoreboardNameProvider.CODEC);

   private static LootScoreProviderType register(String $$0, MapCodec<? extends ScoreboardNameProvider> $$1) {
      return (LootScoreProviderType)Registry.register(
         BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, Identifier.withDefaultNamespace($$0), new LootScoreProviderType($$1)
      );
   }
}
