/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.apache.commons.io.FilenameUtils
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.io.FilenameUtils;

public class ResourceSelectorArgument<T>
implements ArgumentType<Collection<Holder.Reference<T>>> {
    private static final Collection<String> EXAMPLES = List.of("minecraft:*", "*:asset", "*");
    public static final Dynamic2CommandExceptionType ERROR_NO_MATCHES = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("argument.resource_selector.not_found", $$0, $$1));
    final ResourceKey<? extends Registry<T>> registryKey;
    private final HolderLookup<T> registryLookup;

    ResourceSelectorArgument(CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
        this.registryKey = $$1;
        this.registryLookup = $$0.lookupOrThrow($$1);
    }

    public Collection<Holder.Reference<T>> parse(StringReader $$0) throws CommandSyntaxException {
        String $$12 = ResourceSelectorArgument.ensureNamespaced(ResourceSelectorArgument.readPattern($$0));
        List<Holder.Reference<T>> $$2 = this.registryLookup.listElements().filter($$1 -> ResourceSelectorArgument.matches($$12, $$1.key().identifier())).toList();
        if ($$2.isEmpty()) {
            throw ERROR_NO_MATCHES.createWithContext((ImmutableStringReader)$$0, (Object)$$12, (Object)this.registryKey.identifier());
        }
        return $$2;
    }

    public static <T> Collection<Holder.Reference<T>> parse(StringReader $$0, HolderLookup<T> $$12) {
        String $$2 = ResourceSelectorArgument.ensureNamespaced(ResourceSelectorArgument.readPattern($$0));
        return $$12.listElements().filter($$1 -> ResourceSelectorArgument.matches($$2, $$1.key().identifier())).toList();
    }

    private static String readPattern(StringReader $$0) {
        int $$1 = $$0.getCursor();
        while ($$0.canRead() && ResourceSelectorArgument.isAllowedPatternCharacter($$0.peek())) {
            $$0.skip();
        }
        return $$0.getString().substring($$1, $$0.getCursor());
    }

    private static boolean isAllowedPatternCharacter(char $$0) {
        return Identifier.isAllowedInIdentifier($$0) || $$0 == '*' || $$0 == '?';
    }

    private static String ensureNamespaced(String $$0) {
        if (!$$0.contains(":")) {
            return "minecraft:" + $$0;
        }
        return $$0;
    }

    private static boolean matches(String $$0, Identifier $$1) {
        return FilenameUtils.wildcardMatch((String)$$1.toString(), (String)$$0);
    }

    public static <T> ResourceSelectorArgument<T> resourceSelector(CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
        return new ResourceSelectorArgument<T>($$0, $$1);
    }

    public static <T> Collection<Holder.Reference<T>> getSelectedResources(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (Collection)$$0.getArgument($$1, Collection.class);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        return SharedSuggestionProvider.listSuggestions($$0, $$1, this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class Info<T>
    implements ArgumentTypeInfo<ResourceSelectorArgument<T>, Template> {
        @Override
        public void serializeToNetwork(Template $$0, FriendlyByteBuf $$1) {
            $$1.writeResourceKey($$0.registryKey);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf $$0) {
            return new Template($$0.readRegistryKey());
        }

        @Override
        public void serializeToJson(Template $$0, JsonObject $$1) {
            $$1.addProperty("registry", $$0.registryKey.identifier().toString());
        }

        @Override
        public Template unpack(ResourceSelectorArgument<T> $$0) {
            return new Template($$0.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceSelectorArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> $$1) {
                this.registryKey = $$1;
            }

            @Override
            public ResourceSelectorArgument<T> instantiate(CommandBuildContext $$0) {
                return new ResourceSelectorArgument($$0, this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceSelectorArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

