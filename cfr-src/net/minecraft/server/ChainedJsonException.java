/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class ChainedJsonException
extends IOException {
    private final List<Entry> entries = Lists.newArrayList();
    private final String message;

    public ChainedJsonException(String $$0) {
        this.entries.add(new Entry());
        this.message = $$0;
    }

    public ChainedJsonException(String $$0, Throwable $$1) {
        super($$1);
        this.entries.add(new Entry());
        this.message = $$0;
    }

    public void prependJsonKey(String $$0) {
        this.entries.get(0).addJsonKey($$0);
    }

    public void setFilenameAndFlush(String $$0) {
        this.entries.get((int)0).filename = $$0;
        this.entries.add(0, new Entry());
    }

    @Override
    public String getMessage() {
        return "Invalid " + String.valueOf(this.entries.get(this.entries.size() - 1)) + ": " + this.message;
    }

    public static ChainedJsonException forException(Exception $$0) {
        if ($$0 instanceof ChainedJsonException) {
            return (ChainedJsonException)$$0;
        }
        String $$1 = $$0.getMessage();
        if ($$0 instanceof FileNotFoundException) {
            $$1 = "File not found";
        }
        return new ChainedJsonException($$1, $$0);
    }

    public static class Entry {
        @Nullable String filename;
        private final List<String> jsonKeys = Lists.newArrayList();

        Entry() {
        }

        void addJsonKey(String $$0) {
            this.jsonKeys.add(0, $$0);
        }

        public @Nullable String getFilename() {
            return this.filename;
        }

        public String getJsonKeys() {
            return StringUtils.join(this.jsonKeys, (String)"->");
        }

        public String toString() {
            if (this.filename != null) {
                if (this.jsonKeys.isEmpty()) {
                    return this.filename;
                }
                return this.filename + " " + this.getJsonKeys();
            }
            if (this.jsonKeys.isEmpty()) {
                return "(Unknown file)";
            }
            return "(Unknown file) " + this.getJsonKeys();
        }
    }
}

