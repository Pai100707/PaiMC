package net.minecraft.server.jsonrpc;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import org.slf4j.Logger;

public class JsonRpcLogger {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String PREFIX = "RPC Connection #{}: ";

   public void log(ClientInfo $$0, String $$1, Object... $$2) {
      if ($$2.length == 0) {
         LOGGER.info("RPC Connection #{}: " + $$1, $$0.connectionId());
      } else {
         List<Object> $$3 = new ArrayList<>(Arrays.asList($$2));
         $$3.addFirst($$0.connectionId());
         LOGGER.info("RPC Connection #{}: " + $$1, $$3.toArray());
      }
   }
}
