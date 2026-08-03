package net.minecraft.network.protocol.game;

import java.util.function.Function;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ServerboundInteractPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundInteractPacket> STREAM_CODEC = Packet.codec(
      ServerboundInteractPacket::write, ServerboundInteractPacket::new
   );
   private final int entityId;
   private final ServerboundInteractPacket.Action action;
   private final boolean usingSecondaryAction;
   static final ServerboundInteractPacket.Action ATTACK_ACTION = new ServerboundInteractPacket.Action() {
      @Override
      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.ATTACK;
      }

      @Override
      public void dispatch(ServerboundInteractPacket.Handler $$0) {
         $$0.onAttack();
      }

      @Override
      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
      }
   };

   private ServerboundInteractPacket(int $$0, boolean $$1, ServerboundInteractPacket.Action $$2) {
      this.entityId = $$0;
      this.action = $$2;
      this.usingSecondaryAction = $$1;
   }

   public static ServerboundInteractPacket createAttackPacket(Entity $$0, boolean $$1) {
      return new ServerboundInteractPacket($$0.getId(), $$1, ATTACK_ACTION);
   }

   public static ServerboundInteractPacket createInteractionPacket(Entity $$0, boolean $$1, InteractionHand $$2) {
      return new ServerboundInteractPacket($$0.getId(), $$1, new ServerboundInteractPacket.InteractionAction($$2));
   }

   public static ServerboundInteractPacket createInteractionPacket(Entity $$0, boolean $$1, InteractionHand $$2, Vec3 $$3) {
      return new ServerboundInteractPacket($$0.getId(), $$1, new ServerboundInteractPacket.InteractionAtLocationAction($$2, $$3));
   }

   private ServerboundInteractPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.entityId = $$0.readVarInt();
      ServerboundInteractPacket.ActionType $$1 = $$0.readEnum(ServerboundInteractPacket.ActionType.class);
      this.action = $$1.reader.apply($$0);
      this.usingSecondaryAction = $$0.readBoolean();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.entityId);
      $$0.writeEnum(this.action.getType());
      this.action.write($$0);
      $$0.writeBoolean(this.usingSecondaryAction);
   }

   @Override
   public PacketType<ServerboundInteractPacket> type() {
      return GamePacketTypes.SERVERBOUND_INTERACT;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleInteract(this);
   }

   @Nullable
   public Entity getTarget(ServerLevel $$0) {
      return $$0.getEntityOrPart(this.entityId);
   }

   public boolean isUsingSecondaryAction() {
      return this.usingSecondaryAction;
   }

   public boolean isWithinRange(ServerPlayer $$0, AABB $$1, double $$2) {
      return this.action.getType() == ServerboundInteractPacket.ActionType.ATTACK
         ? $$0.isWithinAttackRange($$1, $$2)
         : $$0.isWithinEntityInteractionRange($$1, $$2);
   }

   public void dispatch(ServerboundInteractPacket.Handler $$0) {
      this.action.dispatch($$0);
   }

   interface Action {
      ServerboundInteractPacket.ActionType getType();

      void dispatch(ServerboundInteractPacket.Handler var1);

      void write(net.minecraft.network.FriendlyByteBuf var1);
   }

   static enum ActionType {
      INTERACT(ServerboundInteractPacket.InteractionAction::new),
      ATTACK($$0 -> ServerboundInteractPacket.ATTACK_ACTION),
      INTERACT_AT(ServerboundInteractPacket.InteractionAtLocationAction::new);

      final Function<net.minecraft.network.FriendlyByteBuf, ServerboundInteractPacket.Action> reader;

      private ActionType(final Function<net.minecraft.network.FriendlyByteBuf, ServerboundInteractPacket.Action> $$0) {
         this.reader = $$0;
      }
   }

   public interface Handler {
      void onInteraction(InteractionHand var1);

      void onInteraction(InteractionHand var1, Vec3 var2);

      void onAttack();
   }

   static class InteractionAction implements ServerboundInteractPacket.Action {
      private final InteractionHand hand;

      InteractionAction(InteractionHand $$0) {
         this.hand = $$0;
      }

      private InteractionAction(net.minecraft.network.FriendlyByteBuf $$0) {
         this.hand = $$0.readEnum(InteractionHand.class);
      }

      @Override
      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.INTERACT;
      }

      @Override
      public void dispatch(ServerboundInteractPacket.Handler $$0) {
         $$0.onInteraction(this.hand);
      }

      @Override
      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeEnum(this.hand);
      }
   }

   static class InteractionAtLocationAction implements ServerboundInteractPacket.Action {
      private final InteractionHand hand;
      private final Vec3 location;

      InteractionAtLocationAction(InteractionHand $$0, Vec3 $$1) {
         this.hand = $$0;
         this.location = $$1;
      }

      private InteractionAtLocationAction(net.minecraft.network.FriendlyByteBuf $$0) {
         this.location = new Vec3($$0.readFloat(), $$0.readFloat(), $$0.readFloat());
         this.hand = $$0.readEnum(InteractionHand.class);
      }

      @Override
      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.INTERACT_AT;
      }

      @Override
      public void dispatch(ServerboundInteractPacket.Handler $$0) {
         $$0.onInteraction(this.hand, this.location);
      }

      @Override
      public void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeFloat((float)this.location.x);
         $$0.writeFloat((float)this.location.y);
         $$0.writeFloat((float)this.location.z);
         $$0.writeEnum(this.hand);
      }
   }
}
