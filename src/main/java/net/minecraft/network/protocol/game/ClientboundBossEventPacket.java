package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.BossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;

public class ClientboundBossEventPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundBossEventPacket> STREAM_CODEC = Packet.codec(
      ClientboundBossEventPacket::write, ClientboundBossEventPacket::new
   );
   private static final int FLAG_DARKEN = 1;
   private static final int FLAG_MUSIC = 2;
   private static final int FLAG_FOG = 4;
   private final UUID id;
   private final ClientboundBossEventPacket.Operation operation;
   static final ClientboundBossEventPacket.Operation REMOVE_OPERATION = new ClientboundBossEventPacket.Operation() {
      @Override
      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.REMOVE;
      }

      @Override
      public void dispatch(UUID $$0, ClientboundBossEventPacket.Handler $$1) {
         $$1.remove($$0);
      }

      @Override
      public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      }
   };

   private ClientboundBossEventPacket(UUID $$0, ClientboundBossEventPacket.Operation $$1) {
      this.id = $$0;
      this.operation = $$1;
   }

   private ClientboundBossEventPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this.id = $$0.readUUID();
      ClientboundBossEventPacket.OperationType $$1 = $$0.readEnum(ClientboundBossEventPacket.OperationType.class);
      this.operation = $$1.reader.decode($$0);
   }

   public static ClientboundBossEventPacket createAddPacket(BossEvent $$0) {
      return new ClientboundBossEventPacket($$0.getId(), new ClientboundBossEventPacket.AddOperation($$0));
   }

   public static ClientboundBossEventPacket createRemovePacket(UUID $$0) {
      return new ClientboundBossEventPacket($$0, REMOVE_OPERATION);
   }

   public static ClientboundBossEventPacket createUpdateProgressPacket(BossEvent $$0) {
      return new ClientboundBossEventPacket($$0.getId(), new ClientboundBossEventPacket.UpdateProgressOperation($$0.getProgress()));
   }

   public static ClientboundBossEventPacket createUpdateNamePacket(BossEvent $$0) {
      return new ClientboundBossEventPacket($$0.getId(), new ClientboundBossEventPacket.UpdateNameOperation($$0.getName()));
   }

   public static ClientboundBossEventPacket createUpdateStylePacket(BossEvent $$0) {
      return new ClientboundBossEventPacket($$0.getId(), new ClientboundBossEventPacket.UpdateStyleOperation($$0.getColor(), $$0.getOverlay()));
   }

   public static ClientboundBossEventPacket createUpdatePropertiesPacket(BossEvent $$0) {
      return new ClientboundBossEventPacket(
         $$0.getId(), new ClientboundBossEventPacket.UpdatePropertiesOperation($$0.shouldDarkenScreen(), $$0.shouldPlayBossMusic(), $$0.shouldCreateWorldFog())
      );
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      $$0.writeUUID(this.id);
      $$0.writeEnum(this.operation.getType());
      this.operation.write($$0);
   }

   static int encodeProperties(boolean $$0, boolean $$1, boolean $$2) {
      int $$3 = 0;
      if ($$0) {
         $$3 |= 1;
      }

      if ($$1) {
         $$3 |= 2;
      }

      if ($$2) {
         $$3 |= 4;
      }

      return $$3;
   }

   @Override
   public PacketType<ClientboundBossEventPacket> type() {
      return GamePacketTypes.CLIENTBOUND_BOSS_EVENT;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleBossUpdate(this);
   }

   public void dispatch(ClientboundBossEventPacket.Handler $$0) {
      this.operation.dispatch(this.id, $$0);
   }

   static class AddOperation implements ClientboundBossEventPacket.Operation {
      private final Component name;
      private final float progress;
      private final BossBarColor color;
      private final BossBarOverlay overlay;
      private final boolean darkenScreen;
      private final boolean playMusic;
      private final boolean createWorldFog;

      AddOperation(BossEvent $$0) {
         this.name = $$0.getName();
         this.progress = $$0.getProgress();
         this.color = $$0.getColor();
         this.overlay = $$0.getOverlay();
         this.darkenScreen = $$0.shouldDarkenScreen();
         this.playMusic = $$0.shouldPlayBossMusic();
         this.createWorldFog = $$0.shouldCreateWorldFog();
      }

      private AddOperation(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         this.name = ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
         this.progress = $$0.readFloat();
         this.color = $$0.readEnum(BossBarColor.class);
         this.overlay = $$0.readEnum(BossBarOverlay.class);
         int $$1 = $$0.readUnsignedByte();
         this.darkenScreen = ($$1 & 1) > 0;
         this.playMusic = ($$1 & 2) > 0;
         this.createWorldFog = ($$1 & 4) > 0;
      }

      @Override
      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.ADD;
      }

      @Override
      public void dispatch(UUID $$0, ClientboundBossEventPacket.Handler $$1) {
         $$1.add($$0, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
      }

      @Override
      public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.name);
         $$0.writeFloat(this.progress);
         $$0.writeEnum(this.color);
         $$0.writeEnum(this.overlay);
         $$0.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
      }
   }

   public interface Handler {
      default void add(UUID $$0, Component $$1, float $$2, BossBarColor $$3, BossBarOverlay $$4, boolean $$5, boolean $$6, boolean $$7) {
      }

      default void remove(UUID $$0) {
      }

      default void updateProgress(UUID $$0, float $$1) {
      }

      default void updateName(UUID $$0, Component $$1) {
      }

      default void updateStyle(UUID $$0, BossBarColor $$1, BossBarOverlay $$2) {
      }

      default void updateProperties(UUID $$0, boolean $$1, boolean $$2, boolean $$3) {
      }
   }

   interface Operation {
      ClientboundBossEventPacket.OperationType getType();

      void dispatch(UUID var1, ClientboundBossEventPacket.Handler var2);

      void write(net.minecraft.network.RegistryFriendlyByteBuf var1);
   }

   static enum OperationType {
      ADD(ClientboundBossEventPacket.AddOperation::new),
      REMOVE($$0 -> ClientboundBossEventPacket.REMOVE_OPERATION),
      UPDATE_PROGRESS(ClientboundBossEventPacket.UpdateProgressOperation::new),
      UPDATE_NAME(ClientboundBossEventPacket.UpdateNameOperation::new),
      UPDATE_STYLE(ClientboundBossEventPacket.UpdateStyleOperation::new),
      UPDATE_PROPERTIES(ClientboundBossEventPacket.UpdatePropertiesOperation::new);

      final StreamDecoder<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundBossEventPacket.Operation> reader;

      private OperationType(final StreamDecoder<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundBossEventPacket.Operation> $$0) {
         this.reader = $$0;
      }
   }

   record UpdateNameOperation(Component name) implements ClientboundBossEventPacket.Operation {
      private UpdateNameOperation(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         this(ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0));
      }

      @Override
      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_NAME;
      }

      @Override
      public void dispatch(UUID $$0, ClientboundBossEventPacket.Handler $$1) {
         $$1.updateName($$0, this.name);
      }

      @Override
      public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.name);
      }
   }

   record UpdateProgressOperation(float progress) implements ClientboundBossEventPacket.Operation {
      private UpdateProgressOperation(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         this($$0.readFloat());
      }

      @Override
      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_PROGRESS;
      }

      @Override
      public void dispatch(UUID $$0, ClientboundBossEventPacket.Handler $$1) {
         $$1.updateProgress($$0, this.progress);
      }

      @Override
      public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         $$0.writeFloat(this.progress);
      }
   }

   static class UpdatePropertiesOperation implements ClientboundBossEventPacket.Operation {
      private final boolean darkenScreen;
      private final boolean playMusic;
      private final boolean createWorldFog;

      UpdatePropertiesOperation(boolean $$0, boolean $$1, boolean $$2) {
         this.darkenScreen = $$0;
         this.playMusic = $$1;
         this.createWorldFog = $$2;
      }

      private UpdatePropertiesOperation(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         int $$1 = $$0.readUnsignedByte();
         this.darkenScreen = ($$1 & 1) > 0;
         this.playMusic = ($$1 & 2) > 0;
         this.createWorldFog = ($$1 & 4) > 0;
      }

      @Override
      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_PROPERTIES;
      }

      @Override
      public void dispatch(UUID $$0, ClientboundBossEventPacket.Handler $$1) {
         $$1.updateProperties($$0, this.darkenScreen, this.playMusic, this.createWorldFog);
      }

      @Override
      public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         $$0.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
      }
   }

   static class UpdateStyleOperation implements ClientboundBossEventPacket.Operation {
      private final BossBarColor color;
      private final BossBarOverlay overlay;

      UpdateStyleOperation(BossBarColor $$0, BossBarOverlay $$1) {
         this.color = $$0;
         this.overlay = $$1;
      }

      private UpdateStyleOperation(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         this.color = $$0.readEnum(BossBarColor.class);
         this.overlay = $$0.readEnum(BossBarOverlay.class);
      }

      @Override
      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_STYLE;
      }

      @Override
      public void dispatch(UUID $$0, ClientboundBossEventPacket.Handler $$1) {
         $$1.updateStyle($$0, this.color, this.overlay);
      }

      @Override
      public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         $$0.writeEnum(this.color);
         $$0.writeEnum(this.overlay);
      }
   }
}
