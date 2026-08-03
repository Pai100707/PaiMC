package net.minecraft.world.entity.animal.parrot;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

public abstract class ShoulderRidingEntity extends net.minecraft.world.entity.TamableAnimal {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int RIDE_COOLDOWN = 100;
   private int rideCooldownCounter;

   protected ShoulderRidingEntity(net.minecraft.world.entity.EntityType<? extends ShoulderRidingEntity> $$0, Level $$1) {
      super($$0, $$1);
   }

   public boolean setEntityOnShoulder(ServerPlayer $$0) {
      ScopedCollector $$1 = new ScopedCollector(this.problemPath(), LOGGER);

      boolean var4;
      label27: {
         try {
            TagValueOutput $$2 = TagValueOutput.createWithContext($$1, this.registryAccess());
            this.saveWithoutId($$2);
            $$2.putString("id", this.getEncodeId());
            if ($$0.setEntityOnShoulder($$2.buildResult())) {
               this.discard();
               var4 = true;
               break label27;
            }
         } catch (Throwable var6) {
            try {
               $$1.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         $$1.close();
         return false;
      }

      $$1.close();
      return var4;
   }

   @Override
   public void tick() {
      this.rideCooldownCounter++;
      super.tick();
   }

   public boolean canSitOnShoulder() {
      return this.rideCooldownCounter > 100;
   }
}
