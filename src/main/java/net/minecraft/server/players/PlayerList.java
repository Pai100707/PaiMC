package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.ChatType.Bound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.FileUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class PlayerList {
   public static final File USERBANLIST_FILE = new File("banned-players.json");
   public static final File IPBANLIST_FILE = new File("banned-ips.json");
   public static final File OPLIST_FILE = new File("ops.json");
   public static final File WHITELIST_FILE = new File("whitelist.json");
   public static final Component CHAT_FILTERED_FULL = Component.translatable("chat.filtered_full");
   public static final Component DUPLICATE_LOGIN_DISCONNECT_MESSAGE = Component.translatable("multiplayer.disconnect.duplicate_login");
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SEND_PLAYER_INFO_INTERVAL = 600;
   private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z", Locale.ROOT);
   private final net.minecraft.server.MinecraftServer server;
   private final List<ServerPlayer> players = Lists.newArrayList();
   private final Map<UUID, ServerPlayer> playersByUUID = Maps.newHashMap();
   private final UserBanList bans;
   private final IpBanList ipBans;
   private final ServerOpList ops;
   private final UserWhiteList whitelist;
   private final Map<UUID, ServerStatsCounter> stats = Maps.newHashMap();
   private final Map<UUID, net.minecraft.server.PlayerAdvancements> advancements = Maps.newHashMap();
   private final PlayerDataStorage playerIo;
   private final LayeredRegistryAccess<net.minecraft.server.RegistryLayer> registries;
   private int viewDistance;
   private int simulationDistance;
   private boolean allowCommandsForAllPlayers;
   private int sendAllPlayerInfoIn;

   public PlayerList(
      net.minecraft.server.MinecraftServer $$0, LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$1, PlayerDataStorage $$2, NotificationService $$3
   ) {
      this.server = $$0;
      this.registries = $$1;
      this.playerIo = $$2;
      this.whitelist = new UserWhiteList(WHITELIST_FILE, $$3);
      this.ops = new ServerOpList(OPLIST_FILE, $$3);
      this.bans = new UserBanList(USERBANLIST_FILE, $$3);
      this.ipBans = new IpBanList(IPBANLIST_FILE, $$3);
   }

   public void placeNewPlayer(Connection $$0, ServerPlayer $$1, CommonListenerCookie $$2) {
      NameAndId $$3 = $$1.nameAndId();
      UserNameToIdResolver $$4 = this.server.services().nameToIdCache();
      Optional<NameAndId> $$5 = $$4.get($$3.id());
      String $$6 = $$5.map(NameAndId::name).orElse($$3.name());
      $$4.add($$3);
      ServerLevel $$7 = $$1.level();
      String $$8 = $$0.getLoggableAddress(this.server.logIPs());
      LOGGER.info(
         "{}[{}] logged in with entity id {} at ({}, {}, {})", new Object[]{$$1.getPlainTextName(), $$8, $$1.getId(), $$1.getX(), $$1.getY(), $$1.getZ()}
      );
      LevelData $$9 = $$7.getLevelData();
      ServerGamePacketListenerImpl $$10 = new ServerGamePacketListenerImpl(this.server, $$0, $$1, $$2);
      $$0.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess()), $$10), $$10);
      $$10.suspendFlushing();
      GameRules $$11 = $$7.getGameRules();
      boolean $$12 = (Boolean)$$11.get(GameRules.IMMEDIATE_RESPAWN);
      boolean $$13 = (Boolean)$$11.get(GameRules.REDUCED_DEBUG_INFO);
      boolean $$14 = (Boolean)$$11.get(GameRules.LIMITED_CRAFTING);
      $$10.send(
         new ClientboundLoginPacket(
            $$1.getId(),
            $$9.isHardcore(),
            this.server.levelKeys(),
            this.getMaxPlayers(),
            this.getViewDistance(),
            this.getSimulationDistance(),
            $$13,
            !$$12,
            $$14,
            $$1.createCommonSpawnInfo($$7),
            this.server.enforceSecureProfile()
         )
      );
      $$10.send(new ClientboundChangeDifficultyPacket($$9.getDifficulty(), $$9.isDifficultyLocked()));
      $$10.send(new ClientboundPlayerAbilitiesPacket($$1.getAbilities()));
      $$10.send(new ClientboundSetHeldSlotPacket($$1.getInventory().getSelectedSlot()));
      RecipeManager $$15 = this.server.getRecipeManager();
      $$10.send(new ClientboundUpdateRecipesPacket($$15.getSynchronizedItemProperties(), $$15.getSynchronizedStonecutterRecipes()));
      this.sendPlayerPermissionLevel($$1);
      $$1.getStats().markAllDirty();
      $$1.getRecipeBook().sendInitialRecipeBook($$1);
      this.updateEntireScoreboard($$7.getScoreboard(), $$1);
      this.server.invalidateStatus();
      MutableComponent $$16;
      if ($$1.getGameProfile().name().equalsIgnoreCase($$6)) {
         $$16 = Component.translatable("multiplayer.player.joined", new Object[]{$$1.getDisplayName()});
      } else {
         $$16 = Component.translatable("multiplayer.player.joined.renamed", new Object[]{$$1.getDisplayName(), $$6});
      }

      this.broadcastSystemMessage($$16.withStyle(ChatFormatting.YELLOW), false);
      $$10.teleport($$1.getX(), $$1.getY(), $$1.getZ(), $$1.getYRot(), $$1.getXRot());
      ServerStatus $$18 = this.server.getStatus();
      if ($$18 != null && !$$2.transferred()) {
         $$1.sendServerStatus($$18);
      }

      $$1.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(this.players));
      this.players.add($$1);
      this.playersByUUID.put($$1.getUUID(), $$1);
      this.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of($$1)));
      this.sendLevelInfo($$1, $$7);
      $$7.addNewPlayer($$1);
      this.server.getCustomBossEvents().onPlayerConnect($$1);
      this.sendActivePlayerEffects($$1);
      $$1.initInventoryMenu();
      this.server.notificationManager().playerJoined($$1);
      $$10.resumeFlushing();
   }

   protected void updateEntireScoreboard(net.minecraft.server.ServerScoreboard $$0, ServerPlayer $$1) {
      Set<Objective> $$2 = Sets.newHashSet();

      for (PlayerTeam $$3 : $$0.getPlayerTeams()) {
         $$1.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket($$3, true));
      }

      for (DisplaySlot $$4 : DisplaySlot.values()) {
         Objective $$5 = $$0.getDisplayObjective($$4);
         if ($$5 != null && !$$2.contains($$5)) {
            for (Packet<?> $$7 : $$0.getStartTrackingPackets($$5)) {
               $$1.connection.send($$7);
            }

            $$2.add($$5);
         }
      }
   }

   public void addWorldborderListener(final ServerLevel $$0) {
      $$0.getWorldBorder().addListener(new BorderChangeListener() {
         public void onSetSize(WorldBorder $$0x, double $$1) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderSizePacket($$0), $$0.dimension());
         }

         public void onLerpSize(WorldBorder $$0x, double $$1, double $$2, long $$3, long $$4) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderLerpSizePacket($$0), $$0.dimension());
         }

         public void onSetCenter(WorldBorder $$0x, double $$1, double $$2) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderCenterPacket($$0), $$0.dimension());
         }

         public void onSetWarningTime(WorldBorder $$0x, int $$1) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDelayPacket($$0), $$0.dimension());
         }

         public void onSetWarningBlocks(WorldBorder $$0x, int $$1) {
            PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDistancePacket($$0), $$0.dimension());
         }

         public void onSetDamagePerBlock(WorldBorder $$0x, double $$1) {
         }

         public void onSetSafeZone(WorldBorder $$0x, double $$1) {
         }
      });
   }

   public Optional<CompoundTag> loadPlayerData(NameAndId $$0) {
      CompoundTag $$1 = this.server.getWorldData().getLoadedPlayerTag();
      if (this.server.isSingleplayerOwner($$0) && $$1 != null) {
         LOGGER.debug("loading single player");
         return Optional.of($$1);
      } else {
         return this.playerIo.load($$0);
      }
   }

   protected void save(ServerPlayer $$0) {
      this.playerIo.save($$0);
      ServerStatsCounter $$1 = this.stats.get($$0.getUUID());
      if ($$1 != null) {
         $$1.save();
      }

      net.minecraft.server.PlayerAdvancements $$2 = this.advancements.get($$0.getUUID());
      if ($$2 != null) {
         $$2.save();
      }
   }

   public void remove(ServerPlayer $$0) {
      ServerLevel $$1 = $$0.level();
      $$0.awardStat(Stats.LEAVE_GAME);
      this.save($$0);
      if ($$0.isPassenger()) {
         Entity $$2 = $$0.getRootVehicle();
         if ($$2.hasExactlyOnePlayerPassenger()) {
            LOGGER.debug("Removing player mount");
            $$0.stopRiding();
            $$2.getPassengersAndSelf().forEach($$0x -> $$0x.setRemoved(RemovalReason.UNLOADED_WITH_PLAYER));
         }
      }

      $$0.unRide();

      for (ThrownEnderpearl $$3 : $$0.getEnderPearls()) {
         $$3.setRemoved(RemovalReason.UNLOADED_WITH_PLAYER);
      }

      $$1.removePlayerImmediately($$0, RemovalReason.UNLOADED_WITH_PLAYER);
      $$0.getAdvancements().stopListening();
      this.players.remove($$0);
      this.server.getCustomBossEvents().onPlayerDisconnect($$0);
      UUID $$4 = $$0.getUUID();
      ServerPlayer $$5 = this.playersByUUID.get($$4);
      if ($$5 == $$0) {
         this.playersByUUID.remove($$4);
         this.stats.remove($$4);
         this.advancements.remove($$4);
         this.server.notificationManager().playerLeft($$0);
      }

      this.broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of($$0.getUUID())));
   }

   
   public Component canPlayerLogin(SocketAddress $$0, NameAndId $$1) {
      if (this.bans.isBanned($$1)) {
         UserBanListEntry $$2 = this.bans.get($$1);
         MutableComponent $$3 = Component.translatable("multiplayer.disconnect.banned.reason", new Object[]{$$2.getReasonMessage()});
         if ($$2.getExpires() != null) {
            $$3.append(Component.translatable("multiplayer.disconnect.banned.expiration", new Object[]{BAN_DATE_FORMAT.format($$2.getExpires())}));
         }

         return $$3;
      } else if (!this.isWhiteListed($$1)) {
         return Component.translatable("multiplayer.disconnect.not_whitelisted");
      } else if (this.ipBans.isBanned($$0)) {
         IpBanListEntry $$4 = this.ipBans.get($$0);
         MutableComponent $$5 = Component.translatable("multiplayer.disconnect.banned_ip.reason", new Object[]{$$4.getReasonMessage()});
         if ($$4.getExpires() != null) {
            $$5.append(Component.translatable("multiplayer.disconnect.banned_ip.expiration", new Object[]{BAN_DATE_FORMAT.format($$4.getExpires())}));
         }

         return $$5;
      } else {
         return this.players.size() >= this.getMaxPlayers() && !this.canBypassPlayerLimit($$1)
            ? Component.translatable("multiplayer.disconnect.server_full")
            : null;
      }
   }

   public boolean disconnectAllPlayersWithProfile(UUID $$0) {
      Set<ServerPlayer> $$1 = Sets.newIdentityHashSet();

      for (ServerPlayer $$2 : this.players) {
         if ($$2.getUUID().equals($$0)) {
            $$1.add($$2);
         }
      }

      ServerPlayer $$3 = this.playersByUUID.get($$0);
      if ($$3 != null) {
         $$1.add($$3);
      }

      for (ServerPlayer $$4 : $$1) {
         $$4.connection.disconnect(DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
      }

      return !$$1.isEmpty();
   }

   public ServerPlayer respawn(ServerPlayer $$0, boolean $$1, RemovalReason $$2) {
      TeleportTransition $$3 = $$0.findRespawnPositionAndUseSpawnBlock(!$$1, TeleportTransition.DO_NOTHING);
      this.players.remove($$0);
      $$0.level().removePlayerImmediately($$0, $$2);
      ServerLevel $$4 = $$3.newLevel();
      ServerPlayer $$5 = new ServerPlayer(this.server, $$4, $$0.getGameProfile(), $$0.clientInformation());
      $$5.connection = $$0.connection;
      $$5.restoreFrom($$0, $$1);
      $$5.setId($$0.getId());
      $$5.setMainArm($$0.getMainArm());
      if (!$$3.missingRespawnBlock()) {
         $$5.copyRespawnPosition($$0);
      }

      for (String $$6 : $$0.getTags()) {
         $$5.addTag($$6);
      }

      Vec3 $$7 = $$3.position();
      $$5.snapTo($$7.x, $$7.y, $$7.z, $$3.yRot(), $$3.xRot());
      if ($$3.missingRespawnBlock()) {
         $$5.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
      }

      byte $$8 = (byte)($$1 ? 1 : 0);
      ServerLevel $$9 = $$5.level();
      LevelData $$10 = $$9.getLevelData();
      $$5.connection.send(new ClientboundRespawnPacket($$5.createCommonSpawnInfo($$9), $$8));
      $$5.connection.teleport($$5.getX(), $$5.getY(), $$5.getZ(), $$5.getYRot(), $$5.getXRot());
      $$5.connection.send(new ClientboundSetDefaultSpawnPositionPacket($$4.getRespawnData()));
      $$5.connection.send(new ClientboundChangeDifficultyPacket($$10.getDifficulty(), $$10.isDifficultyLocked()));
      $$5.connection.send(new ClientboundSetExperiencePacket($$5.experienceProgress, $$5.totalExperience, $$5.experienceLevel));
      this.sendActivePlayerEffects($$5);
      this.sendLevelInfo($$5, $$4);
      this.sendPlayerPermissionLevel($$5);
      $$4.addRespawnedPlayer($$5);
      this.players.add($$5);
      this.playersByUUID.put($$5.getUUID(), $$5);
      $$5.initInventoryMenu();
      $$5.setHealth($$5.getHealth());
      ServerPlayer.RespawnConfig $$11 = $$5.getRespawnConfig();
      if (!$$1 && $$11 != null) {
         RespawnData $$12 = $$11.respawnData();
         ServerLevel $$13 = this.server.getLevel($$12.dimension());
         if ($$13 != null) {
            BlockPos $$14 = $$12.pos();
            BlockState $$15 = $$13.getBlockState($$14);
            if ($$15.is(Blocks.RESPAWN_ANCHOR)) {
               $$5.connection
                  .send(
                     new ClientboundSoundPacket(
                        SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, $$14.getX(), $$14.getY(), $$14.getZ(), 1.0F, 1.0F, $$4.getRandom().nextLong()
                     )
                  );
            }
         }
      }

      return $$5;
   }

   public void sendActivePlayerEffects(ServerPlayer $$0) {
      this.sendActiveEffects($$0, $$0.connection);
   }

   public void sendActiveEffects(LivingEntity $$0, ServerGamePacketListenerImpl $$1) {
      for (MobEffectInstance $$2 : $$0.getActiveEffects()) {
         $$1.send(new ClientboundUpdateMobEffectPacket($$0.getId(), $$2, false));
      }
   }

   public void sendPlayerPermissionLevel(ServerPlayer $$0) {
      LevelBasedPermissionSet $$1 = this.server.getProfilePermissions($$0.nameAndId());
      this.sendPlayerPermissionLevel($$0, $$1);
   }

   public void tick() {
      if (++this.sendAllPlayerInfoIn > 600) {
         this.broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(Action.UPDATE_LATENCY), this.players));
         this.sendAllPlayerInfoIn = 0;
      }
   }

   public void broadcastAll(Packet<?> $$0) {
      for (ServerPlayer $$1 : this.players) {
         $$1.connection.send($$0);
      }
   }

   public void broadcastAll(Packet<?> $$0, ResourceKey<Level> $$1) {
      for (ServerPlayer $$2 : this.players) {
         if ($$2.level().dimension() == $$1) {
            $$2.connection.send($$0);
         }
      }
   }

   public void broadcastSystemToTeam(Player $$0, Component $$1) {
      Team $$2 = $$0.getTeam();
      if ($$2 != null) {
         for (String $$4 : $$2.getPlayers()) {
            ServerPlayer $$5 = this.getPlayerByName($$4);
            if ($$5 != null && $$5 != $$0) {
               $$5.sendSystemMessage($$1);
            }
         }
      }
   }

   public void broadcastSystemToAllExceptTeam(Player $$0, Component $$1) {
      Team $$2 = $$0.getTeam();
      if ($$2 == null) {
         this.broadcastSystemMessage($$1, false);
      } else {
         for (int $$3 = 0; $$3 < this.players.size(); $$3++) {
            ServerPlayer $$4 = this.players.get($$3);
            if ($$4.getTeam() != $$2) {
               $$4.sendSystemMessage($$1);
            }
         }
      }
   }

   public String[] getPlayerNamesArray() {
      String[] $$0 = new String[this.players.size()];

      for (int $$1 = 0; $$1 < this.players.size(); $$1++) {
         $$0[$$1] = this.players.get($$1).getGameProfile().name();
      }

      return $$0;
   }

   public UserBanList getBans() {
      return this.bans;
   }

   public IpBanList getIpBans() {
      return this.ipBans;
   }

   public void op(NameAndId $$0) {
      this.op($$0, Optional.empty(), Optional.empty());
   }

   public void op(NameAndId $$0, Optional<LevelBasedPermissionSet> $$1, Optional<Boolean> $$2) {
      this.ops.add(new ServerOpListEntry($$0, $$1.orElse(this.server.operatorUserPermissions()), $$2.orElse(this.ops.canBypassPlayerLimit($$0))));
      ServerPlayer $$3 = this.getPlayer($$0.id());
      if ($$3 != null) {
         this.sendPlayerPermissionLevel($$3);
      }
   }

   public void deop(NameAndId $$0) {
      if (this.ops.remove($$0)) {
         ServerPlayer $$1 = this.getPlayer($$0.id());
         if ($$1 != null) {
            this.sendPlayerPermissionLevel($$1);
         }
      }
   }

   private void sendPlayerPermissionLevel(ServerPlayer $$0, LevelBasedPermissionSet $$1) {
      if ($$0.connection != null) {
         byte $$2 = switch ($$1.level()) {
            case ALL -> 24;
            case MODERATORS -> 25;
            case GAMEMASTERS -> 26;
            case ADMINS -> 27;
            case OWNERS -> 28;
         };
         $$0.connection.send(new ClientboundEntityEventPacket($$0, $$2));
      }

      this.server.getCommands().sendCommands($$0);
   }

   public boolean isWhiteListed(NameAndId $$0) {
      return !this.isUsingWhitelist() || this.ops.contains($$0) || this.whitelist.contains($$0);
   }

   public boolean isOp(NameAndId $$0) {
      return this.ops.contains($$0) || this.server.isSingleplayerOwner($$0) && this.server.getWorldData().isAllowCommands() || this.allowCommandsForAllPlayers;
   }

   
   public ServerPlayer getPlayerByName(String $$0) {
      int $$1 = this.players.size();

      for (int $$2 = 0; $$2 < $$1; $$2++) {
         ServerPlayer $$3 = this.players.get($$2);
         if ($$3.getGameProfile().name().equalsIgnoreCase($$0)) {
            return $$3;
         }
      }

      return null;
   }

   public void broadcast(Player $$0, double $$1, double $$2, double $$3, double $$4, ResourceKey<Level> $$5, Packet<?> $$6) {
      for (int $$7 = 0; $$7 < this.players.size(); $$7++) {
         ServerPlayer $$8 = this.players.get($$7);
         if ($$8 != $$0 && $$8.level().dimension() == $$5) {
            double $$9 = $$1 - $$8.getX();
            double $$10 = $$2 - $$8.getY();
            double $$11 = $$3 - $$8.getZ();
            if ($$9 * $$9 + $$10 * $$10 + $$11 * $$11 < $$4 * $$4) {
               $$8.connection.send($$6);
            }
         }
      }
   }

   public void saveAll() {
      for (int $$0 = 0; $$0 < this.players.size(); $$0++) {
         this.save(this.players.get($$0));
      }
   }

   public UserWhiteList getWhiteList() {
      return this.whitelist;
   }

   public String[] getWhiteListNames() {
      return this.whitelist.getUserList();
   }

   public ServerOpList getOps() {
      return this.ops;
   }

   public String[] getOpNames() {
      return this.ops.getUserList();
   }

   public void reloadWhiteList() {
   }

   public void sendLevelInfo(ServerPlayer $$0, ServerLevel $$1) {
      WorldBorder $$2 = $$1.getWorldBorder();
      $$0.connection.send(new ClientboundInitializeBorderPacket($$2));
      $$0.connection.send(new ClientboundSetTimePacket($$1.getGameTime(), $$1.getDayTime(), (Boolean)$$1.getGameRules().get(GameRules.ADVANCE_TIME)));
      $$0.connection.send(new ClientboundSetDefaultSpawnPositionPacket($$1.getRespawnData()));
      if ($$1.isRaining()) {
         $$0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
         $$0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, $$1.getRainLevel(1.0F)));
         $$0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, $$1.getThunderLevel(1.0F)));
      }

      $$0.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0F));
      this.server.tickRateManager().updateJoiningPlayer($$0);
   }

   public void sendAllPlayerInfo(ServerPlayer $$0) {
      $$0.inventoryMenu.sendAllDataToRemote();
      $$0.resetSentInfo();
      $$0.connection.send(new ClientboundSetHeldSlotPacket($$0.getInventory().getSelectedSlot()));
   }

   public int getPlayerCount() {
      return this.players.size();
   }

   public int getMaxPlayers() {
      return this.server.getMaxPlayers();
   }

   public boolean isUsingWhitelist() {
      return this.server.isUsingWhitelist();
   }

   public List<ServerPlayer> getPlayersWithAddress(String $$0) {
      List<ServerPlayer> $$1 = Lists.newArrayList();

      for (ServerPlayer $$2 : this.players) {
         if ($$2.getIpAddress().equals($$0)) {
            $$1.add($$2);
         }
      }

      return $$1;
   }

   public int getViewDistance() {
      return this.viewDistance;
   }

   public int getSimulationDistance() {
      return this.simulationDistance;
   }

   public net.minecraft.server.MinecraftServer getServer() {
      return this.server;
   }

   
   public CompoundTag getSingleplayerData() {
      return null;
   }

   public void setAllowCommandsForAllPlayers(boolean $$0) {
      this.allowCommandsForAllPlayers = $$0;
   }

   public void removeAll() {
      for (int $$0 = 0; $$0 < this.players.size(); $$0++) {
         this.players.get($$0).connection.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
      }
   }

   public void broadcastSystemMessage(Component $$0, boolean $$1) {
      this.broadcastSystemMessage($$0, $$1x -> $$0, $$1);
   }

   public void broadcastSystemMessage(Component $$0, Function<ServerPlayer, Component> $$1, boolean $$2) {
      this.server.sendSystemMessage($$0);

      for (ServerPlayer $$3 : this.players) {
         Component $$4 = $$1.apply($$3);
         if ($$4 != null) {
            $$3.sendSystemMessage($$4, $$2);
         }
      }
   }

   public void broadcastChatMessage(PlayerChatMessage $$0, CommandSourceStack $$1, Bound $$2) {
      this.broadcastChatMessage($$0, $$1::shouldFilterMessageTo, $$1.getPlayer(), $$2);
   }

   public void broadcastChatMessage(PlayerChatMessage $$0, ServerPlayer $$1, Bound $$2) {
      this.broadcastChatMessage($$0, $$1::shouldFilterMessageTo, $$1, $$2);
   }

   private void broadcastChatMessage(PlayerChatMessage $$0, Predicate<ServerPlayer> $$1, ServerPlayer $$2, Bound $$3) {
      boolean $$4 = this.verifyChatTrusted($$0);
      this.server.logChatMessage($$0.decoratedContent(), $$3, $$4 ? null : "Not Secure");
      OutgoingChatMessage $$5 = OutgoingChatMessage.create($$0);
      boolean $$6 = false;

      for (ServerPlayer $$7 : this.players) {
         boolean $$8 = $$1.test($$7);
         $$7.sendChatMessage($$5, $$8, $$3);
         $$6 |= $$8 && $$0.isFullyFiltered();
      }

      if ($$6 && $$2 != null) {
         $$2.sendSystemMessage(CHAT_FILTERED_FULL);
      }
   }

   private boolean verifyChatTrusted(PlayerChatMessage $$0) {
      return $$0.hasSignature() && !$$0.hasExpiredServer(Instant.now());
   }

   public ServerStatsCounter getPlayerStats(Player $$0) {
      GameProfile $$1 = $$0.getGameProfile();
      return this.stats.computeIfAbsent($$1.id(), $$1x -> {
         Path $$2 = this.locateStatsFile($$1);
         return new ServerStatsCounter(this.server, $$2);
      });
   }

   private Path locateStatsFile(GameProfile $$0) {
      Path $$1 = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR);
      Path $$2 = $$1.resolve($$0.id() + ".json");
      if (Files.exists($$2)) {
         return $$2;
      } else {
         String $$3 = $$0.name() + ".json";
         if (FileUtil.isValidPathSegment($$3)) {
            Path $$4 = $$1.resolve($$3);
            if (Files.isRegularFile($$4)) {
               try {
                  return Files.move($$4, $$2);
               } catch (IOException var7) {
                  LOGGER.warn("Failed to copy file {} to {}", $$3, $$2);
                  return $$4;
               }
            }
         }

         return $$2;
      }
   }

   public net.minecraft.server.PlayerAdvancements getPlayerAdvancements(ServerPlayer $$0) {
      UUID $$1 = $$0.getUUID();
      net.minecraft.server.PlayerAdvancements $$2 = this.advancements.get($$1);
      if ($$2 == null) {
         Path $$3 = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve($$1 + ".json");
         $$2 = new net.minecraft.server.PlayerAdvancements(this.server.getFixerUpper(), this, this.server.getAdvancements(), $$3, $$0);
         this.advancements.put($$1, $$2);
      }

      $$2.setPlayer($$0);
      return $$2;
   }

   public void setViewDistance(int $$0) {
      this.viewDistance = $$0;
      this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket($$0));

      for (ServerLevel $$1 : this.server.getAllLevels()) {
         $$1.getChunkSource().setViewDistance($$0);
      }
   }

   public void setSimulationDistance(int $$0) {
      this.simulationDistance = $$0;
      this.broadcastAll(new ClientboundSetSimulationDistancePacket($$0));

      for (ServerLevel $$1 : this.server.getAllLevels()) {
         $$1.getChunkSource().setSimulationDistance($$0);
      }
   }

   public List<ServerPlayer> getPlayers() {
      return this.players;
   }

   
   public ServerPlayer getPlayer(UUID $$0) {
      return this.playersByUUID.get($$0);
   }

   
   public ServerPlayer getPlayer(String $$0) {
      for (ServerPlayer $$1 : this.players) {
         if ($$1.getGameProfile().name().equalsIgnoreCase($$0)) {
            return $$1;
         }
      }

      return null;
   }

   public boolean canBypassPlayerLimit(NameAndId $$0) {
      return false;
   }

   public void reloadResources() {
      for (net.minecraft.server.PlayerAdvancements $$0 : this.advancements.values()) {
         $$0.reload(this.server.getAdvancements());
      }

      this.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
      RecipeManager $$1 = this.server.getRecipeManager();
      ClientboundUpdateRecipesPacket $$2 = new ClientboundUpdateRecipesPacket($$1.getSynchronizedItemProperties(), $$1.getSynchronizedStonecutterRecipes());

      for (ServerPlayer $$3 : this.players) {
         $$3.connection.send($$2);
         $$3.getRecipeBook().sendInitialRecipeBook($$3);
      }
   }

   public boolean isAllowCommandsForAllPlayers() {
      return this.allowCommandsForAllPlayers;
   }
}
