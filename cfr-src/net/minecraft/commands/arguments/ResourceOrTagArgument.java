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
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.datafixers.util.Either
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public class ResourceOrTagArgument<T>
implements ArgumentType<Result<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    private static final Dynamic2CommandExceptionType ERROR_UNKNOWN_TAG = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("argument.resource_tag.not_found", $$0, $$1));
    private static final Dynamic3CommandExceptionType ERROR_INVALID_TAG_TYPE = new Dynamic3CommandExceptionType(($$0, $$1, $$2) -> Component.translatableEscape("argument.resource_tag.invalid_type", $$0, $$1, $$2));
    private final HolderLookup<T> registryLookup;
    final ResourceKey<? extends Registry<T>> registryKey;

    public ResourceOrTagArgument(CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
        this.registryKey = $$1;
        this.registryLookup = $$0.lookupOrThrow($$1);
    }

    public static <T> ResourceOrTagArgument<T> resourceOrTag(CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
        return new ResourceOrTagArgument<T>($$0, $$1);
    }

    public static <T> Result<T> getResourceOrTag(CommandContext<CommandSourceStack> $$0, String $$1, ResourceKey<Registry<T>> $$2) throws CommandSyntaxException {
        Result $$3 = (Result)$$0.getArgument($$1, Result.class);
        Optional<Result<T>> $$4 = $$3.cast($$2);
        return $$4.orElseThrow(() -> (CommandSyntaxException)((Object)((Object)$$3.unwrap().map($$1 -> {
            ResourceKey $$2 = $$1.key();
            return ResourceArgument.ERROR_INVALID_RESOURCE_TYPE.create((Object)$$2.identifier(), (Object)$$2.registry(), (Object)$$2.identifier());
        }, $$1 -> {
            TagKey $$2 = $$1.key();
            return ERROR_INVALID_TAG_TYPE.create((Object)$$2.location(), $$2.registry(), (Object)$$2.identifier());
        }))));
    }

    public Result<T> parse(StringReader $$0) throws CommandSyntaxException {
        if ($$0.canRead() && $$0.peek() == '#') {
            int $$1 = $$0.getCursor();
            try {
                $$0.skip();
                Identifier $$2 = Identifier.read($$0);
                TagKey $$3 = TagKey.create(this.registryKey, $$2);
                HolderSet.Named $$4 = this.registryLookup.get($$3).orElseThrow(() -> ERROR_UNKNOWN_TAG.createWithContext((ImmutableStringReader)$$0, (Object)$$2, (Object)this.registryKey.identifier()));
                return new TagResult($$4);
            }
            catch (CommandSyntaxException $$5) {
                $$0.setCursor($$1);
                throw $$5;
            }
        }
        Identifier $$6 = Identifier.read($$0);
        ResourceKey $$7 = ResourceKey.create(this.registryKey, $$6);
        Holder.Reference $$8 = this.registryLookup.get($$7).orElseThrow(() -> ResourceArgument.ERROR_UNKNOWN_RESOURCE.createWithContext((ImmutableStringReader)$$0, (Object)$$6, (Object)this.registryKey.identifier()));
        return new ResourceResult($$8);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        return SharedSuggestionProvider.listSuggestions($$0, $$1, this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static interface Result<T>
    extends Predicate<Holder<T>> {
        public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap();

        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> var1);

        public String asPrintable();
    }

    record TagResult<T>(HolderSet.Named<T> tag) implements Result<T>
    {
        @Override
        public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap() {
            return Either.right(this.tag);
        }

        @Override
        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> $$0) {
            return this.tag.key().isFor($$0) ? Optional.of(this) : Optional.empty();
        }

        @Override
        public boolean test(Holder<T> $$0) {
            return this.tag.contains($$0);
        }

        @Override
        public String asPrintable() {
            return "#" + String.valueOf(this.tag.key().location());
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Holder)object);
        }
    }

    record ResourceResult<T>(Holder.Reference<T> value) implements Result<T>
    {
        @Override
        public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap() {
            return Either.left(this.value);
        }

        @Override
        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> $$0) {
            return this.value.key().isFor($$0) ? Optional.of(this) : Optional.empty();
        }

        @Override
        public boolean test(Holder<T> $$0) {
            return $$0.equals(this.value);
        }

        @Override
        public String asPrintable() {
            return this.value.key().identifier().toString();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Holder)object);
        }
    }

    public static class Info<T>
    implements ArgumentTypeInfo<ResourceOrTagArgument<T>, Template> {
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
        public Template unpack(ResourceOrTagArgument<T> $$0) {
            return new Template($$0.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceOrTagArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> $$1) {
                this.registryKey = $$1;
            }

            @Override
            public ResourceOrTagArgument<T> instantiate(CommandBuildContext $$0) {
                return new ResourceOrTagArgument($$0, this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceOrTagArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

