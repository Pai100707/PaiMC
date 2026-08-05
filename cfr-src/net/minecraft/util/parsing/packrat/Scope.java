/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.parsing.packrat;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.Atom;
import org.jspecify.annotations.Nullable;

public final class Scope {
    private static final int NOT_FOUND = -1;
    private static final Object FRAME_START_MARKER = new Object(){

        public String toString() {
            return "frame";
        }
    };
    private static final int ENTRY_STRIDE = 2;
    private @Nullable Object[] stack = new Object[128];
    private int topEntryKeyIndex = 0;
    private int topMarkerKeyIndex = 0;

    public Scope() {
        this.stack[0] = FRAME_START_MARKER;
        this.stack[1] = null;
    }

    private int valueIndex(Atom<?> $$0) {
        for (int $$1 = this.topEntryKeyIndex; $$1 > this.topMarkerKeyIndex; $$1 -= 2) {
            Object $$2 = this.stack[$$1];
            assert ($$2 instanceof Atom);
            if ($$2 != $$0) continue;
            return $$1 + 1;
        }
        return -1;
    }

    public int valueIndexForAny(Atom<?> ... $$0) {
        for (int $$1 = this.topEntryKeyIndex; $$1 > this.topMarkerKeyIndex; $$1 -= 2) {
            Object $$2 = this.stack[$$1];
            assert ($$2 instanceof Atom);
            for (Atom<?> $$3 : $$0) {
                if ($$3 != $$2) continue;
                return $$1 + 1;
            }
        }
        return -1;
    }

    private void ensureCapacity(int $$0) {
        int $$2 = this.topEntryKeyIndex + 1;
        int $$3 = $$2 + $$0 * 2;
        int $$1 = this.stack.length;
        if ($$3 >= $$1) {
            int $$4 = Util.growByHalf($$1, $$3 + 1);
            Object[] $$5 = new Object[$$4];
            System.arraycopy(this.stack, 0, $$5, 0, $$1);
            this.stack = $$5;
        }
        assert (this.validateStructure());
    }

    private void setupNewFrame() {
        this.topEntryKeyIndex += 2;
        this.stack[this.topEntryKeyIndex] = FRAME_START_MARKER;
        this.stack[this.topEntryKeyIndex + 1] = this.topMarkerKeyIndex;
        this.topMarkerKeyIndex = this.topEntryKeyIndex;
    }

    public void pushFrame() {
        this.ensureCapacity(1);
        this.setupNewFrame();
        assert (this.validateStructure());
    }

    private int getPreviousMarkerIndex(int $$0) {
        return (Integer)this.stack[$$0 + 1];
    }

    public void popFrame() {
        assert (this.topMarkerKeyIndex != 0);
        this.topEntryKeyIndex = this.topMarkerKeyIndex - 2;
        this.topMarkerKeyIndex = this.getPreviousMarkerIndex(this.topMarkerKeyIndex);
        assert (this.validateStructure());
    }

    public void splitFrame() {
        int $$0 = this.topMarkerKeyIndex;
        int $$1 = (this.topEntryKeyIndex - this.topMarkerKeyIndex) / 2;
        this.ensureCapacity($$1 + 1);
        this.setupNewFrame();
        int $$2 = $$0 + 2;
        int $$3 = this.topEntryKeyIndex;
        for (int $$4 = 0; $$4 < $$1; ++$$4) {
            $$3 += 2;
            Object $$5 = this.stack[$$2];
            assert ($$5 != null);
            this.stack[$$3] = $$5;
            this.stack[$$3 + 1] = null;
            $$2 += 2;
        }
        this.topEntryKeyIndex = $$3;
        assert (this.validateStructure());
    }

    public void clearFrameValues() {
        for (int $$0 = this.topEntryKeyIndex; $$0 > this.topMarkerKeyIndex; $$0 -= 2) {
            assert (this.stack[$$0] instanceof Atom);
            this.stack[$$0 + 1] = null;
        }
        assert (this.validateStructure());
    }

    public void mergeFrame() {
        int $$0;
        int $$1 = $$0 = this.getPreviousMarkerIndex(this.topMarkerKeyIndex);
        int $$2 = this.topMarkerKeyIndex;
        while ($$2 < this.topEntryKeyIndex) {
            $$1 += 2;
            Object $$3 = this.stack[$$2 += 2];
            assert ($$3 instanceof Atom);
            Object $$4 = this.stack[$$2 + 1];
            Object $$5 = this.stack[$$1];
            if ($$5 != $$3) {
                this.stack[$$1] = $$3;
                this.stack[$$1 + 1] = $$4;
                continue;
            }
            if ($$4 == null) continue;
            this.stack[$$1 + 1] = $$4;
        }
        this.topEntryKeyIndex = $$1;
        this.topMarkerKeyIndex = $$0;
        assert (this.validateStructure());
    }

    public <T> void put(Atom<T> $$0, @Nullable T $$1) {
        int $$2 = this.valueIndex($$0);
        if ($$2 != -1) {
            this.stack[$$2] = $$1;
        } else {
            this.ensureCapacity(1);
            this.topEntryKeyIndex += 2;
            this.stack[this.topEntryKeyIndex] = $$0;
            this.stack[this.topEntryKeyIndex + 1] = $$1;
        }
        assert (this.validateStructure());
    }

    public <T> @Nullable T get(Atom<T> $$0) {
        int $$1 = this.valueIndex($$0);
        return (T)($$1 != -1 ? this.stack[$$1] : null);
    }

    public <T> T getOrThrow(Atom<T> $$0) {
        int $$1 = this.valueIndex($$0);
        if ($$1 == -1) {
            throw new IllegalArgumentException("No value for atom " + String.valueOf($$0));
        }
        return (T)this.stack[$$1];
    }

    public <T> T getOrDefault(Atom<T> $$0, T $$1) {
        int $$2 = this.valueIndex($$0);
        return (T)($$2 != -1 ? this.stack[$$2] : $$1);
    }

    @SafeVarargs
    public final <T> @Nullable T getAny(Atom<? extends T> ... $$0) {
        int $$1 = this.valueIndexForAny($$0);
        return (T)($$1 != -1 ? this.stack[$$1] : null);
    }

    @SafeVarargs
    public final <T> T getAnyOrThrow(Atom<? extends T> ... $$0) {
        int $$1 = this.valueIndexForAny($$0);
        if ($$1 == -1) {
            throw new IllegalArgumentException("No value for atoms " + Arrays.toString($$0));
        }
        return (T)this.stack[$$1];
    }

    public String toString() {
        StringBuilder $$0 = new StringBuilder();
        boolean $$1 = true;
        for (int $$2 = 0; $$2 <= this.topEntryKeyIndex; $$2 += 2) {
            Object $$3 = this.stack[$$2];
            Object $$4 = this.stack[$$2 + 1];
            if ($$3 == FRAME_START_MARKER) {
                $$0.append('|');
                $$1 = true;
                continue;
            }
            if (!$$1) {
                $$0.append(',');
            }
            $$1 = false;
            $$0.append($$3).append(':').append($$4);
        }
        return $$0.toString();
    }

    @VisibleForTesting
    public Map<Atom<?>, ?> lastFrame() {
        HashMap<Atom, Object> $$0 = new HashMap<Atom, Object>();
        for (int $$1 = this.topEntryKeyIndex; $$1 > this.topMarkerKeyIndex; $$1 -= 2) {
            Object $$2 = this.stack[$$1];
            Object $$3 = this.stack[$$1 + 1];
            $$0.put((Atom)$$2, $$3);
        }
        return $$0;
    }

    public boolean hasOnlySingleFrame() {
        for (int $$0 = this.topEntryKeyIndex; $$0 > 0; --$$0) {
            if (this.stack[$$0] != FRAME_START_MARKER) continue;
            return false;
        }
        if (this.stack[0] != FRAME_START_MARKER) {
            throw new IllegalStateException("Corrupted stack");
        }
        return true;
    }

    private boolean validateStructure() {
        assert (this.topMarkerKeyIndex >= 0);
        assert (this.topEntryKeyIndex >= this.topMarkerKeyIndex);
        for (int $$0 = 0; $$0 <= this.topEntryKeyIndex; $$0 += 2) {
            Object $$1 = this.stack[$$0];
            if ($$1 == FRAME_START_MARKER || $$1 instanceof Atom) continue;
            return false;
        }
        int $$2 = this.topMarkerKeyIndex;
        while ($$2 != 0) {
            Object $$3 = this.stack[$$2];
            if ($$3 != FRAME_START_MARKER) {
                return false;
            }
            $$2 = this.getPreviousMarkerIndex($$2);
        }
        return true;
    }
}

