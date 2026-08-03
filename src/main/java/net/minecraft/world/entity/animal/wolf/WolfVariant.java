package net.minecraft.world.entity.animal.wolf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

public record WolfVariant(WolfVariant.AssetInfo assetInfo, SpawnPrioritySelectors spawnConditions) implements PriorityProvider<SpawnContext, SpawnCondition> {
   public static final Codec<WolfVariant> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            WolfVariant.AssetInfo.CODEC.fieldOf("assets").forGetter(WolfVariant::assetInfo),
            SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions").forGetter(WolfVariant::spawnConditions)
         )
         .apply($$0, WolfVariant::new)
   );
   public static final Codec<WolfVariant> NETWORK_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(WolfVariant.AssetInfo.CODEC.fieldOf("assets").forGetter(WolfVariant::assetInfo)).apply($$0, WolfVariant::new)
   );
   public static final Codec<Holder<WolfVariant>> CODEC = RegistryFixedCodec.create(Registries.WOLF_VARIANT);
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WOLF_VARIANT);

   private WolfVariant(WolfVariant.AssetInfo $$0) {
      this($$0, SpawnPrioritySelectors.EMPTY);
   }

   @Override
   public List<PriorityProvider.Selector<SpawnContext, SpawnCondition>> selectors() {
      return this.spawnConditions.selectors();
   }

   public record AssetInfo(ResourceTexture wild, ResourceTexture tame, ResourceTexture angry) {
      public static final Codec<WolfVariant.AssetInfo> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               ResourceTexture.CODEC.fieldOf("wild").forGetter(WolfVariant.AssetInfo::wild),
               ResourceTexture.CODEC.fieldOf("tame").forGetter(WolfVariant.AssetInfo::tame),
               ResourceTexture.CODEC.fieldOf("angry").forGetter(WolfVariant.AssetInfo::angry)
            )
            .apply($$0, WolfVariant.AssetInfo::new)
      );
   }
}
