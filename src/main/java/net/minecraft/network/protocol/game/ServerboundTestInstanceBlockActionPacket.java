package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity.Data;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity.Status;

public record ServerboundTestInstanceBlockActionPacket(BlockPos pos, ServerboundTestInstanceBlockActionPacket.Action action, Data data)
   implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ServerboundTestInstanceBlockActionPacket> STREAM_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      ServerboundTestInstanceBlockActionPacket::pos,
      ServerboundTestInstanceBlockActionPacket.Action.STREAM_CODEC,
      ServerboundTestInstanceBlockActionPacket::action,
      Data.STREAM_CODEC,
      ServerboundTestInstanceBlockActionPacket::data,
      ServerboundTestInstanceBlockActionPacket::new
   );

   public ServerboundTestInstanceBlockActionPacket(
      BlockPos $$0, ServerboundTestInstanceBlockActionPacket.Action $$1, Optional<ResourceKey<GameTestInstance>> $$2, Vec3i $$3, Rotation $$4, boolean $$5
   ) {
      this($$0, $$1, new Data($$2, $$3, $$4, $$5, Status.CLEARED, Optional.empty()));
   }

   @Override
   public PacketType<ServerboundTestInstanceBlockActionPacket> type() {
      return GamePacketTypes.SERVERBOUND_TEST_INSTANCE_BLOCK_ACTION;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleTestInstanceBlockAction(this);
   }

   public static enum Action {
      INIT(0),
      QUERY(1),
      SET(2),
      RESET(3),
      SAVE(4),
      EXPORT(5),
      RUN(6);

      private static final IntFunction<ServerboundTestInstanceBlockActionPacket.Action> BY_ID = ByIdMap.continuous(
         $$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO
      );
      public static final StreamCodec<ByteBuf, ServerboundTestInstanceBlockActionPacket.Action> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
      private final int id;

      private Action(final int $$0) {
         this.id = $$0;
      }
   }
}
