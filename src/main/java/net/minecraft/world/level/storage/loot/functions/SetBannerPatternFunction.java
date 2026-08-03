package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetBannerPatternFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               BannerPatternLayers.CODEC.fieldOf("patterns").forGetter($$0x -> $$0x.patterns), Codec.BOOL.fieldOf("append").forGetter($$0x -> $$0x.append)
            )
         )
         .apply($$0, SetBannerPatternFunction::new)
   );
   private final BannerPatternLayers patterns;
   private final boolean append;

   SetBannerPatternFunction(List<LootItemCondition> $$0, BannerPatternLayers $$1, boolean $$2) {
      super($$0);
      this.patterns = $$1;
      this.append = $$2;
   }

   @Override
   protected ItemStack run(ItemStack $$0, LootContext $$1) {
      if (this.append) {
         $$0.update(
            DataComponents.BANNER_PATTERNS,
            BannerPatternLayers.EMPTY,
            this.patterns,
            ($$0x, $$1x) -> new BannerPatternLayers.Builder().addAll($$0x).addAll($$1x).build()
         );
      } else {
         $$0.set(DataComponents.BANNER_PATTERNS, this.patterns);
      }

      return $$0;
   }

   @Override
   public LootItemFunctionType<SetBannerPatternFunction> getType() {
      return LootItemFunctions.SET_BANNER_PATTERN;
   }

   public static SetBannerPatternFunction.Builder setBannerPattern(boolean $$0) {
      return new SetBannerPatternFunction.Builder($$0);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetBannerPatternFunction.Builder> {
      private final BannerPatternLayers.Builder patterns = new BannerPatternLayers.Builder();
      private final boolean append;

      Builder(boolean $$0) {
         this.append = $$0;
      }

      protected SetBannerPatternFunction.Builder getThis() {
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
      }

      public SetBannerPatternFunction.Builder addPattern(Holder<BannerPattern> $$0, DyeColor $$1) {
         this.patterns.add($$0, $$1);
         return this;
      }
   }
}
