package net.minecraft.server.jsonrpc.api;

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
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
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

public record Schema<T>(
   Optional<URI> reference, List<String> type, Optional<Schema<?>> items, Map<String, Schema<?>> properties, List<String> enumValues, Codec<T> codec
) {
   public static final Codec<? extends Schema<?>> CODEC = Codec.recursive(
         "Schema",
         $$0 -> RecordCodecBuilder.create(
            $$1 -> $$1.group(
                  ReferenceUtil.REFERENCE_CODEC.optionalFieldOf("$ref").forGetter(Schema::reference),
                  ExtraCodecs.compactListCodec(Codec.STRING).optionalFieldOf("type", List.of()).forGetter(Schema::type),
                  $$0.optionalFieldOf("items").forGetter(Schema::items),
                  Codec.unboundedMap(Codec.STRING, $$0).optionalFieldOf("properties", Map.of()).forGetter(Schema::properties),
                  Codec.STRING.listOf().optionalFieldOf("enum", List.of()).forGetter(Schema::enumValues)
               )
               .apply($$1, ($$0xx, $$1x, $$2, $$3, $$4) -> null)
         )
      )
      .validate($$0 -> $$0 == null ? DataResult.error(() -> "Should not deserialize schema") : DataResult.success($$0));
   private static final List<SchemaComponent<?>> SCHEMA_REGISTRY = new ArrayList<>();
   public static final Schema<Boolean> BOOL_SCHEMA = ofType("boolean", Codec.BOOL);
   public static final Schema<Integer> INT_SCHEMA = ofType("integer", Codec.INT);
   public static final Schema<Either<Boolean, Integer>> BOOL_OR_INT_SCHEMA = ofTypes(List.of("boolean", "integer"), Codec.either(Codec.BOOL, Codec.INT));
   public static final Schema<Float> NUMBER_SCHEMA = ofType("number", Codec.FLOAT);
   public static final Schema<String> STRING_SCHEMA = ofType("string", Codec.STRING);
   public static final Schema<UUID> UUID_SCHEMA = ofType("string", UUIDUtil.CODEC);
   public static final Schema<DiscoveryService.DiscoverResponse> DISCOVERY_SCHEMA = ofType("string", DiscoveryService.DiscoverResponse.CODEC.codec());
   public static final SchemaComponent<Difficulty> DIFFICULTY_SCHEMA = registerSchema("difficulty", ofEnum(Difficulty::values, Difficulty.CODEC));
   public static final SchemaComponent<GameType> GAME_TYPE_SCHEMA = registerSchema("game_type", ofEnum(GameType::values, GameType.CODEC));
   public static final Schema<PermissionLevel> PERMISSION_LEVEL_SCHEMA = ofType("integer", PermissionLevel.INT_CODEC);
   public static final SchemaComponent<PlayerDto> PLAYER_SCHEMA = registerSchema(
      "player", record(PlayerDto.CODEC.codec()).withField("id", UUID_SCHEMA).withField("name", STRING_SCHEMA)
   );
   public static final SchemaComponent<DiscoveryService.DiscoverInfo> VERSION_SCHEMA = registerSchema(
      "version", record(DiscoveryService.DiscoverInfo.CODEC.codec()).withField("name", STRING_SCHEMA).withField("protocol", INT_SCHEMA)
   );
   public static final SchemaComponent<ServerStateService.ServerState> SERVER_STATE_SCHEMA = registerSchema(
      "server_state",
      record(ServerStateService.ServerState.CODEC)
         .withField("started", BOOL_SCHEMA)
         .withField("players", PLAYER_SCHEMA.asRef().asArray())
         .withField("version", VERSION_SCHEMA.asRef())
   );
   public static final Schema<GameRuleType> RULE_TYPE_SCHEMA = ofEnum(GameRuleType::values);
   public static final SchemaComponent<GameRulesService.GameRuleUpdate<?>> TYPED_GAME_RULE_SCHEMA = registerSchema(
      "typed_game_rule",
      record(GameRulesService.GameRuleUpdate.TYPED_CODEC)
         .withField("key", STRING_SCHEMA)
         .withField("value", BOOL_OR_INT_SCHEMA)
         .withField("type", RULE_TYPE_SCHEMA)
   );
   public static final SchemaComponent<GameRulesService.GameRuleUpdate<?>> UNTYPED_GAME_RULE_SCHEMA = registerSchema(
      "untyped_game_rule", record(GameRulesService.GameRuleUpdate.CODEC).withField("key", STRING_SCHEMA).withField("value", BOOL_OR_INT_SCHEMA)
   );
   public static final SchemaComponent<Message> MESSAGE_SCHEMA = registerSchema(
      "message",
      record(Message.CODEC)
         .withField("literal", STRING_SCHEMA)
         .withField("translatable", STRING_SCHEMA)
         .withField("translatableParams", STRING_SCHEMA.asArray())
   );
   public static final SchemaComponent<ServerStateService.SystemMessage> SYSTEM_MESSAGE_SCHEMA = registerSchema(
      "system_message",
      record(ServerStateService.SystemMessage.CODEC)
         .withField("message", MESSAGE_SCHEMA.asRef())
         .withField("overlay", BOOL_SCHEMA)
         .withField("receivingPlayers", PLAYER_SCHEMA.asRef().asArray())
   );
   public static final SchemaComponent<PlayerService.KickDto> KICK_PLAYER_SCHEMA = registerSchema(
      "kick_player", record(PlayerService.KickDto.CODEC.codec()).withField("message", MESSAGE_SCHEMA.asRef()).withField("player", PLAYER_SCHEMA.asRef())
   );
   public static final SchemaComponent<OperatorService.OperatorDto> OPERATOR_SCHEMA = registerSchema(
      "operator",
      record(OperatorService.OperatorDto.CODEC.codec())
         .withField("player", PLAYER_SCHEMA.asRef())
         .withField("bypassesPlayerLimit", BOOL_SCHEMA)
         .withField("permissionLevel", INT_SCHEMA)
   );
   public static final SchemaComponent<IpBanlistService.IncomingIpBanDto> INCOMING_IP_BAN_SCHEMA = registerSchema(
      "incoming_ip_ban",
      record(IpBanlistService.IncomingIpBanDto.CODEC.codec())
         .withField("player", PLAYER_SCHEMA.asRef())
         .withField("ip", STRING_SCHEMA)
         .withField("reason", STRING_SCHEMA)
         .withField("source", STRING_SCHEMA)
         .withField("expires", STRING_SCHEMA)
   );
   public static final SchemaComponent<IpBanlistService.IpBanDto> IP_BAN_SCHEMA = registerSchema(
      "ip_ban",
      record(IpBanlistService.IpBanDto.CODEC.codec())
         .withField("ip", STRING_SCHEMA)
         .withField("reason", STRING_SCHEMA)
         .withField("source", STRING_SCHEMA)
         .withField("expires", STRING_SCHEMA)
   );
   public static final SchemaComponent<BanlistService.UserBanDto> PLAYER_BAN_SCHEMA = registerSchema(
      "user_ban",
      record(BanlistService.UserBanDto.CODEC.codec())
         .withField("player", PLAYER_SCHEMA.asRef())
         .withField("reason", STRING_SCHEMA)
         .withField("source", STRING_SCHEMA)
         .withField("expires", STRING_SCHEMA)
   );

   public static <T> Codec<Schema<T>> typedCodec() {
      return (Codec<Schema<T>>)CODEC;
   }

   public Schema<T> info() {
      return new Schema<>(
         this.reference,
         this.type,
         this.items.map(Schema::info),
         this.properties.entrySet().stream().collect(Collectors.toMap(Entry::getKey, $$0 -> $$0.getValue().info())),
         this.enumValues,
         this.codec
      );
   }

   private static <T> SchemaComponent<T> registerSchema(String $$0, Schema<T> $$1) {
      SchemaComponent<T> $$2 = new SchemaComponent<>($$0, ReferenceUtil.createLocalReference($$0), $$1);
      SCHEMA_REGISTRY.add($$2);
      return $$2;
   }

   public static List<SchemaComponent<?>> getSchemaRegistry() {
      return SCHEMA_REGISTRY;
   }

   public static <T> Schema<T> ofRef(URI $$0, Codec<T> $$1) {
      return new Schema<>(Optional.of($$0), List.of(), Optional.empty(), Map.of(), List.of(), $$1);
   }

   public static <T> Schema<T> ofType(String $$0, Codec<T> $$1) {
      return ofTypes(List.of($$0), $$1);
   }

   public static <T> Schema<T> ofTypes(List<String> $$0, Codec<T> $$1) {
      return new Schema<>(Optional.empty(), $$0, Optional.empty(), Map.of(), List.of(), $$1);
   }

   public static <E extends Enum<E> & StringRepresentable> Schema<E> ofEnum(Supplier<E[]> $$0) {
      return ofEnum($$0, StringRepresentable.fromEnum($$0));
   }

   public static <E extends Enum<E> & StringRepresentable> Schema<E> ofEnum(Supplier<E[]> $$0, Codec<E> $$1) {
      List<String> $$2 = Stream.<Enum>of((Enum[])$$0.get()).map($$0x -> ((StringRepresentable)$$0x).getSerializedName()).toList();
      return ofEnum($$2, $$1);
   }

   public static <T> Schema<T> ofEnum(List<String> $$0, Codec<T> $$1) {
      return new Schema<>(Optional.empty(), List.of("string"), Optional.empty(), Map.of(), $$0, $$1);
   }

   public static <T> Schema<List<T>> arrayOf(Schema<?> $$0, Codec<T> $$1) {
      return new Schema<>(Optional.empty(), List.of("array"), Optional.of($$0), Map.of(), List.of(), $$1.listOf());
   }

   public static <T> Schema<T> record(Codec<T> $$0) {
      return new Schema<>(Optional.empty(), List.of("object"), Optional.empty(), Map.of(), List.of(), $$0);
   }

   private static <T> Schema<T> record(Map<String, Schema<?>> $$0, Codec<T> $$1) {
      return new Schema<>(Optional.empty(), List.of("object"), Optional.empty(), $$0, List.of(), $$1);
   }

   public Schema<T> withField(String $$0, Schema<?> $$1) {
      HashMap<String, Schema<?>> $$2 = new HashMap<>(this.properties);
      $$2.put($$0, $$1);
      return record($$2, this.codec);
   }

   public Schema<List<T>> asArray() {
      return arrayOf(this, this.codec);
   }
}
