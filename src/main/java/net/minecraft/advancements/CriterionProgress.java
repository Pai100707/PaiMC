package net.minecraft.advancements;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import org.jspecify.annotations.Nullable;

public class CriterionProgress {
   @Nullable
   private Instant obtained;

   public CriterionProgress() {
   }

   public CriterionProgress(Instant $$0) {
      this.obtained = $$0;
   }

   public boolean isDone() {
      return this.obtained != null;
   }

   public void grant() {
      this.obtained = Instant.now();
   }

   public void revoke() {
      this.obtained = null;
   }

   @Nullable
   public Instant getObtained() {
      return this.obtained;
   }

   @Override
   public String toString() {
      return "CriterionProgress{obtained=" + (this.obtained == null ? "false" : this.obtained) + "}";
   }

   public void serializeToNetwork(FriendlyByteBuf $$0) {
      $$0.writeNullable(this.obtained, FriendlyByteBuf::writeInstant);
   }

   public static net.minecraft.advancements.CriterionProgress fromNetwork(FriendlyByteBuf $$0) {
      net.minecraft.advancements.CriterionProgress $$1 = new net.minecraft.advancements.CriterionProgress();
      $$1.obtained = (Instant)$$0.readNullable(FriendlyByteBuf::readInstant);
      return $$1;
   }
}
