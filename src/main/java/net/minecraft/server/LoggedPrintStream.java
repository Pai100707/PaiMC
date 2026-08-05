package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;

public class LoggedPrintStream extends PrintStream {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final String name;

   public LoggedPrintStream(String $$0, OutputStream $$1) {
      super($$1, false, StandardCharsets.UTF_8);
      this.name = $$0;
   }

   @Override
   public void println(String $$0) {
      this.logLine($$0);
   }

   @Override
   public void println(Object $$0) {
      this.logLine(String.valueOf($$0));
   }

   protected void logLine(String $$0) {
      LOGGER.info("[{}]: {}", this.name, $$0);
   }
}
