package net.minecraft.util.debugchart;

import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugSubscriptions;

public enum RemoteDebugSampleType {
   TICK_TIME(DebugSubscriptions.DEDICATED_SERVER_TICK_TIME);

   private final DebugSubscription<?> subscription;

   private RemoteDebugSampleType(final DebugSubscription<?> $$0) {
      this.subscription = $$0;
   }

   public DebugSubscription<?> subscription() {
      return this.subscription;
   }
}
