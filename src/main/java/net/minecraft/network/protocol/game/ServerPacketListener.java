package net.minecraft.network.protocol.game;

import com.mojang.logging.LogUtils;
import net.minecraft.ReportedException;
import net.minecraft.network.protocol.Packet;
import org.slf4j.Logger;

public interface ServerPacketListener extends net.minecraft.network.ServerboundPacketListener {
   Logger LOGGER = LogUtils.getLogger();

   @Override
   default void onPacketError(Packet $$0, Exception $$1) throws ReportedException {
      LOGGER.error("Failed to handle packet {}, suppressing error", $$0, $$1);
   }
}
