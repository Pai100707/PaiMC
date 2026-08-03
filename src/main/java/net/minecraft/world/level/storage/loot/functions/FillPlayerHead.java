package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FillPlayerHead extends LootItemConditionalFunction {
   public static final MapCodec<FillPlayerHead> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter($$0x -> $$0x.entityTarget)).apply($$0, FillPlayerHead::new)
   );
   private final LootContext.EntityTarget entityTarget;

   public FillPlayerHead(List<LootItemCondition> $$0, LootContext.EntityTarget $$1) {
      super($$0);
      this.entityTarget = $$1;
   }

   @Override
   public LootItemFunctionType<FillPlayerHead> getType() {
      return LootItemFunctions.FILL_PLAYER_HEAD;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(this.entityTarget.contextParam());
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if ($$0.is(Items.PLAYER_HEAD) && $$1.getOptionalParameter(this.entityTarget.contextParam()) instanceof Player $$2) {
         $$0.set(DataComponents.PROFILE, ResolvableProfile.createResolved($$2.getGameProfile()));
      }

      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> fillPlayerHead(LootContext.EntityTarget $$0) {
      return simpleBuilder($$1 -> new FillPlayerHead($$1, $$0));
   }
}
