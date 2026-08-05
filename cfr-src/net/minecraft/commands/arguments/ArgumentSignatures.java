/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignableCommand;
import org.jspecify.annotations.Nullable;

public record ArgumentSignatures(List<Entry> entries) {
    public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
    private static final int MAX_ARGUMENT_COUNT = 8;
    private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

    public ArgumentSignatures(FriendlyByteBuf $$0) {
        this($$0.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), Entry::new));
    }

    public void write(FriendlyByteBuf $$02) {
        $$02.writeCollection(this.entries, ($$0, $$1) -> $$1.write((FriendlyByteBuf)((Object)$$0)));
    }

    public static ArgumentSignatures signCommand(SignableCommand<?> $$0, Signer $$12) {
        List<Entry> $$2 = $$0.arguments().stream().map($$1 -> {
            MessageSignature $$2 = $$12.sign($$1.value());
            if ($$2 != null) {
                return new Entry($$1.name(), $$2);
            }
            return null;
        }).filter(Objects::nonNull).toList();
        return new ArgumentSignatures($$2);
    }

    @FunctionalInterface
    public static interface Signer {
        public @Nullable MessageSignature sign(String var1);
    }

    public record Entry(String name, MessageSignature signature) {
        public Entry(FriendlyByteBuf $$0) {
            this($$0.readUtf(16), MessageSignature.read($$0));
        }

        public void write(FriendlyByteBuf $$0) {
            $$0.writeUtf(this.name, 16);
            MessageSignature.write($$0, this.signature);
        }
    }
}

