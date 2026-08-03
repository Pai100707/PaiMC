package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class LogTestReporter implements TestReporter {
   private static final Logger LOGGER = LogUtils.getLogger();

   @Override
   public void onTestFailed(GameTestInfo $$0) {
      String $$1 = $$0.getTestBlockPos().toShortString();
      if ($$0.isRequired()) {
         LOGGER.error("{} failed at {}! {}", new Object[]{$$0.id(), $$1, Util.describeError($$0.getError())});
      } else {
         LOGGER.warn("(optional) {} failed at {}. {}", new Object[]{$$0.id(), $$1, Util.describeError($$0.getError())});
      }
   }

   @Override
   public void onTestSuccess(GameTestInfo $$0) {
   }
}
