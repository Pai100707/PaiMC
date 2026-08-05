/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 */
package net.minecraft;

import org.slf4j.Logger;

public class DefaultUncaughtExceptionHandlerWithName
implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public DefaultUncaughtExceptionHandlerWithName(Logger $$0) {
        this.logger = $$0;
    }

    @Override
    public void uncaughtException(Thread $$0, Throwable $$1) {
        this.logger.error("Caught previously unhandled exception :");
        this.logger.error($$0.getName(), $$1);
    }
}

