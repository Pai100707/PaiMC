/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 */
package net.minecraft;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.SharedConstants;

public record TracingExecutor(ExecutorService service) implements Executor
{
    public Executor forName(String $$0) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return $$1 -> this.service.execute(() -> {
                Thread $$2 = Thread.currentThread();
                String $$3 = $$2.getName();
                $$2.setName($$0);
                try (Zone $$4 = TracyClient.beginZone((String)$$0, (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                    $$1.run();
                }
                finally {
                    $$2.setName($$3);
                }
            });
        }
        if (TracyClient.isAvailable()) {
            return $$1 -> this.service.execute(() -> {
                try (Zone $$2 = TracyClient.beginZone((String)$$0, (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                    $$1.run();
                }
            });
        }
        return this.service;
    }

    @Override
    public void execute(Runnable $$0) {
        this.service.execute(TracingExecutor.wrapUnnamed($$0));
    }

    public void shutdownAndAwait(long $$0, TimeUnit $$1) {
        boolean $$4;
        this.service.shutdown();
        try {
            boolean $$2 = this.service.awaitTermination($$0, $$1);
        }
        catch (InterruptedException $$3) {
            $$4 = false;
        }
        if (!$$4) {
            this.service.shutdownNow();
        }
    }

    private static Runnable wrapUnnamed(Runnable $$0) {
        if (!TracyClient.isAvailable()) {
            return $$0;
        }
        return () -> {
            try (Zone $$1 = TracyClient.beginZone((String)"task", (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                $$0.run();
            }
        };
    }
}

