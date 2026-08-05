/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.visitors;

import java.util.List;
import net.minecraft.nbt.TagType;

public record FieldSelector(List<String> path, TagType<?> type, String name) {
    public FieldSelector(TagType<?> $$0, String $$1) {
        this(List.of(), $$0, $$1);
    }

    public FieldSelector(String $$0, TagType<?> $$1, String $$2) {
        this(List.of($$0), $$1, $$2);
    }

    public FieldSelector(String $$0, String $$1, TagType<?> $$2, String $$3) {
        this(List.of($$0, $$1), $$2, $$3);
    }
}

