package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.SuspiciousStewEffects.Entry;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetStewEffectFunction extends LootItemConditionalFunction {
   private static final Codec<List<SetStewEffectFunction.EffectEntry>> EFFECTS_LIST = SetStewEffectFunction.EffectEntry.CODEC.listOf().validate($$0 -> {
      Set<Holder<MobEffect>> $$1 = new ObjectOpenHashSet();

      for (SetStewEffectFunction.EffectEntry $$2 : $$0) {
         if (!$$1.add($$2.effect())) {
            return DataResult.error(() -> "Encountered duplicate mob effect: '" + $$2.effect() + "'");
         }
      }

      return DataResult.success($$0);
   });
   public static final MapCodec<SetStewEffectFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(EFFECTS_LIST.optionalFieldOf("effects", List.of()).forGetter($$0x -> $$0x.effects)).apply($$0, SetStewEffectFunction::new)
   );
   private final List<SetStewEffectFunction.EffectEntry> effects;

   SetStewEffectFunction(List<LootItemCondition> $$0, List<SetStewEffectFunction.EffectEntry> $$1) {
      super($$0);
      this.effects = $$1;
   }

   @Override
   public LootItemFunctionType<SetStewEffectFunction> getType() {
      return LootItemFunctions.SET_STEW_EFFECT;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.effects.stream().flatMap($$0 -> $$0.duration().getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if ($$0.is(Items.SUSPICIOUS_STEW) && !this.effects.isEmpty()) {
         SetStewEffectFunction.EffectEntry $$2 = (SetStewEffectFunction.EffectEntry)Util.getRandom(this.effects, $$1.getRandom());
         Holder<MobEffect> $$3 = $$2.effect();
         int $$4 = $$2.duration().getInt($$1);
         if (!((MobEffect)$$3.value()).isInstantenous()) {
            $$4 *= 20;
         }

         Entry $$5 = new Entry($$3, $$4);
         $$0.update(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY, $$5, SuspiciousStewEffects::withEffectAdded);
         return $$0;
      } else {
         return $$0;
      }
   }

   public static SetStewEffectFunction.Builder stewEffect() {
      return new SetStewEffectFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetStewEffectFunction.Builder> {
      private final com.google.common.collect.ImmutableList.Builder<SetStewEffectFunction.EffectEntry> effects = ImmutableList.builder();

      protected SetStewEffectFunction.Builder getThis() {
         return this;
      }

      public SetStewEffectFunction.Builder withEffect(Holder<MobEffect> $$0, NumberProvider $$1) {
         this.effects.add(new SetStewEffectFunction.EffectEntry($$0, $$1));
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new SetStewEffectFunction(this.getConditions(), this.effects.build());
      }
   }

   record EffectEntry(Holder<MobEffect> effect, NumberProvider duration) {
      public static final Codec<SetStewEffectFunction.EffectEntry> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               MobEffect.CODEC.fieldOf("type").forGetter(SetStewEffectFunction.EffectEntry::effect),
               NumberProviders.CODEC.fieldOf("duration").forGetter(SetStewEffectFunction.EffectEntry::duration)
            )
            .apply($$0, SetStewEffectFunction.EffectEntry::new)
      );
   }
}
