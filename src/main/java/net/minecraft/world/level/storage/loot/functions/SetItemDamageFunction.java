package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.slf4j.Logger;

public class SetItemDamageFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec<SetItemDamageFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               NumberProviders.CODEC.fieldOf("damage").forGetter($$0x -> $$0x.damage), Codec.BOOL.fieldOf("add").orElse(false).forGetter($$0x -> $$0x.add)
            )
         )
         .apply($$0, SetItemDamageFunction::new)
   );
   private final NumberProvider damage;
   private final boolean add;

   private SetItemDamageFunction(List<LootItemCondition> $$0, NumberProvider $$1, boolean $$2) {
      super($$0);
      this.damage = $$1;
      this.add = $$2;
   }

   @Override
   public LootItemFunctionType<SetItemDamageFunction> getType() {
      return LootItemFunctions.SET_DAMAGE;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.damage.getReferencedContextParams();
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if ($$0.isDamageableItem()) {
         int $$2 = $$0.getMaxDamage();
         float $$3 = this.add ? 1.0F - (float)$$0.getDamageValue() / $$2 : 0.0F;
         float $$4 = 1.0F - Mth.clamp(this.damage.getFloat($$1) + $$3, 0.0F, 1.0F);
         $$0.setDamageValue(Mth.floor($$4 * $$2));
      } else {
         LOGGER.warn("Couldn't set damage of loot item {}", $$0);
      }

      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider $$0) {
      return simpleBuilder($$1 -> new SetItemDamageFunction($$1, $$0, false));
   }

   public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider $$0, boolean $$1) {
      return simpleBuilder($$2 -> new SetItemDamageFunction($$2, $$0, $$1));
   }
}
