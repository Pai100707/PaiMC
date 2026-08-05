/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;

public class GameRulesService {
    public static List<GameRuleUpdate<?>> get(MinecraftApi $$0) {
        ArrayList $$1 = new ArrayList();
        $$0.gameRuleService().getAvailableGameRules().forEach($$2 -> GameRulesService.addGameRule($$0, $$2, $$1));
        return $$1;
    }

    private static <T> void addGameRule(MinecraftApi $$0, GameRule<T> $$1, List<GameRuleUpdate<?>> $$2) {
        T $$3 = $$0.gameRuleService().getRuleValue($$1);
        $$2.add(GameRulesService.getTypedRule($$0, $$1, Objects.requireNonNull($$3)));
    }

    public static <T> GameRuleUpdate<T> getTypedRule(MinecraftApi $$0, GameRule<T> $$1, T $$2) {
        return $$0.gameRuleService().getTypedRule($$1, $$2);
    }

    public static <T> GameRuleUpdate<T> update(MinecraftApi $$0, GameRuleUpdate<T> $$1, ClientInfo $$2) {
        return $$0.gameRuleService().updateGameRule($$1, $$2);
    }

    public record GameRuleUpdate<T>(GameRule<T> gameRule, T value) {
        public static final Codec<GameRuleUpdate<?>> TYPED_CODEC = BuiltInRegistries.GAME_RULE.byNameCodec().dispatch("key", GameRuleUpdate::gameRule, GameRuleUpdate::getValueAndTypeCodec);
        public static final Codec<GameRuleUpdate<?>> CODEC = BuiltInRegistries.GAME_RULE.byNameCodec().dispatch("key", GameRuleUpdate::gameRule, GameRuleUpdate::getValueCodec);

        private static <T> MapCodec<? extends GameRuleUpdate<T>> getValueCodec(GameRule<T> $$0) {
            return $$0.valueCodec().fieldOf("value").xmap($$1 -> new GameRuleUpdate<Object>($$0, $$1), GameRuleUpdate::value);
        }

        private static <T> MapCodec<? extends GameRuleUpdate<T>> getValueAndTypeCodec(GameRule<T> $$0) {
            return RecordCodecBuilder.mapCodec($$12 -> $$12.group((App)StringRepresentable.fromEnum(GameRuleType::values).fieldOf("type").forGetter($$0 -> $$0.gameRule.gameRuleType()), (App)$$0.valueCodec().fieldOf("value").forGetter(GameRuleUpdate::value)).apply((Applicative)$$12, ($$1, $$2) -> GameRuleUpdate.getUntypedRule($$0, $$1, $$2)));
        }

        private static <T> GameRuleUpdate<T> getUntypedRule(GameRule<T> $$0, GameRuleType $$1, T $$2) {
            if ($$0.gameRuleType() != $$1) {
                throw new InvalidParameterJsonRpcException("Stated type \"" + String.valueOf($$1) + "\" mismatches with actual type \"" + String.valueOf($$0.gameRuleType()) + "\" of gamerule \"" + $$0.id() + "\"");
            }
            return new GameRuleUpdate<T>($$0, $$2);
        }
    }
}

