package net.minecraft.util.profiling;

import com.mojang.jtracy.TracyClient;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class Profiler {
   private static final ThreadLocal<TracyZoneFiller> TRACY_FILLER = ThreadLocal.withInitial(TracyZoneFiller::new);
   private static final ThreadLocal<ProfilerFiller> ACTIVE = new ThreadLocal<>();
   private static final AtomicInteger ACTIVE_COUNT = new AtomicInteger();

   private Profiler() {
   }

   public static Profiler.Scope use(ProfilerFiller $$0) {
      startUsing($$0);
      return Profiler::stopUsing;
   }

   private static void startUsing(ProfilerFiller $$0) {
      if (ACTIVE.get() != null) {
         throw new IllegalStateException("Profiler is already active");
      } else {
         ProfilerFiller $$1 = decorateFiller($$0);
         ACTIVE.set($$1);
         ACTIVE_COUNT.incrementAndGet();
         $$1.startTick();
      }
   }

   private static void stopUsing() {
      ProfilerFiller $$0 = ACTIVE.get();
      if ($$0 == null) {
         throw new IllegalStateException("Profiler was not active");
      } else {
         ACTIVE.remove();
         ACTIVE_COUNT.decrementAndGet();
         $$0.endTick();
      }
   }

   private static ProfilerFiller decorateFiller(ProfilerFiller $$0) {
      return ProfilerFiller.combine(getDefaultFiller(), $$0);
   }

   public static ProfilerFiller get() {
      return ACTIVE_COUNT.get() == 0 ? getDefaultFiller() : Objects.requireNonNullElseGet(ACTIVE.get(), Profiler::getDefaultFiller);
   }

   private static ProfilerFiller getDefaultFiller() {
      return (ProfilerFiller)(TracyClient.isAvailable() ? TRACY_FILLER.get() : InactiveProfiler.INSTANCE);
   }

   public interface Scope extends AutoCloseable {
      @Override
      void close();
   }
}
