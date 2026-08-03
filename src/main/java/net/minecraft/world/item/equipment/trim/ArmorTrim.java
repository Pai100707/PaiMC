package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.equipment.EquipmentAsset;

public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern) implements TooltipProvider {
   public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material), TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern)
         )
         .apply($$0, ArmorTrim::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(
      TrimMaterial.STREAM_CODEC, ArmorTrim::material, TrimPattern.STREAM_CODEC, ArmorTrim::pattern, ArmorTrim::new
   );
   private static final Component UPGRADE_TITLE = Component.translatable(
         Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.upgrade"))
      )
      .withStyle(ChatFormatting.GRAY);

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      $$1.accept(UPGRADE_TITLE);
      $$1.accept(CommonComponents.space().append(((TrimPattern)this.pattern.value()).copyWithStyle(this.material)));
      $$1.accept(CommonComponents.space().append(((TrimMaterial)this.material.value()).description()));
   }

   public Identifier layerAssetId(String $$0, ResourceKey<EquipmentAsset> $$1) {
      MaterialAssetGroup.AssetInfo $$2 = ((TrimMaterial)this.material().value()).assets().assetId($$1);
      return ((TrimPattern)this.pattern().value()).assetId().withPath($$2x -> $$0 + "/" + $$2x + "_" + $$2.suffix());
   }
}
