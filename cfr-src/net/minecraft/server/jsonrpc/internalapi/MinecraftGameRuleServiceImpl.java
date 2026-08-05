/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.internalapi;

import java.util.stream.Stream;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftGameRuleService;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.GameRulesService;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

public class MinecraftGameRuleServiceImpl
implements MinecraftGameRuleService {
    private final DedicatedServer server;
    private final GameRules gameRules;
    private final JsonRpcLogger jsonrpcLogger;

    public MinecraftGameRuleServiceImpl(DedicatedServer $$0, JsonRpcLogger $$1) {
        this.server = $$0;
        this.gameRules = $$0.getWorldData().getGameRules();
        this.jsonrpcLogger = $$1;
    }

    @Override
    public <T> GameRulesService.GameRuleUpdate<T> updateGameRule(GameRulesService.GameRuleUpdate<T> $$0, ClientInfo $$1) {
        GameRule<T> $$2 = $$0.gameRule();
        T $$3 = this.gameRules.get($$2);
        T $$4 = $$0.value();
        this.gameRules.set($$2, $$4, this.server);
        this.jsonrpcLogger.log($$1, "Game rule '{}' updated from '{}' to '{}'", $$2.id(), $$2.serialize($$3), $$2.serialize($$4));
        return $$0;
    }

    @Override
    public <T> GameRulesService.GameRuleUpdate<T> getTypedRule(GameRule<T> $$0, T $$1) {
        return new GameRulesService.GameRuleUpdate<T>($$0, $$1);
    }

    @Override
    public Stream<GameRule<?>> getAvailableGameRules() {
        return this.gameRules.availableRules();
    }

    @Override
    public <T> T getRuleValue(GameRule<T> $$0) {
        return this.gameRules.get($$0);
    }
}

