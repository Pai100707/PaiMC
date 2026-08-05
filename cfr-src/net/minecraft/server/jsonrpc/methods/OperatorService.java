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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.util.Util;

public class OperatorService {
    public static List<OperatorDto> get(MinecraftApi $$02) {
        return $$02.operatorListService().getEntries().stream().filter($$0 -> $$0.getUser() != null).map(OperatorDto::from).toList();
    }

    public static List<OperatorDto> clear(MinecraftApi $$0, ClientInfo $$1) {
        $$0.operatorListService().clear($$1);
        return OperatorService.get($$0);
    }

    public static List<OperatorDto> remove(MinecraftApi $$0, List<PlayerDto> $$12, ClientInfo $$22) {
        List<CompletableFuture> $$3 = $$12.stream().map($$1 -> $$0.playerListService().getUser($$1.id(), $$1.name())).toList();
        for (Optional $$4 : Util.sequence($$3).join()) {
            $$4.ifPresent($$2 -> $$0.operatorListService().deop((NameAndId)$$2, $$22));
        }
        return OperatorService.get($$0);
    }

    public static List<OperatorDto> add(MinecraftApi $$0, List<OperatorDto> $$12, ClientInfo $$22) {
        List<CompletableFuture> $$3 = $$12.stream().map($$1 -> $$0.playerListService().getUser($$1.player().id(), $$1.player().name()).thenApply($$12 -> $$12.map($$1 -> new Op((NameAndId)$$1, $$1.permissionLevel(), $$1.bypassesPlayerLimit())))).toList();
        for (Optional $$4 : Util.sequence($$3).join()) {
            $$4.ifPresent($$2 -> $$0.operatorListService().op($$2.user(), $$2.permissionLevel(), $$2.bypassesPlayerLimit(), $$22));
        }
        return OperatorService.get($$0);
    }

    public static List<OperatorDto> set(MinecraftApi $$02, List<OperatorDto> $$12, ClientInfo $$22) {
        List<CompletableFuture> $$3 = $$12.stream().map($$1 -> $$02.playerListService().getUser($$1.player().id(), $$1.player().name()).thenApply($$12 -> $$12.map($$1 -> new Op((NameAndId)$$1, $$1.permissionLevel(), $$1.bypassesPlayerLimit())))).toList();
        Set $$4 = Util.sequence($$3).join().stream().flatMap(Optional::stream).collect(Collectors.toSet());
        Set $$5 = $$02.operatorListService().getEntries().stream().filter($$0 -> $$0.getUser() != null).map($$0 -> new Op((NameAndId)$$0.getUser(), Optional.of($$0.permissions().level()), Optional.of($$0.getBypassesPlayerLimit()))).collect(Collectors.toSet());
        $$5.stream().filter($$1 -> !$$4.contains($$1)).forEach($$2 -> $$02.operatorListService().deop($$2.user(), $$22));
        $$4.stream().filter($$1 -> !$$5.contains($$1)).forEach($$2 -> $$02.operatorListService().op($$2.user(), $$2.permissionLevel(), $$2.bypassesPlayerLimit(), $$22));
        return OperatorService.get($$02);
    }

    record Op(NameAndId user, Optional<PermissionLevel> permissionLevel, Optional<Boolean> bypassesPlayerLimit) {
    }

    public record OperatorDto(PlayerDto player, Optional<PermissionLevel> permissionLevel, Optional<Boolean> bypassesPlayerLimit) {
        public static final MapCodec<OperatorDto> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)PlayerDto.CODEC.codec().fieldOf("player").forGetter(OperatorDto::player), (App)PermissionLevel.INT_CODEC.optionalFieldOf("permissionLevel").forGetter(OperatorDto::permissionLevel), (App)Codec.BOOL.optionalFieldOf("bypassesPlayerLimit").forGetter(OperatorDto::bypassesPlayerLimit)).apply((Applicative)$$0, OperatorDto::new));

        public static OperatorDto from(ServerOpListEntry $$0) {
            return new OperatorDto(PlayerDto.from(Objects.requireNonNull((NameAndId)$$0.getUser())), Optional.of($$0.permissions().level()), Optional.of($$0.getBypassesPlayerLimit()));
        }
    }
}

