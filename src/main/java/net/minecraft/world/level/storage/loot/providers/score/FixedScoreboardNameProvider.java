package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.scores.ScoreHolder;

public record FixedScoreboardNameProvider(String name) implements ScoreboardNameProvider {
   public static final MapCodec<FixedScoreboardNameProvider> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.STRING.fieldOf("name").forGetter(FixedScoreboardNameProvider::name)).apply($$0, FixedScoreboardNameProvider::new)
   );

   public static ScoreboardNameProvider forName(String $$0) {
      return new FixedScoreboardNameProvider($$0);
   }

   @Override
   public LootScoreProviderType getType() {
      return ScoreboardNameProviders.FIXED;
   }

   @Override
   public ScoreHolder getScoreHolder(LootContext $$0) {
      return ScoreHolder.forNameOnly(this.name);
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of();
   }
}
