package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class LevelEntityGetterAdapter<T extends EntityAccess> implements LevelEntityGetter<T> {
   private final EntityLookup<T> visibleEntities;
   private final EntitySectionStorage<T> sectionStorage;

   public LevelEntityGetterAdapter(EntityLookup<T> $$0, EntitySectionStorage<T> $$1) {
      this.visibleEntities = $$0;
      this.sectionStorage = $$1;
   }

   @Nullable
   @Override
   public T get(int $$0) {
      return this.visibleEntities.getEntity($$0);
   }

   @Nullable
   @Override
   public T get(UUID $$0) {
      return this.visibleEntities.getEntity($$0);
   }

   @Override
   public Iterable<T> getAll() {
      return this.visibleEntities.getAllEntities();
   }

   @Override
   public <U extends T> void get(EntityTypeTest<T, U> $$0, AbortableIterationConsumer<U> $$1) {
      this.visibleEntities.getEntities($$0, $$1);
   }

   @Override
   public void get(AABB $$0, Consumer<T> $$1) {
      this.sectionStorage.getEntities($$0, AbortableIterationConsumer.forConsumer($$1));
   }

   @Override
   public <U extends T> void get(EntityTypeTest<T, U> $$0, AABB $$1, AbortableIterationConsumer<U> $$2) {
      this.sectionStorage.getEntities($$0, $$1, $$2);
   }
}
