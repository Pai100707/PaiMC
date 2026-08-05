/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands;

@FunctionalInterface
public interface CommandResultCallback {
    public static final CommandResultCallback EMPTY = new CommandResultCallback(){

        @Override
        public void onResult(boolean $$0, int $$1) {
        }

        public String toString() {
            return "<empty>";
        }
    };

    public void onResult(boolean var1, int var2);

    default public void onSuccess(int $$0) {
        this.onResult(true, $$0);
    }

    default public void onFailure() {
        this.onResult(false, 0);
    }

    public static CommandResultCallback chain(CommandResultCallback $$0, CommandResultCallback $$1) {
        if ($$0 == EMPTY) {
            return $$1;
        }
        if ($$1 == EMPTY) {
            return $$0;
        }
        return ($$2, $$3) -> {
            $$0.onResult($$2, $$3);
            $$1.onResult($$2, $$3);
        };
    }
}

