package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetLoreFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetLoreFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               ComponentSerialization.CODEC.sizeLimitedListOf(256).fieldOf("lore").forGetter($$0x -> $$0x.lore),
               ListOperation.codec(256).forGetter($$0x -> $$0x.mode),
               LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter($$0x -> $$0x.resolutionContext)
            )
         )
         .apply($$0, SetLoreFunction::new)
   );
   private final List<Component> lore;
   private final ListOperation mode;
   private final Optional<LootContext.EntityTarget> resolutionContext;

   public SetLoreFunction(List<LootItemCondition> $$0, List<Component> $$1, ListOperation $$2, Optional<LootContext.EntityTarget> $$3) {
      super($$0);
      this.lore = List.copyOf($$1);
      this.mode = $$2;
      this.resolutionContext = $$3;
   }

   @Override
   public LootItemFunctionType<SetLoreFunction> getType() {
      return LootItemFunctions.SET_LORE;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.resolutionContext.<Set<ContextKey<?>>>map($$0 -> Set.of($$0.contextParam())).orElseGet(Set::of);
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      $$0.update(DataComponents.LORE, ItemLore.EMPTY, $$1x -> new ItemLore(this.updateLore($$1x, $$1)));
      return $$0;
   }

   private List<Component> updateLore(ItemLore $$0, LootContext $$1) {
      if ($$0 == null && this.lore.isEmpty()) {
         return List.of();
      } else {
         UnaryOperator<Component> $$2 = SetNameFunction.createResolver($$1, this.resolutionContext.orElse(null));
         List<Component> $$3 = this.lore.stream().map($$2).toList();
         return this.mode.apply($$0.lines(), $$3, 256);
      }
   }

   public static SetLoreFunction.Builder setLore() {
      return new SetLoreFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetLoreFunction.Builder> {
      private Optional<LootContext.EntityTarget> resolutionContext = Optional.empty();
      private final com.google.common.collect.ImmutableList.Builder<Component> lore = ImmutableList.builder();
      private ListOperation mode = ListOperation.Append.INSTANCE;

      public SetLoreFunction.Builder setMode(ListOperation $$0) {
         this.mode = $$0;
         return this;
      }

      public SetLoreFunction.Builder setResolutionContext(LootContext.EntityTarget $$0) {
         this.resolutionContext = Optional.of($$0);
         return this;
      }

      public SetLoreFunction.Builder addLine(Component $$0) {
         this.lore.add($$0);
         return this;
      }

      protected SetLoreFunction.Builder getThis() {
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new SetLoreFunction(this.getConditions(), this.lore.build(), this.mode, this.resolutionContext);
      }
   }
}
