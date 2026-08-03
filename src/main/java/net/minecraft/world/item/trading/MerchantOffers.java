package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

public class MerchantOffers extends ArrayList<MerchantOffer> {
   public static final Codec<MerchantOffers> CODEC = MerchantOffer.CODEC
      .listOf()
      .optionalFieldOf("Recipes", List.of())
      .xmap(MerchantOffers::new, Function.identity())
      .codec();
   public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffers> STREAM_CODEC = MerchantOffer.STREAM_CODEC
      .apply(ByteBufCodecs.collection(MerchantOffers::new));

   public MerchantOffers() {
   }

   private MerchantOffers(int $$0) {
      super($$0);
   }

   private MerchantOffers(Collection<MerchantOffer> $$0) {
      super($$0);
   }

   @Nullable
   public MerchantOffer getRecipeFor(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1, int $$2) {
      if ($$2 > 0 && $$2 < this.size()) {
         MerchantOffer $$3 = this.get($$2);
         return $$3.satisfiedBy($$0, $$1) ? $$3 : null;
      } else {
         for (int $$4 = 0; $$4 < this.size(); $$4++) {
            MerchantOffer $$5 = this.get($$4);
            if ($$5.satisfiedBy($$0, $$1)) {
               return $$5;
            }
         }

         return null;
      }
   }

   public MerchantOffers copy() {
      MerchantOffers $$0 = new MerchantOffers(this.size());

      for (MerchantOffer $$1 : this) {
         $$0.add($$1.copy());
      }

      return $$0;
   }
}
