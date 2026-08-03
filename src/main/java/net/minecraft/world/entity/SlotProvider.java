package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.slot.SlotCollection;
import org.jspecify.annotations.Nullable;

public interface SlotProvider {
   @Nullable
   net.minecraft.world.entity.SlotAccess getSlot(int var1);

   default SlotCollection getSlotsFromRange(IntList $$0) {
      List<net.minecraft.world.entity.SlotAccess> $$1 = $$0.intStream().mapToObj(this::getSlot).filter(Objects::nonNull).toList();
      return SlotCollection.of($$1);
   }
}
