/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.methods;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.util.Util;

public class AllowlistService {
    public static List<PlayerDto> get(MinecraftApi $$02) {
        return $$02.allowListService().getEntries().stream().filter($$0 -> $$0.getUser() != null).map($$0 -> PlayerDto.from((NameAndId)$$0.getUser())).toList();
    }

    public static List<PlayerDto> add(MinecraftApi $$0, List<PlayerDto> $$12, ClientInfo $$22) {
        List<CompletableFuture> $$3 = $$12.stream().map($$1 -> $$0.playerListService().getUser($$1.id(), $$1.name())).toList();
        for (Optional $$4 : Util.sequence($$3).join()) {
            $$4.ifPresent($$2 -> $$0.allowListService().add(new UserWhiteListEntry((NameAndId)$$2), $$22));
        }
        return AllowlistService.get($$0);
    }

    public static List<PlayerDto> clear(MinecraftApi $$0, ClientInfo $$1) {
        $$0.allowListService().clear($$1);
        return AllowlistService.get($$0);
    }

    public static List<PlayerDto> remove(MinecraftApi $$0, List<PlayerDto> $$12, ClientInfo $$22) {
        List<CompletableFuture> $$3 = $$12.stream().map($$1 -> $$0.playerListService().getUser($$1.id(), $$1.name())).toList();
        for (Optional $$4 : Util.sequence($$3).join()) {
            $$4.ifPresent($$2 -> $$0.allowListService().remove((NameAndId)$$2, $$22));
        }
        $$0.allowListService().kickUnlistedPlayers($$22);
        return AllowlistService.get($$0);
    }

    public static List<PlayerDto> set(MinecraftApi $$0, List<PlayerDto> $$12, ClientInfo $$22) {
        List<CompletableFuture> $$3 = $$12.stream().map($$1 -> $$0.playerListService().getUser($$1.id(), $$1.name())).toList();
        Set $$4 = Util.sequence($$3).join().stream().flatMap(Optional::stream).collect(Collectors.toSet());
        Set $$5 = $$0.allowListService().getEntries().stream().map(StoredUserEntry::getUser).collect(Collectors.toSet());
        $$5.stream().filter($$1 -> !$$4.contains($$1)).forEach($$2 -> $$0.allowListService().remove((NameAndId)$$2, $$22));
        $$4.stream().filter($$1 -> !$$5.contains($$1)).forEach($$2 -> $$0.allowListService().add(new UserWhiteListEntry((NameAndId)$$2), $$22));
        $$0.allowListService().kickUnlistedPlayers($$22);
        return AllowlistService.get($$0);
    }
}

