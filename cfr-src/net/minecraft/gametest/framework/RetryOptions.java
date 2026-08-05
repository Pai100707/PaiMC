/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

public record RetryOptions(int numberOfTries, boolean haltOnFailure) {
    private static final RetryOptions NO_RETRIES = new RetryOptions(1, true);

    public static RetryOptions noRetries() {
        return NO_RETRIES;
    }

    public boolean unlimitedTries() {
        return this.numberOfTries < 1;
    }

    public boolean hasTriesLeft(int $$0, int $$1) {
        boolean $$2 = $$0 != $$1;
        boolean $$3 = this.unlimitedTries() || $$0 < this.numberOfTries;
        return $$3 && (!$$2 || !this.haltOnFailure);
    }

    public boolean hasRetries() {
        return this.numberOfTries != 1;
    }
}

