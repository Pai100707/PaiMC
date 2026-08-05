/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.food;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.Level;

public record FoodProperties(int nutrition, float saturation, boolean canAlwaysEat) implements ConsumableListener
{
    public static final Codec<FoodProperties> DIRECT_CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodProperties::nutrition), (App)Codec.FLOAT.fieldOf("saturation").forGetter(FoodProperties::saturation), (App)Codec.BOOL.optionalFieldOf("can_always_eat", (Object)false).forGetter(FoodProperties::canAlwaysEat)).apply((Applicative)$$0, FoodProperties::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodProperties> DIRECT_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, FoodProperties::nutrition, ByteBufCodecs.FLOAT, FoodProperties::saturation, ByteBufCodecs.BOOL, FoodProperties::canAlwaysEat, FoodProperties::new);

    @Override
    public void onConsume(Level $$0, LivingEntity $$1, ItemStack $$2, Consumable $$3) {
        RandomSource $$4 = $$1.getRandom();
        $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), $$3.sound().value(), SoundSource.NEUTRAL, 1.0f, $$4.triangle(1.0f, 0.4f));
        if ($$1 instanceof Player) {
            Player $$5 = (Player)$$1;
            $$5.getFoodData().eat(this);
            $$0.playSound(null, $$5.getX(), $$5.getY(), $$5.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, Mth.randomBetween($$4, 0.9f, 1.0f));
        }
    }

    public static class Builder {
        private int nutrition;
        private float saturationModifier;
        private boolean canAlwaysEat;

        public Builder nutrition(int $$0) {
            this.nutrition = $$0;
            return this;
        }

        public Builder saturationModifier(float $$0) {
            this.saturationModifier = $$0;
            return this;
        }

        public Builder alwaysEdible() {
            this.canAlwaysEat = true;
            return this;
        }

        public FoodProperties build() {
            float $$0 = FoodConstants.saturationByModifier(this.nutrition, this.saturationModifier);
            return new FoodProperties(this.nutrition, $$0, this.canAlwaysEat);
        }
    }
}

