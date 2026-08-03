package net.minecraft.world.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.Level;

public record FoodProperties(int nutrition, float saturation, boolean canAlwaysEat) implements ConsumableListener {
   public static final Codec<net.minecraft.world.food.FoodProperties> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(net.minecraft.world.food.FoodProperties::nutrition),
            Codec.FLOAT.fieldOf("saturation").forGetter(net.minecraft.world.food.FoodProperties::saturation),
            Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(net.minecraft.world.food.FoodProperties::canAlwaysEat)
         )
         .apply($$0, net.minecraft.world.food.FoodProperties::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.food.FoodProperties> DIRECT_STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      net.minecraft.world.food.FoodProperties::nutrition,
      ByteBufCodecs.FLOAT,
      net.minecraft.world.food.FoodProperties::saturation,
      ByteBufCodecs.BOOL,
      net.minecraft.world.food.FoodProperties::canAlwaysEat,
      net.minecraft.world.food.FoodProperties::new
   );

   public void onConsume(Level $$0, LivingEntity $$1, ItemStack $$2, Consumable $$3) {
      RandomSource $$4 = $$1.getRandom();
      $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), (SoundEvent)$$3.sound().value(), SoundSource.NEUTRAL, 1.0F, $$4.triangle(1.0F, 0.4F));
      if ($$1 instanceof Player $$5) {
         $$5.getFoodData().eat(this);
         $$0.playSound(null, $$5.getX(), $$5.getY(), $$5.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, Mth.randomBetween($$4, 0.9F, 1.0F));
      }
   }

   public static class Builder {
      private int nutrition;
      private float saturationModifier;
      private boolean canAlwaysEat;

      public net.minecraft.world.food.FoodProperties.Builder nutrition(int $$0) {
         this.nutrition = $$0;
         return this;
      }

      public net.minecraft.world.food.FoodProperties.Builder saturationModifier(float $$0) {
         this.saturationModifier = $$0;
         return this;
      }

      public net.minecraft.world.food.FoodProperties.Builder alwaysEdible() {
         this.canAlwaysEat = true;
         return this;
      }

      public net.minecraft.world.food.FoodProperties build() {
         float $$0 = net.minecraft.world.food.FoodConstants.saturationByModifier(this.nutrition, this.saturationModifier);
         return new net.minecraft.world.food.FoodProperties(this.nutrition, $$0, this.canAlwaysEat);
      }
   }
}
