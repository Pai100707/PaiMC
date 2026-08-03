package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class VibrationSelector {
   public static final Codec<VibrationSelector> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            VibrationInfo.CODEC.lenientOptionalFieldOf("event").forGetter($$0x -> $$0x.currentVibrationData.map(Pair::getLeft)),
            Codec.LONG.fieldOf("tick").forGetter($$0x -> $$0x.currentVibrationData.<Long>map(Pair::getRight).orElse(-1L))
         )
         .apply($$0, VibrationSelector::new)
   );
   private Optional<Pair<VibrationInfo, Long>> currentVibrationData;

   public VibrationSelector(Optional<VibrationInfo> $$0, long $$1) {
      this.currentVibrationData = $$0.map($$1x -> Pair.of($$1x, $$1));
   }

   public VibrationSelector() {
      this.currentVibrationData = Optional.empty();
   }

   public void addCandidate(VibrationInfo $$0, long $$1) {
      if (this.shouldReplaceVibration($$0, $$1)) {
         this.currentVibrationData = Optional.of(Pair.of($$0, $$1));
      }
   }

   private boolean shouldReplaceVibration(VibrationInfo $$0, long $$1) {
      if (this.currentVibrationData.isEmpty()) {
         return true;
      } else {
         Pair<VibrationInfo, Long> $$2 = this.currentVibrationData.get();
         long $$3 = (Long)$$2.getRight();
         if ($$1 != $$3) {
            return false;
         } else {
            VibrationInfo $$4 = (VibrationInfo)$$2.getLeft();
            if ($$0.distance() < $$4.distance()) {
               return true;
            } else {
               return $$0.distance() > $$4.distance()
                  ? false
                  : VibrationSystem.getGameEventFrequency($$0.gameEvent()) > VibrationSystem.getGameEventFrequency($$4.gameEvent());
            }
         }
      }
   }

   public Optional<VibrationInfo> chosenCandidate(long $$0) {
      if (this.currentVibrationData.isEmpty()) {
         return Optional.empty();
      } else {
         return this.currentVibrationData.get().getRight() < $$0 ? Optional.of((VibrationInfo)this.currentVibrationData.get().getLeft()) : Optional.empty();
      }
   }

   public void startOver() {
      this.currentVibrationData = Optional.empty();
   }
}
