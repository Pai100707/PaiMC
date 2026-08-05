/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft;

import net.minecraft.CrashReport;

public class ReportedException
extends RuntimeException {
    private final CrashReport report;

    public ReportedException(CrashReport $$0) {
        this.report = $$0;
    }

    public CrashReport getReport() {
        return this.report;
    }

    @Override
    public Throwable getCause() {
        return this.report.getException();
    }

    @Override
    public String getMessage() {
        return this.report.getTitle();
    }
}

