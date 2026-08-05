/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;

public enum PackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    UNKNOWN("unknown"),
    COMPATIBLE("compatible");

    public static final int UNKNOWN_VERSION = Integer.MAX_VALUE;
    private final Component description;
    private final Component confirmation;

    private PackCompatibility(String $$0) {
        this.description = Component.translatable("pack.incompatible." + $$0).withStyle(ChatFormatting.GRAY);
        this.confirmation = Component.translatable("pack.incompatible.confirm." + $$0);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static PackCompatibility forVersion(InclusiveRange<PackFormat> $$0, PackFormat $$1) {
        if ($$0.minInclusive().major() == Integer.MAX_VALUE) {
            return UNKNOWN;
        }
        if ($$0.maxInclusive().compareTo($$1) < 0) {
            return TOO_OLD;
        }
        if ($$1.compareTo($$0.minInclusive()) < 0) {
            return TOO_NEW;
        }
        return COMPATIBLE;
    }

    public Component getDescription() {
        return this.description;
    }

    public Component getConfirmation() {
        return this.confirmation;
    }
}

