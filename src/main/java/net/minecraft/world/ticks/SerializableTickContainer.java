package net.minecraft.world.ticks;

import java.util.List;

public interface SerializableTickContainer<T> {
   List<net.minecraft.world.ticks.SavedTick<T>> pack(long var1);
}
