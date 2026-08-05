/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.LogTestReporter;
import net.minecraft.gametest.framework.TestReporter;

public class GlobalTestReporter {
    private static TestReporter DELEGATE = new LogTestReporter();

    public static void replaceWith(TestReporter $$0) {
        DELEGATE = $$0;
    }

    public static void onTestFailed(GameTestInfo $$0) {
        DELEGATE.onTestFailed($$0);
    }

    public static void onTestSuccess(GameTestInfo $$0) {
        DELEGATE.onTestSuccess($$0);
    }

    public static void finish() {
        DELEGATE.finish();
    }
}

