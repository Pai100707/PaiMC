package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ChangeItemDamage(LevelBasedValue amount) implements EnchantmentEntityEffect {
   public static final MapCodec<ChangeItemDamage> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter($$0x -> $$0x.amount)).apply($$0, ChangeItemDamage::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      net.minecraft.world.item.ItemStack $$5 = $$2.itemStack();
      if ($$5.has(DataComponents.MAX_DAMAGE) && $$5.has(DataComponents.DAMAGE)) {
         ServerPlayer $$7 = $$2.owner() instanceof ServerPlayer $$6 ? $$6 : null;
         int $$8 = (int)this.amount.calculate($$1);
         $$5.hurtAndBreak($$8, $$0, $$7, $$2.onBreak());
      }
   }

   @Override
   public MapCodec<ChangeItemDamage> codec() {
      return CODEC;
   }
}
