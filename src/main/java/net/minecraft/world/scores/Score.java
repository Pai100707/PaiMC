package net.minecraft.world.scores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;

public class Score implements net.minecraft.world.scores.ReadOnlyScoreInfo {
   private int value;
   private boolean locked = true;
   
   private Component display;
   
   private NumberFormat numberFormat;

   public Score() {
   }

   public Score(net.minecraft.world.scores.Score.Packed $$0) {
      this.value = $$0.value;
      this.locked = $$0.locked;
      this.display = $$0.display.orElse(null);
      this.numberFormat = $$0.numberFormat.orElse(null);
   }

   public net.minecraft.world.scores.Score.Packed pack() {
      return new net.minecraft.world.scores.Score.Packed(this.value, this.locked, Optional.ofNullable(this.display), Optional.ofNullable(this.numberFormat));
   }

   @Override
   public int value() {
      return this.value;
   }

   public void value(int $$0) {
      this.value = $$0;
   }

   @Override
   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean $$0) {
      this.locked = $$0;
   }

   
   public Component display() {
      return this.display;
   }

   public void display(Component $$0) {
      this.display = $$0;
   }

   
   @Override
   public NumberFormat numberFormat() {
      return this.numberFormat;
   }

   public void numberFormat(NumberFormat $$0) {
      this.numberFormat = $$0;
   }

   public record Packed(int value, boolean locked, Optional<Component> display, Optional<NumberFormat> numberFormat) {
      public static final MapCodec<net.minecraft.world.scores.Score.Packed> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.INT.optionalFieldOf("Score", 0).forGetter(net.minecraft.world.scores.Score.Packed::value),
               Codec.BOOL.optionalFieldOf("Locked", false).forGetter(net.minecraft.world.scores.Score.Packed::locked),
               ComponentSerialization.CODEC.optionalFieldOf("display").forGetter(net.minecraft.world.scores.Score.Packed::display),
               NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(net.minecraft.world.scores.Score.Packed::numberFormat)
            )
            .apply($$0, net.minecraft.world.scores.Score.Packed::new)
      );
   }
}
