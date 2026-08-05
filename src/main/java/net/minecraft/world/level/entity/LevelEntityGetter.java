package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.phys.AABB;

public interface LevelEntityGetter<T extends EntityAccess> {
   
   T get(int var1);

   
   T get(UUID var1);

   Iterable<T> getAll();

   <U extends T> void get(EntityTypeTest<T, U> var1, AbortableIterationConsumer<U> var2);

   void get(AABB var1, Consumer<T> var2);

   <U extends T> void get(EntityTypeTest<T, U> var1, AABB var2, AbortableIterationConsumer<U> var3);
}
