/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import org.jspecify.annotations.Nullable;

public class SnbtOperations {
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_STRING_UUID = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_string_uuid")));
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_NUMBER_OR_BOOLEAN = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_number_or_boolean")));
    public static final String BUILTIN_TRUE = "true";
    public static final String BUILTIN_FALSE = "false";
    public static final Map<BuiltinKey, BuiltinOperation> BUILTIN_OPERATIONS = Map.of(new BuiltinKey("bool", 1), new BuiltinOperation(){

        @Override
        public <T> T run(DynamicOps<T> $$0, List<T> $$1, ParseState<StringReader> $$2) {
            Boolean $$3 = 1.convert($$0, $$1.getFirst());
            if ($$3 == null) {
                $$2.errorCollector().store($$2.mark(), ERROR_EXPECTED_NUMBER_OR_BOOLEAN);
                return null;
            }
            return (T)$$0.createBoolean($$3.booleanValue());
        }

        private static <T> @Nullable Boolean convert(DynamicOps<T> $$0, T $$1) {
            Optional $$2 = $$0.getBooleanValue($$1).result();
            if ($$2.isPresent()) {
                return (Boolean)$$2.get();
            }
            Optional $$3 = $$0.getNumberValue($$1).result();
            if ($$3.isPresent()) {
                return ((Number)$$3.get()).doubleValue() != 0.0;
            }
            return null;
        }
    }, new BuiltinKey("uuid", 1), new BuiltinOperation(){

        /*
         * WARNING - void declaration
         */
        @Override
        public <T> T run(DynamicOps<T> $$0, List<T> $$1, ParseState<StringReader> $$2) {
            void $$6;
            Optional $$3 = $$0.getStringValue($$1.getFirst()).result();
            if ($$3.isEmpty()) {
                $$2.errorCollector().store($$2.mark(), ERROR_EXPECTED_STRING_UUID);
                return null;
            }
            try {
                UUID $$4 = UUID.fromString((String)$$3.get());
            }
            catch (IllegalArgumentException $$5) {
                $$2.errorCollector().store($$2.mark(), ERROR_EXPECTED_STRING_UUID);
                return null;
            }
            return (T)$$0.createIntList(IntStream.of(UUIDUtil.uuidToIntArray((UUID)$$6)));
        }
    });
    public static final SuggestionSupplier<StringReader> BUILTIN_IDS = new SuggestionSupplier<StringReader>(){
        private final Set<String> keys = Stream.concat(Stream.of("false", "true"), BUILTIN_OPERATIONS.keySet().stream().map(BuiltinKey::id)).collect(Collectors.toSet());

        @Override
        public Stream<String> possibleValues(ParseState<StringReader> $$0) {
            return this.keys.stream();
        }
    };

    public record BuiltinKey(String id, int argCount) {
        @Override
        public String toString() {
            return this.id + "/" + this.argCount;
        }
    }

    public static interface BuiltinOperation {
        public <T> @Nullable T run(DynamicOps<T> var1, List<T> var2, ParseState<StringReader> var3);
    }
}

