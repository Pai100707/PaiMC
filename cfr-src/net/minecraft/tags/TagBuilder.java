/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;

public class TagBuilder {
    private final List<TagEntry> entries = new ArrayList<TagEntry>();

    public static TagBuilder create() {
        return new TagBuilder();
    }

    public List<TagEntry> build() {
        return List.copyOf(this.entries);
    }

    public TagBuilder add(TagEntry $$0) {
        this.entries.add($$0);
        return this;
    }

    public TagBuilder addElement(Identifier $$0) {
        return this.add(TagEntry.element($$0));
    }

    public TagBuilder addOptionalElement(Identifier $$0) {
        return this.add(TagEntry.optionalElement($$0));
    }

    public TagBuilder addTag(Identifier $$0) {
        return this.add(TagEntry.tag($$0));
    }

    public TagBuilder addOptionalTag(Identifier $$0) {
        return this.add(TagEntry.optionalTag($$0));
    }
}

