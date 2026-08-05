/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.chat.contents;

import java.util.Locale;
import net.minecraft.network.chat.contents.TranslatableContents;

public class TranslatableFormatException
extends IllegalArgumentException {
    public TranslatableFormatException(TranslatableContents $$0, String $$1) {
        super(String.format(Locale.ROOT, "Error parsing: %s: %s", $$0, $$1));
    }

    public TranslatableFormatException(TranslatableContents $$0, int $$1) {
        super(String.format(Locale.ROOT, "Invalid index %d requested for %s", $$1, $$0));
    }

    public TranslatableFormatException(TranslatableContents $$0, Throwable $$1) {
        super(String.format(Locale.ROOT, "Error while parsing: %s", $$0), $$1);
    }
}

