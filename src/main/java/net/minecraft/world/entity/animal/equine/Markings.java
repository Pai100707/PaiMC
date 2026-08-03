package net.minecraft.world.entity.animal.equine;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum Markings {
   NONE(0),
   WHITE(1),
   WHITE_FIELD(2),
   WHITE_DOTS(3),
   BLACK_DOTS(4);

   private static final IntFunction<Markings> BY_ID = ByIdMap.continuous(Markings::getId, values(), OutOfBoundsStrategy.WRAP);
   private final int id;

   private Markings(final int $$0) {
      this.id = $$0;
   }

   public int getId() {
      return this.id;
   }

   public static Markings byId(int $$0) {
      return BY_ID.apply($$0);
   }
}
