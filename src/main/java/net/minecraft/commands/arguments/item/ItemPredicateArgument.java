package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate.Type;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument extends ParserBasedArgument<ItemPredicateArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo:'bar'}");
   static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.item.id.invalid", new Object[]{$$0})
   );
   static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("arguments.item.tag.unknown", new Object[]{$$0})
   );
   static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("arguments.item.component.unknown", new Object[]{$$0})
   );
   static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("arguments.item.component.malformed", new Object[]{$$0, $$1})
   );
   static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("arguments.item.predicate.unknown", new Object[]{$$0})
   );
   static final Dynamic2CommandExceptionType ERROR_MALFORMED_PREDICATE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("arguments.item.predicate.malformed", new Object[]{$$0, $$1})
   );
   private static final Identifier COUNT_ID = Identifier.withDefaultNamespace("count");
   static final Map<Identifier, ItemPredicateArgument.ComponentWrapper> PSEUDO_COMPONENTS = Stream.of(
         new ItemPredicateArgument.ComponentWrapper(COUNT_ID, $$0 -> true, Ints.CODEC.map($$0 -> $$1 -> $$0.matches($$1.getCount())))
      )
      .collect(Collectors.toUnmodifiableMap(ItemPredicateArgument.ComponentWrapper::id, $$0 -> (ItemPredicateArgument.ComponentWrapper)$$0));
   static final Map<Identifier, ItemPredicateArgument.PredicateWrapper> PSEUDO_PREDICATES = Stream.of(
         new ItemPredicateArgument.PredicateWrapper(COUNT_ID, Ints.CODEC.map($$0 -> $$1 -> $$0.matches($$1.getCount())))
      )
      .collect(Collectors.toUnmodifiableMap(ItemPredicateArgument.PredicateWrapper::id, $$0 -> (ItemPredicateArgument.PredicateWrapper)$$0));

   private static ItemPredicateArgument.PredicateWrapper createComponentExistencePredicate(Reference<DataComponentType<?>> $$0) {
      Predicate<ItemStack> $$1 = $$1x -> $$1x.has((DataComponentType)$$0.value());
      return new ItemPredicateArgument.PredicateWrapper($$0.key().identifier(), Unit.CODEC.map($$1x -> $$1));
   }

   public ItemPredicateArgument(net.minecraft.commands.CommandBuildContext $$0) {
      super(ComponentPredicateParser.createGrammar(new ItemPredicateArgument.Context($$0)).mapResult($$0x -> Util.allOf($$0x)::test));
   }

   public static ItemPredicateArgument itemPredicate(net.minecraft.commands.CommandBuildContext $$0) {
      return new ItemPredicateArgument($$0);
   }

   public static ItemPredicateArgument.Result getItemPredicate(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (ItemPredicateArgument.Result)$$0.getArgument($$1, ItemPredicateArgument.Result.class);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   record ComponentWrapper(Identifier id, Predicate<ItemStack> presenceChecker, Decoder<? extends Predicate<ItemStack>> valueChecker) {

      public static <T> ItemPredicateArgument.ComponentWrapper create(ImmutableStringReader $$0, Identifier $$1, DataComponentType<T> $$2) throws CommandSyntaxException {
         Codec<T> $$3 = $$2.codec();
         if ($$3 == null) {
            throw ItemPredicateArgument.ERROR_UNKNOWN_COMPONENT.createWithContext($$0, $$1);
         } else {
            return new ItemPredicateArgument.ComponentWrapper($$1, $$1x -> $$1x.has($$2), $$3.map($$1x -> $$2x -> {
               T $$3x = (T)$$2x.get($$2);
               return Objects.equals($$1x, $$3x);
            }));
         }
      }

      public Predicate<ItemStack> decode(ImmutableStringReader $$0, Dynamic<?> $$1) throws CommandSyntaxException {
         DataResult<? extends Predicate<ItemStack>> $$2 = this.valueChecker.parse($$1);
         return (Predicate<ItemStack>)$$2.getOrThrow($$1x -> ItemPredicateArgument.ERROR_MALFORMED_COMPONENT.createWithContext($$0, this.id.toString(), $$1x));
      }
   }

   static class Context
      implements ComponentPredicateParser.Context<Predicate<ItemStack>, ItemPredicateArgument.ComponentWrapper, ItemPredicateArgument.PredicateWrapper> {
      private final Provider registries;
      private final RegistryLookup<Item> items;
      private final RegistryLookup<DataComponentType<?>> components;
      private final RegistryLookup<Type<?>> predicates;

      Context(Provider $$0) {
         this.registries = $$0;
         this.items = $$0.lookupOrThrow(Registries.ITEM);
         this.components = $$0.lookupOrThrow(Registries.DATA_COMPONENT_TYPE);
         this.predicates = $$0.lookupOrThrow(Registries.DATA_COMPONENT_PREDICATE_TYPE);
      }

      public Predicate<ItemStack> forElementType(ImmutableStringReader $$0, Identifier $$1) throws CommandSyntaxException {
         Reference<Item> $$2 = (Reference<Item>)this.items
            .get(ResourceKey.create(Registries.ITEM, $$1))
            .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_ITEM.createWithContext($$0, $$1));
         return $$1x -> $$1x.is($$2);
      }

      public Predicate<ItemStack> forTagType(ImmutableStringReader $$0, Identifier $$1) throws CommandSyntaxException {
         HolderSet<Item> $$2 = (HolderSet<Item>)this.items
            .get(TagKey.create(Registries.ITEM, $$1))
            .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_TAG.createWithContext($$0, $$1));
         return $$1x -> $$1x.is($$2);
      }

      public ItemPredicateArgument.ComponentWrapper lookupComponentType(ImmutableStringReader $$0, Identifier $$1) throws CommandSyntaxException {
         ItemPredicateArgument.ComponentWrapper $$2 = ItemPredicateArgument.PSEUDO_COMPONENTS.get($$1);
         if ($$2 != null) {
            return $$2;
         } else {
            DataComponentType<?> $$3 = this.components
               .get(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, $$1))
               .<DataComponentType<?>>map(Holder::value)
               .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_COMPONENT.createWithContext($$0, $$1));
            return ItemPredicateArgument.ComponentWrapper.create($$0, $$1, $$3);
         }
      }

      public Predicate<ItemStack> createComponentTest(ImmutableStringReader $$0, ItemPredicateArgument.ComponentWrapper $$1, Dynamic<?> $$2) throws CommandSyntaxException {
         return $$1.decode($$0, RegistryOps.injectRegistryContext($$2, this.registries));
      }

      public Predicate<ItemStack> createComponentTest(ImmutableStringReader $$0, ItemPredicateArgument.ComponentWrapper $$1) {
         return $$1.presenceChecker;
      }

      public ItemPredicateArgument.PredicateWrapper lookupPredicateType(ImmutableStringReader $$0, Identifier $$1) throws CommandSyntaxException {
         ItemPredicateArgument.PredicateWrapper $$2 = ItemPredicateArgument.PSEUDO_PREDICATES.get($$1);
         return $$2 != null
            ? $$2
            : this.predicates
               .get(ResourceKey.create(Registries.DATA_COMPONENT_PREDICATE_TYPE, $$1))
               .map(ItemPredicateArgument.PredicateWrapper::new)
               .or(
                  () -> this.components
                     .get(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, $$1))
                     .map(ItemPredicateArgument::createComponentExistencePredicate)
               )
               .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_PREDICATE.createWithContext($$0, $$1));
      }

      public Predicate<ItemStack> createPredicateTest(ImmutableStringReader $$0, ItemPredicateArgument.PredicateWrapper $$1, Dynamic<?> $$2) throws CommandSyntaxException {
         return $$1.decode($$0, RegistryOps.injectRegistryContext($$2, this.registries));
      }

      @Override
      public Stream<Identifier> listElementTypes() {
         return this.items.listElementIds().map(ResourceKey::identifier);
      }

      @Override
      public Stream<Identifier> listTagTypes() {
         return this.items.listTagIds().map(TagKey::location);
      }

      @Override
      public Stream<Identifier> listComponentTypes() {
         return Stream.concat(
            ItemPredicateArgument.PSEUDO_COMPONENTS.keySet().stream(),
            this.components.listElements().filter($$0 -> !((DataComponentType)$$0.value()).isTransient()).map($$0 -> $$0.key().identifier())
         );
      }

      @Override
      public Stream<Identifier> listPredicateTypes() {
         return Stream.concat(ItemPredicateArgument.PSEUDO_PREDICATES.keySet().stream(), this.predicates.listElementIds().map(ResourceKey::identifier));
      }

      public Predicate<ItemStack> negate(Predicate<ItemStack> $$0) {
         return $$0.negate();
      }

      public Predicate<ItemStack> anyOf(List<Predicate<ItemStack>> $$0) {
         return Util.anyOf($$0);
      }
   }

   record PredicateWrapper(Identifier id, Decoder<? extends Predicate<ItemStack>> type) {
      public PredicateWrapper(Reference<Type<?>> $$0) {
         this($$0.key().identifier(), ((Type)$$0.value()).codec().map($$0x -> $$0x::matches));
      }

      public Predicate<ItemStack> decode(ImmutableStringReader $$0, Dynamic<?> $$1) throws CommandSyntaxException {
         DataResult<? extends Predicate<ItemStack>> $$2 = this.type.parse($$1);
         return (Predicate<ItemStack>)$$2.getOrThrow($$1x -> ItemPredicateArgument.ERROR_MALFORMED_PREDICATE.createWithContext($$0, this.id.toString(), $$1x));
      }
   }

   public interface Result extends Predicate<ItemStack> {
   }
}
