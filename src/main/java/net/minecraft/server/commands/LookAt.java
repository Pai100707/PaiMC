package net.minecraft.server.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface LookAt {
   void perform(CommandSourceStack var1, Entity var2);

   public record LookAtEntity(Entity entity, Anchor anchor) implements LookAt {
      @Override
      public void perform(CommandSourceStack $$0, Entity $$1) {
         if ($$1 instanceof ServerPlayer $$2) {
            $$2.lookAt($$0.getAnchor(), this.entity, this.anchor);
         } else {
            $$1.lookAt($$0.getAnchor(), this.anchor.apply(this.entity));
         }
      }
   }

   public record LookAtPosition(Vec3 position) implements LookAt {
      @Override
      public void perform(CommandSourceStack $$0, Entity $$1) {
         $$1.lookAt($$0.getAnchor(), this.position);
      }
   }
}
