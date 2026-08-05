/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import org.jspecify.annotations.Nullable;

public class CriterionProgress {
    private @Nullable Instant obtained;

    public CriterionProgress() {
    }

    public CriterionProgress(Instant $$0) {
        this.obtained = $$0;
    }

    public boolean isDone() {
        return this.obtained != null;
    }

    public void grant() {
        this.obtained = Instant.now();
    }

    public void revoke() {
        this.obtained = null;
    }

    public @Nullable Instant getObtained() {
        return this.obtained;
    }

    public String toString() {
        return "CriterionProgress{obtained=" + String.valueOf(this.obtained == null ? "false" : this.obtained) + "}";
    }

    public void serializeToNetwork(FriendlyByteBuf $$0) {
        $$0.writeNullable(this.obtained, FriendlyByteBuf::writeInstant);
    }

    public static CriterionProgress fromNetwork(FriendlyByteBuf $$0) {
        CriterionProgress $$1 = new CriterionProgress();
        $$1.obtained = $$0.readNullable(FriendlyByteBuf::readInstant);
        return $$1;
    }
}

