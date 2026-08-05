/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.rcon.thread;

import com.mojang.logging.LogUtils;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class GenericThread
implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final int MAX_STOP_WAIT = 5;
    protected volatile boolean running;
    protected final String name;
    protected @Nullable Thread thread;

    protected GenericThread(String $$0) {
        this.name = $$0;
    }

    public synchronized boolean start() {
        if (this.running) {
            return true;
        }
        this.running = true;
        this.thread = new Thread((Runnable)this, this.name + " #" + UNIQUE_THREAD_ID.incrementAndGet());
        this.thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
        this.thread.start();
        LOGGER.info("Thread {} started", (Object)this.name);
        return true;
    }

    public synchronized void stop() {
        this.running = false;
        if (null == this.thread) {
            return;
        }
        int $$0 = 0;
        while (this.thread.isAlive()) {
            try {
                this.thread.join(1000L);
                if (++$$0 >= 5) {
                    LOGGER.warn("Waited {} seconds attempting force stop!", (Object)$$0);
                    continue;
                }
                if (!this.thread.isAlive()) continue;
                LOGGER.warn("Thread {} ({}) failed to exit after {} second(s)", new Object[]{this, this.thread.getState(), $$0, new Exception("Stack:")});
                this.thread.interrupt();
            }
            catch (InterruptedException interruptedException) {}
        }
        LOGGER.info("Thread {} stopped", (Object)this.name);
        this.thread = null;
    }

    public boolean isRunning() {
        return this.running;
    }
}

