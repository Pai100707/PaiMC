package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

public class ParticleArgument implements ArgumentType<ParticleOptions> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle{foo:bar}");
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_PARTICLE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("particle.notFound", new Object[]{$$0})
   );
   public static final DynamicCommandExceptionType ERROR_INVALID_OPTIONS = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("particle.invalidOptions", new Object[]{$$0})
   );
   private final Provider registries;
   private static final TagParser<?> VALUE_PARSER = TagParser.create(NbtOps.INSTANCE);

   public ParticleArgument(net.minecraft.commands.CommandBuildContext $$0) {
      this.registries = $$0;
   }

   public static ParticleArgument particle(net.minecraft.commands.CommandBuildContext $$0) {
      return new ParticleArgument($$0);
   }

   public static ParticleOptions getParticle(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (ParticleOptions)$$0.getArgument($$1, ParticleOptions.class);
   }

   public ParticleOptions parse(StringReader $$0) throws CommandSyntaxException {
      return readParticle($$0, this.registries);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static ParticleOptions readParticle(StringReader $$0, Provider $$1) throws CommandSyntaxException {
      ParticleType<?> $$2 = readParticleType($$0, $$1.lookupOrThrow(Registries.PARTICLE_TYPE));
      return readParticle(VALUE_PARSER, $$0, (ParticleType<ParticleOptions>)$$2, $$1);
   }

   private static ParticleType<?> readParticleType(StringReader $$0, HolderLookup<ParticleType<?>> $$1) throws CommandSyntaxException {
      Identifier $$2 = Identifier.read($$0);
      ResourceKey<ParticleType<?>> $$3 = ResourceKey.create(Registries.PARTICLE_TYPE, $$2);
      return (ParticleType<?>)((Reference)$$1.get($$3).orElseThrow(() -> ERROR_UNKNOWN_PARTICLE.createWithContext($$0, $$2))).value();
   }

   private static <T extends ParticleOptions, O> T readParticle(TagParser<O> $$0, StringReader $$1, ParticleType<T> $$2, Provider $$3) throws CommandSyntaxException {
      RegistryOps<O> $$4 = $$3.createSerializationContext($$0.getOps());
      O $$5;
      if ($$1.canRead() && $$1.peek() == '{') {
         $$5 = (O)$$0.parseAsArgument($$1);
      } else {
         $$5 = (O)$$4.emptyMap();
      }

      return (T)$$2.codec().codec().parse($$4, $$5).getOrThrow(ERROR_INVALID_OPTIONS::create);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      RegistryLookup<ParticleType<?>> $$2 = this.registries.lookupOrThrow(Registries.PARTICLE_TYPE);
      return net.minecraft.commands.SharedSuggestionProvider.suggestResource($$2.listElementIds().map(ResourceKey::identifier), $$1);
   }
}
