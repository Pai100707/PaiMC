package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface OwnableEntity {
   @Nullable
   net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> getOwnerReference();

   Level level();

   @Nullable
   default net.minecraft.world.entity.LivingEntity getOwner() {
      return net.minecraft.world.entity.EntityReference.getLivingEntity(this.getOwnerReference(), this.level());
   }

   @Nullable
   default net.minecraft.world.entity.LivingEntity getRootOwner() {
      Set<Object> $$0 = new ObjectArraySet();
      net.minecraft.world.entity.LivingEntity $$1 = this.getOwner();
      $$0.add(this);

      while ($$1 instanceof net.minecraft.world.entity.OwnableEntity) {
         net.minecraft.world.entity.OwnableEntity $$2 = (net.minecraft.world.entity.OwnableEntity)$$1;
         net.minecraft.world.entity.LivingEntity $$3 = $$2.getOwner();
         if ($$0.contains($$3)) {
            return null;
         }

         $$0.add($$1);
         $$1 = $$2.getOwner();
      }

      return $$1;
   }
}
