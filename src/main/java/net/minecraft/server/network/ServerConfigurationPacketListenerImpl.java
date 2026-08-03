package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.config.JoinWorldTask;
import net.minecraft.server.network.config.PrepareSpawnTask;
import net.minecraft.server.network.config.ServerCodeOfConductConfigurationTask;
import net.minecraft.server.network.config.ServerResourcePackConfigurationTask;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.flag.FeatureFlags;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerConfigurationPacketListenerImpl extends ServerCommonPacketListenerImpl implements ServerConfigurationPacketListener, TickablePacketListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component DISCONNECT_REASON_INVALID_DATA = Component.translatable("multiplayer.disconnect.invalid_player_data");
   private static final Component DISCONNECT_REASON_CONFIGURATION_ERROR = Component.translatable("multiplayer.disconnect.configuration_error");
   private final GameProfile gameProfile;
   private final Queue<ConfigurationTask> configurationTasks = new ConcurrentLinkedQueue<>();
   @Nullable
   private ConfigurationTask currentTask;
   private ClientInformation clientInformation;
   @Nullable
   private SynchronizeRegistriesTask synchronizeRegistriesTask;
   @Nullable
   private PrepareSpawnTask prepareSpawnTask;

   public ServerConfigurationPacketListenerImpl(net.minecraft.server.MinecraftServer $$0, Connection $$1, CommonListenerCookie $$2) {
      super($$0, $$1, $$2);
      this.gameProfile = $$2.gameProfile();
      this.clientInformation = $$2.clientInformation();
   }

   @Override
   protected GameProfile playerProfile() {
      return this.gameProfile;
   }

   @Override
   public void onDisconnect(DisconnectionDetails $$0) {
      LOGGER.info("{} ({}) lost connection: {}", new Object[]{this.gameProfile.name(), this.gameProfile.id(), $$0.reason().getString()});
      if (this.prepareSpawnTask != null) {
         this.prepareSpawnTask.close();
         this.prepareSpawnTask = null;
      }

      super.onDisconnect($$0);
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }

   public void startConfiguration() {
      this.send(new ClientboundCustomPayloadPacket(new BrandPayload(this.server.getServerModName())));
      net.minecraft.server.ServerLinks $$0 = this.server.serverLinks();
      if (!$$0.isEmpty()) {
         this.send(new ClientboundServerLinksPacket($$0.untrust()));
      }

      LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$1 = this.server.registries();
      List<KnownPack> $$2 = this.server.getResourceManager().listPacks().flatMap($$0x -> $$0x.location().knownPackInfo().stream()).toList();
      this.send(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(this.server.getWorldData().enabledFeatures())));
      this.synchronizeRegistriesTask = new SynchronizeRegistriesTask($$2, $$1);
      this.configurationTasks.add(this.synchronizeRegistriesTask);
      this.addOptionalTasks();
      this.returnToWorld();
   }

   public void returnToWorld() {
      this.prepareSpawnTask = new PrepareSpawnTask(this.server, new NameAndId(this.gameProfile));
      this.configurationTasks.add(this.prepareSpawnTask);
      this.configurationTasks.add(new JoinWorldTask());
      this.startNextTask();
   }

   private void addOptionalTasks() {
      Map<String, String> $$0 = this.server.getCodeOfConducts();
      if (!$$0.isEmpty()) {
         this.configurationTasks.add(new ServerCodeOfConductConfigurationTask(() -> {
            String $$1 = $$0.get(this.clientInformation.language().toLowerCase(Locale.ROOT));
            if ($$1 == null) {
               $$1 = $$0.get("en_us");
            }

            if ($$1 == null) {
               $$1 = $$0.values().iterator().next();
            }

            return $$1;
         }));
      }

      this.server.getServerResourcePack().ifPresent($$0x -> this.configurationTasks.add(new ServerResourcePackConfigurationTask($$0x)));
   }

   public void handleClientInformation(ServerboundClientInformationPacket $$0) {
      this.clientInformation = $$0.information();
   }

   @Override
   public void handleResourcePackResponse(ServerboundResourcePackPacket $$0) {
      super.handleResourcePackResponse($$0);
      if ($$0.action().isTerminal()) {
         this.finishCurrentTask(ServerResourcePackConfigurationTask.TYPE);
      }
   }

   public void handleSelectKnownPacks(ServerboundSelectKnownPacks $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.server.packetProcessor());
      if (this.synchronizeRegistriesTask == null) {
         throw new IllegalStateException("Unexpected response from client: received pack selection, but no negotiation ongoing");
      } else {
         this.synchronizeRegistriesTask.handleResponse($$0.knownPacks(), this::send);
         this.finishCurrentTask(SynchronizeRegistriesTask.TYPE);
      }
   }

   public void handleAcceptCodeOfConduct(ServerboundAcceptCodeOfConductPacket $$0) {
      this.finishCurrentTask(ServerCodeOfConductConfigurationTask.TYPE);
   }

   public void handleConfigurationFinished(ServerboundFinishConfigurationPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.server.packetProcessor());
      this.finishCurrentTask(JoinWorldTask.TYPE);
      this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess())));

      try {
         PlayerList $$1 = this.server.getPlayerList();
         if ($$1.getPlayer(this.gameProfile.id()) != null) {
            this.disconnect(PlayerList.DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
            return;
         }

         Component $$2 = $$1.canPlayerLogin(this.connection.getRemoteAddress(), new NameAndId(this.gameProfile));
         if ($$2 != null) {
            this.disconnect($$2);
            return;
         }

         Objects.requireNonNull(this.prepareSpawnTask).spawnPlayer(this.connection, this.createCookie(this.clientInformation));
      } catch (Exception var4) {
         LOGGER.error("Couldn't place player in world", var4);
         this.disconnect(DISCONNECT_REASON_INVALID_DATA);
      }
   }

   public void tick() {
      this.keepConnectionAlive();
      ConfigurationTask $$0 = this.currentTask;
      if ($$0 != null) {
         try {
            if ($$0.tick()) {
               this.finishCurrentTask($$0.type());
            }
         } catch (Exception var3) {
            LOGGER.error("Failed to tick configuration task {}", $$0.type(), var3);
            this.disconnect(DISCONNECT_REASON_CONFIGURATION_ERROR);
         }
      }

      if (this.prepareSpawnTask != null) {
         this.prepareSpawnTask.keepAlive();
      }
   }

   private void startNextTask() {
      if (this.currentTask != null) {
         throw new IllegalStateException("Task " + this.currentTask.type().id() + " has not finished yet");
      } else if (this.isAcceptingMessages()) {
         ConfigurationTask $$0 = this.configurationTasks.poll();
         if ($$0 != null) {
            this.currentTask = $$0;

            try {
               $$0.start(this::send);
            } catch (Exception var3) {
               LOGGER.error("Failed to start configuration task {}", $$0.type(), var3);
               this.disconnect(DISCONNECT_REASON_CONFIGURATION_ERROR);
            }
         }
      }
   }

   private void finishCurrentTask(ConfigurationTask.Type $$0) {
      ConfigurationTask.Type $$1 = this.currentTask != null ? this.currentTask.type() : null;
      if (!$$0.equals($$1)) {
         throw new IllegalStateException("Unexpected request for task finish, current task: " + $$1 + ", requested: " + $$0);
      } else {
         this.currentTask = null;
         this.startNextTask();
      }
   }
}
