/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

public record Atom<T>(String name) {
    @Override
    public String toString() {
        return "<" + this.name + ">";
    }

    public static <T> Atom<T> of(String $$0) {
        return new Atom<T>($$0);
    }
}

