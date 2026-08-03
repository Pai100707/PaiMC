package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ProfiledReloadInstance extends SimpleReloadInstance<ProfiledReloadInstance.State> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Stopwatch total = Stopwatch.createUnstarted();

   public static ReloadInstance of(ResourceManager $$0, List<PreparableReloadListener> $$1, Executor $$2, Executor $$3, CompletableFuture<Unit> $$4) {
      ProfiledReloadInstance $$5 = new ProfiledReloadInstance($$1);
      $$5.startTasks(
         $$2,
         $$3,
         $$0,
         $$1,
         ($$1x, $$2x, $$3x, $$4x, $$5x) -> {
            AtomicLong $$6 = new AtomicLong();
            AtomicLong $$7 = new AtomicLong();
            AtomicLong $$8 = new AtomicLong();
            AtomicLong $$9 = new AtomicLong();
            CompletableFuture<Void> $$10 = $$3x.reload(
               $$1x, profiledExecutor($$4x, $$6, $$7, $$3x.getName()), $$2x, profiledExecutor($$5x, $$8, $$9, $$3x.getName())
            );
            return $$10.thenApplyAsync($$5xx -> {
               LOGGER.debug("Finished reloading {}", $$3x.getName());
               return new ProfiledReloadInstance.State($$3x.getName(), $$6, $$7, $$8, $$9);
            }, $$3);
         },
         $$4
      );
      return $$5;
   }

   private ProfiledReloadInstance(List<PreparableReloadListener> $$0) {
      super($$0);
      this.total.start();
   }

   @Override
   protected CompletableFuture<List<ProfiledReloadInstance.State>> prepareTasks(
      Executor $$0,
      Executor $$1,
      ResourceManager $$2,
      List<PreparableReloadListener> $$3,
      SimpleReloadInstance.StateFactory<ProfiledReloadInstance.State> $$4,
      CompletableFuture<?> $$5
   ) {
      return super.prepareTasks($$0, $$1, $$2, $$3, $$4, $$5).thenApplyAsync(this::finish, $$1);
   }

   private static Executor profiledExecutor(Executor $$0, AtomicLong $$1, AtomicLong $$2, String $$3) {
      return $$4 -> $$0.execute(() -> {
         ProfilerFiller $$4x = Profiler.get();
         $$4x.push($$3);
         long $$5 = Util.getNanos();
         $$4.run();
         $$1.addAndGet(Util.getNanos() - $$5);
         $$2.incrementAndGet();
         $$4x.pop();
      });
   }

   private List<ProfiledReloadInstance.State> finish(List<ProfiledReloadInstance.State> $$0) {
      this.total.stop();
      long $$1 = 0L;
      LOGGER.info("Resource reload finished after {} ms", this.total.elapsed(TimeUnit.MILLISECONDS));

      for (ProfiledReloadInstance.State $$2 : $$0) {
         long $$3 = TimeUnit.NANOSECONDS.toMillis($$2.preparationNanos.get());
         long $$4 = $$2.preparationCount.get();
         long $$5 = TimeUnit.NANOSECONDS.toMillis($$2.reloadNanos.get());
         long $$6 = $$2.reloadCount.get();
         long $$7 = $$3 + $$5;
         long $$8 = $$4 + $$6;
         String $$9 = $$2.name;
         LOGGER.info(
            "{} took approximately {} tasks/{} ms ({} tasks/{} ms preparing, {} tasks/{} ms applying)", new Object[]{$$9, $$8, $$7, $$4, $$3, $$6, $$5}
         );
         $$1 += $$5;
      }

      LOGGER.info("Total blocking time: {} ms", $$1);
      return $$0;
   }

   public record State(String name, AtomicLong preparationNanos, AtomicLong preparationCount, AtomicLong reloadNanos, AtomicLong reloadCount) {
   }
}
