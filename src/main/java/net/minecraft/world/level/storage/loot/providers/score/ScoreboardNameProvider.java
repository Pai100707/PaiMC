package net.minecraft.world.level.storage.loot.providers.score;

import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.scores.ScoreHolder;

public interface ScoreboardNameProvider {
   
   ScoreHolder getScoreHolder(LootContext var1);

   LootScoreProviderType getType();

   Set<ContextKey<?>> getReferencedContextParams();
}
