/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ThreadingDetector {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final Semaphore lock = new Semaphore(1);
    private final Lock stackTraceLock = new ReentrantLock();
    private volatile @Nullable Thread threadThatFailedToAcquire;
    private volatile @Nullable ReportedException fullException;

    public ThreadingDetector(String $$0) {
        this.name = $$0;
    }

    public void checkAndLock() {
        block6: {
            boolean $$0 = false;
            try {
                this.stackTraceLock.lock();
                if (this.lock.tryAcquire()) break block6;
                this.threadThatFailedToAcquire = Thread.currentThread();
                $$0 = true;
                this.stackTraceLock.unlock();
                try {
                    this.lock.acquire();
                }
                catch (InterruptedException $$1) {
                    Thread.currentThread().interrupt();
                }
                throw this.fullException;
            }
            finally {
                if (!$$0) {
                    this.stackTraceLock.unlock();
                }
            }
        }
    }

    public void checkAndUnlock() {
        try {
            this.stackTraceLock.lock();
            Thread $$0 = this.threadThatFailedToAcquire;
            if ($$0 != null) {
                ReportedException $$1;
                this.fullException = $$1 = ThreadingDetector.makeThreadingException(this.name, $$0);
                this.lock.release();
                throw $$1;
            }
            this.lock.release();
        }
        finally {
            this.stackTraceLock.unlock();
        }
    }

    public static ReportedException makeThreadingException(String $$0, @Nullable Thread $$1) {
        String $$2 = Stream.of(Thread.currentThread(), $$1).filter(Objects::nonNull).map(ThreadingDetector::stackTrace).collect(Collectors.joining("\n"));
        String $$3 = "Accessing " + $$0 + " from multiple threads";
        CrashReport $$4 = new CrashReport($$3, new IllegalStateException($$3));
        CrashReportCategory $$5 = $$4.addCategory("Thread dumps");
        $$5.setDetail("Thread dumps", $$2);
        LOGGER.error("Thread dumps: \n{}", (Object)$$2);
        return new ReportedException($$4);
    }

    private static String stackTrace(Thread $$0) {
        return $$0.getName() + ": \n\tat " + Arrays.stream($$0.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "));
    }
}

