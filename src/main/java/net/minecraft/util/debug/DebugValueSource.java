package net.minecraft.util.debug;

import net.minecraft.server.level.ServerLevel;

public interface DebugValueSource {
   void registerDebugValues(ServerLevel var1, DebugValueSource.Registration var2);

   public interface Registration {
      <T> void register(DebugSubscription<T> var1, DebugValueSource.ValueGetter<T> var2);
   }

   public interface ValueGetter<T> {
      
      T get();
   }
}
