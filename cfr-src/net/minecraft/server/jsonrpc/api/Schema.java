/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.jsonrpc.api;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.api.ReferenceUtil;
import net.minecraft.server.jsonrpc.api.SchemaComponent;
import net.minecraft.server.jsonrpc.methods.BanlistService;
import net.minecraft.server.jsonrpc.methods.DiscoveryService;
import net.minecraft.server.jsonrpc.methods.GameRulesService;
import net.minecraft.server.jsonrpc.methods.IpBanlistService;
import net.minecraft.server.jsonrpc.methods.Message;
import net.minecraft.server.jsonrpc.methods.OperatorService;
import net.minecraft.server.jsonrpc.methods.PlayerService;
import net.minecraft.server.jsonrpc.methods.ServerStateService;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRuleType;

public record Schema<T>(Optional<URI> reference, List<String> type, Optional<Schema<?>> items, Map<String, Schema<?>> properties, List<String> enumValues, Codec<T> codec) {
    public static final Codec<? extends Schema<?>> CODEC = Codec.recursive((String)"Schema", $$0 -> RecordCodecBuilder.create($$12 -> $$12.group((App)ReferenceUtil.REFERENCE_CODEC.optionalFieldOf("$ref").forGetter(Schema::reference), (App)ExtraCodecs.compactListCodec(Codec.STRING).optionalFieldOf("type", List.of()).forGetter(Schema::type), (App)$$0.optionalFieldOf("items").forGetter(Schema::items), (App)Codec.unboundedMap((Codec)Codec.STRING, (Codec)$$0).optionalFieldOf("properties", Map.of()).forGetter(Schema::properties), (App)Codec.STRING.listOf().optionalFieldOf("enum", List.of()).forGetter(Schema::enumValues)).apply((Applicative)$$12, ($$0, $$1, $$2, $$3, $$4) -> null))).validate($$0 -> {
        if ($$0 == null) {
            return DataResult.error(() -> "Should not deserialize schema");
        }
        return DataResult.success((Object)$$0);
    });
    private static final List<SchemaComponent<?>> SCHEMA_REGISTRY = new ArrayList();
    public static final Schema<Boolean> BOOL_SCHEMA = Schema.ofType("boolean", Codec.BOOL);
    public static final Schema<Integer> INT_SCHEMA = Schema.ofType("integer", Codec.INT);
    public static final Schema<Either<Boolean, Integer>> BOOL_OR_INT_SCHEMA = Schema.ofTypes(List.of("boolean", "integer"), Codec.either((Codec)Codec.BOOL, (Codec)Codec.INT));
    public static final Schema<Float> NUMBER_SCHEMA = Schema.ofType("number", Codec.FLOAT);
    public static final Schema<String> STRING_SCHEMA = Schema.ofType("string", Codec.STRING);
    public static final Schema<UUID> UUID_SCHEMA = Schema.ofType("string", UUIDUtil.CODEC);
    public static final Schema<DiscoveryService.DiscoverResponse> DISCOVERY_SCHEMA = Schema.ofType("string", DiscoveryService.DiscoverResponse.CODEC.codec());
    public static final SchemaComponent<Difficulty> DIFFICULTY_SCHEMA = Schema.registerSchema("difficulty", Schema.ofEnum(Difficulty::values, Difficulty.CODEC));
    public static final SchemaComponent<GameType> GAME_TYPE_SCHEMA = Schema.registerSchema("game_type", Schema.ofEnum(GameType::values, GameType.CODEC));
    public static final Schema<PermissionLevel> PERMISSION_LEVEL_SCHEMA = Schema.ofType("integer", PermissionLevel.INT_CODEC);
    public static final SchemaComponent<PlayerDto> PLAYER_SCHEMA = Schema.registerSchema("player", Schema.record(PlayerDto.CODEC.codec()).withField("id", UUID_SCHEMA).withField("name", STRING_SCHEMA));
    public static final SchemaComponent<DiscoveryService.DiscoverInfo> VERSION_SCHEMA = Schema.registerSchema("version", Schema.record(DiscoveryService.DiscoverInfo.CODEC.codec()).withField("name", STRING_SCHEMA).withField("protocol", INT_SCHEMA));
    public static final SchemaComponent<ServerStateService.ServerState> SERVER_STATE_SCHEMA = Schema.registerSchema("server_state", Schema.record(ServerStateService.ServerState.CODEC).withField("started", BOOL_SCHEMA).withField("players", PLAYER_SCHEMA.asRef().asArray()).withField("version", VERSION_SCHEMA.asRef()));
    public static final Schema<GameRuleType> RULE_TYPE_SCHEMA = Schema.ofEnum(GameRuleType::values);
    public static final SchemaComponent<GameRulesService.GameRuleUpdate<?>> TYPED_GAME_RULE_SCHEMA = Schema.registerSchema("typed_game_rule", Schema.record(GameRulesService.GameRuleUpdate.TYPED_CODEC).withField("key", STRING_SCHEMA).withField("value", BOOL_OR_INT_SCHEMA).withField("type", RULE_TYPE_SCHEMA));
    public static final SchemaComponent<GameRulesService.GameRuleUpdate<?>> UNTYPED_GAME_RULE_SCHEMA = Schema.registerSchema("untyped_game_rule", Schema.record(GameRulesService.GameRuleUpdate.CODEC).withField("key", STRING_SCHEMA).withField("value", BOOL_OR_INT_SCHEMA));
    public static final SchemaComponent<Message> MESSAGE_SCHEMA = Schema.registerSchema("message", Schema.record(Message.CODEC).withField("literal", STRING_SCHEMA).withField("translatable", STRING_SCHEMA).withField("translatableParams", STRING_SCHEMA.asArray()));
    public static final SchemaComponent<ServerStateService.SystemMessage> SYSTEM_MESSAGE_SCHEMA = Schema.registerSchema("system_message", Schema.record(ServerStateService.SystemMessage.CODEC).withField("message", MESSAGE_SCHEMA.asRef()).withField("overlay", BOOL_SCHEMA).withField("receivingPlayers", PLAYER_SCHEMA.asRef().asArray()));
    public static final SchemaComponent<PlayerService.KickDto> KICK_PLAYER_SCHEMA = Schema.registerSchema("kick_player", Schema.record(PlayerService.KickDto.CODEC.codec()).withField("message", MESSAGE_SCHEMA.asRef()).withField("player", PLAYER_SCHEMA.asRef()));
    public static final SchemaComponent<OperatorService.OperatorDto> OPERATOR_SCHEMA = Schema.registerSchema("operator", Schema.record(OperatorService.OperatorDto.CODEC.codec()).withField("player", PLAYER_SCHEMA.asRef()).withField("bypassesPlayerLimit", BOOL_SCHEMA).withField("permissionLevel", INT_SCHEMA));
    public static final SchemaComponent<IpBanlistService.IncomingIpBanDto> INCOMING_IP_BAN_SCHEMA = Schema.registerSchema("incoming_ip_ban", Schema.record(IpBanlistService.IncomingIpBanDto.CODEC.codec()).withField("player", PLAYER_SCHEMA.asRef()).withField("ip", STRING_SCHEMA).withField("reason", STRING_SCHEMA).withField("source", STRING_SCHEMA).withField("expires", STRING_SCHEMA));
    public static final SchemaComponent<IpBanlistService.IpBanDto> IP_BAN_SCHEMA = Schema.registerSchema("ip_ban", Schema.record(IpBanlistService.IpBanDto.CODEC.codec()).withField("ip", STRING_SCHEMA).withField("reason", STRING_SCHEMA).withField("source", STRING_SCHEMA).withField("expires", STRING_SCHEMA));
    public static final SchemaComponent<BanlistService.UserBanDto> PLAYER_BAN_SCHEMA = Schema.registerSchema("user_ban", Schema.record(BanlistService.UserBanDto.CODEC.codec()).withField("player", PLAYER_SCHEMA.asRef()).withField("reason", STRING_SCHEMA).withField("source", STRING_SCHEMA).withField("expires", STRING_SCHEMA));

    public static <T> Codec<Schema<T>> typedCodec() {
        return CODEC;
    }

    public Schema<T> info() {
        return new Schema<T>(this.reference, this.type, this.items.map(Schema::info), this.properties.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, $$0 -> ((Schema)$$0.getValue()).info())), this.enumValues, this.codec);
    }

    private static <T> SchemaComponent<T> registerSchema(String $$0, Schema<T> $$1) {
        SchemaComponent<T> $$2 = new SchemaComponent<T>($$0, ReferenceUtil.createLocalReference($$0), $$1);
        SCHEMA_REGISTRY.add($$2);
        return $$2;
    }

    public static List<SchemaComponent<?>> getSchemaRegistry() {
        return SCHEMA_REGISTRY;
    }

    public static <T> Schema<T> ofRef(URI $$0, Codec<T> $$1) {
        return new Schema<T>(Optional.of($$0), List.of(), Optional.empty(), Map.of(), List.of(), $$1);
    }

    public static <T> Schema<T> ofType(String $$0, Codec<T> $$1) {
        return Schema.ofTypes(List.of($$0), $$1);
    }

    public static <T> Schema<T> ofTypes(List<String> $$0, Codec<T> $$1) {
        return new Schema<T>(Optional.empty(), $$0, Optional.empty(), Map.of(), List.of(), $$1);
    }

    public static <E extends Enum<E>> Schema<E> ofEnum(Supplier<E[]> $$0) {
        return Schema.ofEnum($$0, StringRepresentable.fromEnum($$0));
    }

    public static <E extends Enum<E>> Schema<E> ofEnum(Supplier<E[]> $$02, Codec<E> $$1) {
        List<String> $$2 = Stream.of((Enum[])$$02.get()).map($$0 -> ((StringRepresentable)$$0).getSerializedName()).toList();
        return Schema.ofEnum($$2, $$1);
    }

    public static <T> Schema<T> ofEnum(List<String> $$0, Codec<T> $$1) {
        return new Schema<T>(Optional.empty(), List.of("string"), Optional.empty(), Map.of(), $$0, $$1);
    }

    public static <T> Schema<List<T>> arrayOf(Schema<?> $$0, Codec<T> $$1) {
        return new Schema<List<T>>(Optional.empty(), List.of("array"), Optional.of($$0), Map.of(), List.of(), $$1.listOf());
    }

    public static <T> Schema<T> record(Codec<T> $$0) {
        return new Schema<T>(Optional.empty(), List.of("object"), Optional.empty(), Map.of(), List.of(), $$0);
    }

    private static <T> Schema<T> record(Map<String, Schema<?>> $$0, Codec<T> $$1) {
        return new Schema<T>(Optional.empty(), List.of("object"), Optional.empty(), $$0, List.of(), $$1);
    }

    public Schema<T> withField(String $$0, Schema<?> $$1) {
        HashMap $$2 = new HashMap(this.properties);
        $$2.put($$0, $$1);
        return Schema.record($$2, this.codec);
    }

    public Schema<List<T>> asArray() {
        return Schema.arrayOf(this, this.codec);
    }
}

