/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityAnchorArgument
implements ArgumentType<Anchor> {
    private static final Collection<String> EXAMPLES = Arrays.asList("eyes", "feet");
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.anchor.invalid", $$0));

    public static Anchor getAnchor(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (Anchor)((Object)$$0.getArgument($$1, Anchor.class));
    }

    public static EntityAnchorArgument anchor() {
        return new EntityAnchorArgument();
    }

    public Anchor parse(StringReader $$0) throws CommandSyntaxException {
        int $$1 = $$0.getCursor();
        String $$2 = $$0.readUnquotedString();
        Anchor $$3 = Anchor.getByName($$2);
        if ($$3 == null) {
            $$0.setCursor($$1);
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)$$0, (Object)$$2);
        }
        return $$3;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        return SharedSuggestionProvider.suggest(Anchor.BY_NAME.keySet(), $$1);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static enum Anchor {
        FEET("feet", ($$0, $$1) -> $$0),
        EYES("eyes", ($$0, $$1) -> new Vec3($$0.x, $$0.y + (double)$$1.getEyeHeight(), $$0.z));

        static final Map<String, Anchor> BY_NAME;
        private final String name;
        private final BiFunction<Vec3, Entity, Vec3> transform;

        private Anchor(String $$0, BiFunction<Vec3, Entity, Vec3> $$1) {
            this.name = $$0;
            this.transform = $$1;
        }

        public static @Nullable Anchor getByName(String $$0) {
            return BY_NAME.get($$0);
        }

        public Vec3 apply(Entity $$0) {
            return this.transform.apply($$0.position(), $$0);
        }

        public Vec3 apply(CommandSourceStack $$0) {
            Entity $$1 = $$0.getEntity();
            if ($$1 == null) {
                return $$0.getPosition();
            }
            return this.transform.apply($$0.getPosition(), $$1);
        }

        static {
            BY_NAME = Util.make(Maps.newHashMap(), $$0 -> {
                for (Anchor $$1 : Anchor.values()) {
                    $$0.put($$1.name, $$1);
                }
            });
        }
    }
}

