package net.minecraft.server.jsonrpc.methods;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;

public class GameRulesService {
   public static List<GameRulesService.GameRuleUpdate<?>> get(MinecraftApi $$0) {
      List<GameRulesService.GameRuleUpdate<?>> $$1 = new ArrayList<>();
      $$0.gameRuleService().getAvailableGameRules().forEach($$2 -> addGameRule($$0, $$2, $$1));
      return $$1;
   }

   private static <T> void addGameRule(MinecraftApi $$0, GameRule<T> $$1, List<GameRulesService.GameRuleUpdate<?>> $$2) {
      T $$3 = $$0.gameRuleService().getRuleValue($$1);
      $$2.add(getTypedRule($$0, $$1, Objects.requireNonNull($$3)));
   }

   public static <T> GameRulesService.GameRuleUpdate<T> getTypedRule(MinecraftApi $$0, GameRule<T> $$1, T $$2) {
      return $$0.gameRuleService().getTypedRule($$1, $$2);
   }

   public static <T> GameRulesService.GameRuleUpdate<T> update(MinecraftApi $$0, GameRulesService.GameRuleUpdate<T> $$1, ClientInfo $$2) {
      return $$0.gameRuleService().updateGameRule($$1, $$2);
   }

   public record GameRuleUpdate<T>(GameRule<T> gameRule, T value) {
      public static final Codec<GameRulesService.GameRuleUpdate<?>> TYPED_CODEC = BuiltInRegistries.GAME_RULE
         .byNameCodec()
         .dispatch("key", GameRulesService.GameRuleUpdate::gameRule, GameRulesService.GameRuleUpdate::getValueAndTypeCodec);
      public static final Codec<GameRulesService.GameRuleUpdate<?>> CODEC = BuiltInRegistries.GAME_RULE
         .byNameCodec()
         .dispatch("key", GameRulesService.GameRuleUpdate::gameRule, GameRulesService.GameRuleUpdate::getValueCodec);

      private static <T> MapCodec<? extends GameRulesService.GameRuleUpdate<T>> getValueCodec(GameRule<T> $$0) {
         return $$0.valueCodec().fieldOf("value").xmap($$1 -> new GameRulesService.GameRuleUpdate<>($$0, $$1), GameRulesService.GameRuleUpdate::value);
      }

      private static <T> MapCodec<? extends GameRulesService.GameRuleUpdate<T>> getValueAndTypeCodec(GameRule<T> $$0) {
         return RecordCodecBuilder.mapCodec(
            $$1 -> $$1.group(
                  StringRepresentable.fromEnum(GameRuleType::values).fieldOf("type").forGetter($$0xx -> $$0xx.gameRule.gameRuleType()),
                  $$0.valueCodec().fieldOf("value").forGetter(GameRulesService.GameRuleUpdate::value)
               )
               .apply($$1, ($$1x, $$2) -> getUntypedRule($$0, $$1x, $$2))
         );
      }

      private static <T> GameRulesService.GameRuleUpdate<T> getUntypedRule(GameRule<T> $$0, GameRuleType $$1, T $$2) {
         if ($$0.gameRuleType() != $$1) {
            throw new InvalidParameterJsonRpcException(
               "Stated type \"" + $$1 + "\" mismatches with actual type \"" + $$0.gameRuleType() + "\" of gamerule \"" + $$0.id() + "\""
            );
         } else {
            return new GameRulesService.GameRuleUpdate<>($$0, $$2);
         }
      }
   }
}
