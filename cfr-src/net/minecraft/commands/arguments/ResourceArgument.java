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
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
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
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceArgument<T>
implements ArgumentType<Holder.Reference<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_NOT_SUMMONABLE_ENTITY = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("entity.not_summonable", $$0));
    public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_RESOURCE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("argument.resource.not_found", $$0, $$1));
    public static final Dynamic3CommandExceptionType ERROR_INVALID_RESOURCE_TYPE = new Dynamic3CommandExceptionType(($$0, $$1, $$2) -> Component.translatableEscape("argument.resource.invalid_type", $$0, $$1, $$2));
    final ResourceKey<? extends Registry<T>> registryKey;
    private final HolderLookup<T> registryLookup;

    public ResourceArgument(CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
        this.registryKey = $$1;
        this.registryLookup = $$0.lookupOrThrow($$1);
    }

    public static <T> ResourceArgument<T> resource(CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
        return new ResourceArgument<T>($$0, $$1);
    }

    public static <T> Holder.Reference<T> getResource(CommandContext<CommandSourceStack> $$0, String $$1, ResourceKey<Registry<T>> $$2) throws CommandSyntaxException {
        Holder.Reference $$3 = (Holder.Reference)$$0.getArgument($$1, Holder.Reference.class);
        ResourceKey $$4 = $$3.key();
        if ($$4.isFor($$2)) {
            return $$3;
        }
        throw ERROR_INVALID_RESOURCE_TYPE.create((Object)$$4.identifier(), (Object)$$4.registry(), (Object)$$2.identifier());
    }

    public static Holder.Reference<Attribute> getAttribute(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ResourceArgument.getResource($$0, $$1, Registries.ATTRIBUTE);
    }

    public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ResourceArgument.getResource($$0, $$1, Registries.CONFIGURED_FEATURE);
    }

    public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ResourceArgument.getResource($$0, $$1, Registries.STRUCTURE);
    }

    public static Holder.Reference<EntityType<?>> getEntityType(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ResourceArgument.getResource($$0, $$1, Registries.ENTITY_TYPE);
    }

    public static Holder.Reference<EntityType<?>> getSummonableEntityType(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        Holder.Reference<EntityType<?>> $$2 = ResourceArgument.getResource($$0, $$1, Registries.ENTITY_TYPE);
        if (!((EntityType)$$2.value()).canSummon()) {
            throw ERROR_NOT_SUMMONABLE_ENTITY.create((Object)$$2.key().identifier().toString());
        }
        return $$2;
    }

    public static Holder.Reference<MobEffect> getMobEffect(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ResourceArgument.getResource($$0, $$1, Registries.MOB_EFFECT);
    }

    public static Holder.Reference<Enchantment> getEnchantment(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ResourceArgument.getResource($$0, $$1, Registries.ENCHANTMENT);
    }

    public Holder.Reference<T> parse(StringReader $$0) throws CommandSyntaxException {
        Identifier $$1 = Identifier.read($$0);
        ResourceKey $$2 = ResourceKey.create(this.registryKey, $$1);
        return this.registryLookup.get($$2).orElseThrow(() -> ERROR_UNKNOWN_RESOURCE.createWithContext((ImmutableStringReader)$$0, (Object)$$1, (Object)this.registryKey.identifier()));
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
    implements ArgumentTypeInfo<ResourceArgument<T>, Template> {
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
        public Template unpack(ResourceArgument<T> $$0) {
            return new Template($$0.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> $$1) {
                this.registryKey = $$1;
            }

            @Override
            public ResourceArgument<T> instantiate(CommandBuildContext $$0) {
                return new ResourceArgument($$0, this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

