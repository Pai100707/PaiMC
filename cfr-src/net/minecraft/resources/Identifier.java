/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.resources;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.function.UnaryOperator;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

public final class Identifier
implements Comparable<Identifier> {
    public static final Codec<Identifier> CODEC = Codec.STRING.comapFlatMap(Identifier::read, Identifier::toString).stable();
    public static final StreamCodec<ByteBuf, Identifier> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Identifier::parse, Identifier::toString);
    public static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType((Message)Component.translatable("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String REALMS_NAMESPACE = "realms";
    private final String namespace;
    private final String path;

    private Identifier(String $$0, String $$1) {
        assert (Identifier.isValidNamespace($$0));
        assert (Identifier.isValidPath($$1));
        this.namespace = $$0;
        this.path = $$1;
    }

    private static Identifier createUntrusted(String $$0, String $$1) {
        return new Identifier(Identifier.assertValidNamespace($$0, $$1), Identifier.assertValidPath($$0, $$1));
    }

    public static Identifier fromNamespaceAndPath(String $$0, String $$1) {
        return Identifier.createUntrusted($$0, $$1);
    }

    public static Identifier parse(String $$0) {
        return Identifier.bySeparator($$0, ':');
    }

    public static Identifier withDefaultNamespace(String $$0) {
        return new Identifier(DEFAULT_NAMESPACE, Identifier.assertValidPath(DEFAULT_NAMESPACE, $$0));
    }

    public static @Nullable Identifier tryParse(String $$0) {
        return Identifier.tryBySeparator($$0, ':');
    }

    public static @Nullable Identifier tryBuild(String $$0, String $$1) {
        if (Identifier.isValidNamespace($$0) && Identifier.isValidPath($$1)) {
            return new Identifier($$0, $$1);
        }
        return null;
    }

    public static Identifier bySeparator(String $$0, char $$1) {
        int $$2 = $$0.indexOf($$1);
        if ($$2 >= 0) {
            String $$3 = $$0.substring($$2 + 1);
            if ($$2 != 0) {
                String $$4 = $$0.substring(0, $$2);
                return Identifier.createUntrusted($$4, $$3);
            }
            return Identifier.withDefaultNamespace($$3);
        }
        return Identifier.withDefaultNamespace($$0);
    }

    public static @Nullable Identifier tryBySeparator(String $$0, char $$1) {
        int $$2 = $$0.indexOf($$1);
        if ($$2 >= 0) {
            String $$3 = $$0.substring($$2 + 1);
            if (!Identifier.isValidPath($$3)) {
                return null;
            }
            if ($$2 != 0) {
                String $$4 = $$0.substring(0, $$2);
                return Identifier.isValidNamespace($$4) ? new Identifier($$4, $$3) : null;
            }
            return new Identifier(DEFAULT_NAMESPACE, $$3);
        }
        return Identifier.isValidPath($$0) ? new Identifier(DEFAULT_NAMESPACE, $$0) : null;
    }

    public static DataResult<Identifier> read(String $$0) {
        try {
            return DataResult.success((Object)Identifier.parse($$0));
        }
        catch (IdentifierException $$1) {
            return DataResult.error(() -> "Not a valid resource location: " + $$0 + " " + $$1.getMessage());
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public Identifier withPath(String $$0) {
        return new Identifier(this.namespace, Identifier.assertValidPath(this.namespace, $$0));
    }

    public Identifier withPath(UnaryOperator<String> $$0) {
        return this.withPath((String)$$0.apply(this.path));
    }

    public Identifier withPrefix(String $$0) {
        return this.withPath($$0 + this.path);
    }

    public Identifier withSuffix(String $$0) {
        return this.withPath(this.path + $$0);
    }

    public String toString() {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 instanceof Identifier) {
            Identifier $$1 = (Identifier)$$0;
            return this.namespace.equals($$1.namespace) && this.path.equals($$1.path);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    @Override
    public int compareTo(Identifier $$0) {
        int $$1 = this.path.compareTo($$0.path);
        if ($$1 == 0) {
            $$1 = this.namespace.compareTo($$0.namespace);
        }
        return $$1;
    }

    public String toDebugFileName() {
        return this.toString().replace('/', '_').replace(':', '_');
    }

    public String toLanguageKey() {
        return this.namespace + "." + this.path;
    }

    public String toShortLanguageKey() {
        return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toLanguageKey();
    }

    public String toShortString() {
        return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toString();
    }

    public String toLanguageKey(String $$0) {
        return $$0 + "." + this.toLanguageKey();
    }

    public String toLanguageKey(String $$0, String $$1) {
        return $$0 + "." + this.toLanguageKey() + "." + $$1;
    }

    private static String readGreedy(StringReader $$0) {
        int $$1 = $$0.getCursor();
        while ($$0.canRead() && Identifier.isAllowedInIdentifier($$0.peek())) {
            $$0.skip();
        }
        return $$0.getString().substring($$1, $$0.getCursor());
    }

    public static Identifier read(StringReader $$0) throws CommandSyntaxException {
        int $$1 = $$0.getCursor();
        String $$2 = Identifier.readGreedy($$0);
        try {
            return Identifier.parse($$2);
        }
        catch (IdentifierException $$3) {
            $$0.setCursor($$1);
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)$$0);
        }
    }

    public static Identifier readNonEmpty(StringReader $$0) throws CommandSyntaxException {
        int $$1 = $$0.getCursor();
        String $$2 = Identifier.readGreedy($$0);
        if ($$2.isEmpty()) {
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)$$0);
        }
        try {
            return Identifier.parse($$2);
        }
        catch (IdentifierException $$3) {
            $$0.setCursor($$1);
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)$$0);
        }
    }

    public static boolean isAllowedInIdentifier(char $$0) {
        return $$0 >= '0' && $$0 <= '9' || $$0 >= 'a' && $$0 <= 'z' || $$0 == '_' || $$0 == ':' || $$0 == '/' || $$0 == '.' || $$0 == '-';
    }

    public static boolean isValidPath(String $$0) {
        for (int $$1 = 0; $$1 < $$0.length(); ++$$1) {
            if (Identifier.validPathChar($$0.charAt($$1))) continue;
            return false;
        }
        return true;
    }

    public static boolean isValidNamespace(String $$0) {
        for (int $$1 = 0; $$1 < $$0.length(); ++$$1) {
            if (Identifier.validNamespaceChar($$0.charAt($$1))) continue;
            return false;
        }
        return true;
    }

    private static String assertValidNamespace(String $$0, String $$1) {
        if (!Identifier.isValidNamespace($$0)) {
            throw new IdentifierException("Non [a-z0-9_.-] character in namespace of location: " + $$0 + ":" + $$1);
        }
        return $$0;
    }

    public static boolean validPathChar(char $$0) {
        return $$0 == '_' || $$0 == '-' || $$0 >= 'a' && $$0 <= 'z' || $$0 >= '0' && $$0 <= '9' || $$0 == '/' || $$0 == '.';
    }

    private static boolean validNamespaceChar(char $$0) {
        return $$0 == '_' || $$0 == '-' || $$0 >= 'a' && $$0 <= 'z' || $$0 >= '0' && $$0 <= '9' || $$0 == '.';
    }

    private static String assertValidPath(String $$0, String $$1) {
        if (!Identifier.isValidPath($$1)) {
            throw new IdentifierException("Non [a-z0-9/._-] character in path of location: " + $$0 + ":" + $$1);
        }
        return $$1;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((Identifier)object);
    }
}

