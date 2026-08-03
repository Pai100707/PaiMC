package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public class MerchantOffer {
   public static final Codec<MerchantOffer> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ItemCost.CODEC.fieldOf("buy").forGetter($$0x -> $$0x.baseCostA),
            ItemCost.CODEC.lenientOptionalFieldOf("buyB").forGetter($$0x -> $$0x.costB),
            net.minecraft.world.item.ItemStack.CODEC.fieldOf("sell").forGetter($$0x -> $$0x.result),
            Codec.INT.lenientOptionalFieldOf("uses", 0).forGetter($$0x -> $$0x.uses),
            Codec.INT.lenientOptionalFieldOf("maxUses", 4).forGetter($$0x -> $$0x.maxUses),
            Codec.BOOL.lenientOptionalFieldOf("rewardExp", true).forGetter($$0x -> $$0x.rewardExp),
            Codec.INT.lenientOptionalFieldOf("specialPrice", 0).forGetter($$0x -> $$0x.specialPriceDiff),
            Codec.INT.lenientOptionalFieldOf("demand", 0).forGetter($$0x -> $$0x.demand),
            Codec.FLOAT.lenientOptionalFieldOf("priceMultiplier", 0.0F).forGetter($$0x -> $$0x.priceMultiplier),
            Codec.INT.lenientOptionalFieldOf("xp", 1).forGetter($$0x -> $$0x.xp)
         )
         .apply($$0, MerchantOffer::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffer> STREAM_CODEC = StreamCodec.of(
      MerchantOffer::writeToStream, MerchantOffer::createFromStream
   );
   private final ItemCost baseCostA;
   private final Optional<ItemCost> costB;
   private final net.minecraft.world.item.ItemStack result;
   private int uses;
   private final int maxUses;
   private final boolean rewardExp;
   private int specialPriceDiff;
   private int demand;
   private final float priceMultiplier;
   private final int xp;

   private MerchantOffer(
      ItemCost $$0, Optional<ItemCost> $$1, net.minecraft.world.item.ItemStack $$2, int $$3, int $$4, boolean $$5, int $$6, int $$7, float $$8, int $$9
   ) {
      this.baseCostA = $$0;
      this.costB = $$1;
      this.result = $$2;
      this.uses = $$3;
      this.maxUses = $$4;
      this.rewardExp = $$5;
      this.specialPriceDiff = $$6;
      this.demand = $$7;
      this.priceMultiplier = $$8;
      this.xp = $$9;
   }

   public MerchantOffer(ItemCost $$0, net.minecraft.world.item.ItemStack $$1, int $$2, int $$3, float $$4) {
      this($$0, Optional.empty(), $$1, $$2, $$3, $$4);
   }

   public MerchantOffer(ItemCost $$0, Optional<ItemCost> $$1, net.minecraft.world.item.ItemStack $$2, int $$3, int $$4, float $$5) {
      this($$0, $$1, $$2, 0, $$3, $$4, $$5);
   }

   public MerchantOffer(ItemCost $$0, Optional<ItemCost> $$1, net.minecraft.world.item.ItemStack $$2, int $$3, int $$4, int $$5, float $$6) {
      this($$0, $$1, $$2, $$3, $$4, $$5, $$6, 0);
   }

   public MerchantOffer(ItemCost $$0, Optional<ItemCost> $$1, net.minecraft.world.item.ItemStack $$2, int $$3, int $$4, int $$5, float $$6, int $$7) {
      this($$0, $$1, $$2, $$3, $$4, true, 0, $$7, $$6, $$5);
   }

   private MerchantOffer(MerchantOffer $$0) {
      this($$0.baseCostA, $$0.costB, $$0.result.copy(), $$0.uses, $$0.maxUses, $$0.rewardExp, $$0.specialPriceDiff, $$0.demand, $$0.priceMultiplier, $$0.xp);
   }

   public net.minecraft.world.item.ItemStack getBaseCostA() {
      return this.baseCostA.itemStack();
   }

   public net.minecraft.world.item.ItemStack getCostA() {
      return this.baseCostA.itemStack().copyWithCount(this.getModifiedCostCount(this.baseCostA));
   }

   private int getModifiedCostCount(ItemCost $$0) {
      int $$1 = $$0.count();
      int $$2 = Math.max(0, Mth.floor($$1 * this.demand * this.priceMultiplier));
      return Mth.clamp($$1 + $$2 + this.specialPriceDiff, 1, $$0.itemStack().getMaxStackSize());
   }

   public net.minecraft.world.item.ItemStack getCostB() {
      return this.costB.map(ItemCost::itemStack).orElse(net.minecraft.world.item.ItemStack.EMPTY);
   }

   public ItemCost getItemCostA() {
      return this.baseCostA;
   }

   public Optional<ItemCost> getItemCostB() {
      return this.costB;
   }

   public net.minecraft.world.item.ItemStack getResult() {
      return this.result;
   }

   public void updateDemand() {
      this.demand = this.demand + this.uses - (this.maxUses - this.uses);
   }

   public net.minecraft.world.item.ItemStack assemble() {
      return this.result.copy();
   }

   public int getUses() {
      return this.uses;
   }

   public void resetUses() {
      this.uses = 0;
   }

   public int getMaxUses() {
      return this.maxUses;
   }

   public void increaseUses() {
      this.uses++;
   }

   public int getDemand() {
      return this.demand;
   }

   public void addToSpecialPriceDiff(int $$0) {
      this.specialPriceDiff += $$0;
   }

   public void resetSpecialPriceDiff() {
      this.specialPriceDiff = 0;
   }

   public int getSpecialPriceDiff() {
      return this.specialPriceDiff;
   }

   public void setSpecialPriceDiff(int $$0) {
      this.specialPriceDiff = $$0;
   }

   public float getPriceMultiplier() {
      return this.priceMultiplier;
   }

   public int getXp() {
      return this.xp;
   }

   public boolean isOutOfStock() {
      return this.uses >= this.maxUses;
   }

   public void setToOutOfStock() {
      this.uses = this.maxUses;
   }

   public boolean needsRestock() {
      return this.uses > 0;
   }

   public boolean shouldRewardExp() {
      return this.rewardExp;
   }

   public boolean satisfiedBy(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      if (!this.baseCostA.test($$0) || $$0.getCount() < this.getModifiedCostCount(this.baseCostA)) {
         return false;
      } else {
         return !this.costB.isPresent() ? $$1.isEmpty() : this.costB.get().test($$1) && $$1.getCount() >= this.costB.get().count();
      }
   }

   public boolean take(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      if (!this.satisfiedBy($$0, $$1)) {
         return false;
      } else {
         $$0.shrink(this.getCostA().getCount());
         if (!this.getCostB().isEmpty()) {
            $$1.shrink(this.getCostB().getCount());
         }

         return true;
      }
   }

   public MerchantOffer copy() {
      return new MerchantOffer(this);
   }

   private static void writeToStream(RegistryFriendlyByteBuf $$0, MerchantOffer $$1) {
      ItemCost.STREAM_CODEC.encode($$0, $$1.getItemCostA());
      net.minecraft.world.item.ItemStack.STREAM_CODEC.encode($$0, $$1.getResult());
      ItemCost.OPTIONAL_STREAM_CODEC.encode($$0, $$1.getItemCostB());
      $$0.writeBoolean($$1.isOutOfStock());
      $$0.writeInt($$1.getUses());
      $$0.writeInt($$1.getMaxUses());
      $$0.writeInt($$1.getXp());
      $$0.writeInt($$1.getSpecialPriceDiff());
      $$0.writeFloat($$1.getPriceMultiplier());
      $$0.writeInt($$1.getDemand());
   }

   public static MerchantOffer createFromStream(RegistryFriendlyByteBuf $$0) {
      ItemCost $$1 = (ItemCost)ItemCost.STREAM_CODEC.decode($$0);
      net.minecraft.world.item.ItemStack $$2 = (net.minecraft.world.item.ItemStack)net.minecraft.world.item.ItemStack.STREAM_CODEC.decode($$0);
      Optional<ItemCost> $$3 = (Optional<ItemCost>)ItemCost.OPTIONAL_STREAM_CODEC.decode($$0);
      boolean $$4 = $$0.readBoolean();
      int $$5 = $$0.readInt();
      int $$6 = $$0.readInt();
      int $$7 = $$0.readInt();
      int $$8 = $$0.readInt();
      float $$9 = $$0.readFloat();
      int $$10 = $$0.readInt();
      MerchantOffer $$11 = new MerchantOffer($$1, $$3, $$2, $$5, $$6, $$7, $$9, $$10);
      if ($$4) {
         $$11.setToOutOfStock();
      }

      $$11.setSpecialPriceDiff($$8);
      return $$11;
   }
}
