package net.minecraft.world.entity.boss.enderdragon.phases;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.slf4j.Logger;

public class EnderDragonPhaseManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final EnderDragon dragon;
   private final DragonPhaseInstance[] phases = new DragonPhaseInstance[EnderDragonPhase.getCount()];
   
   private DragonPhaseInstance currentPhase;

   public EnderDragonPhaseManager(EnderDragon $$0) {
      this.dragon = $$0;
      this.setPhase(EnderDragonPhase.HOVERING);
   }

   public void setPhase(EnderDragonPhase<?> $$0) {
      if (this.currentPhase == null || $$0 != this.currentPhase.getPhase()) {
         if (this.currentPhase != null) {
            this.currentPhase.end();
         }

         this.currentPhase = this.getPhase((EnderDragonPhase<DragonPhaseInstance>)$$0);
         if (!this.dragon.level().isClientSide()) {
            this.dragon.getEntityData().set(EnderDragon.DATA_PHASE, $$0.getId());
         }

         LOGGER.debug("Dragon is now in phase {} on the {}", $$0, this.dragon.level().isClientSide() ? "client" : "server");
         this.currentPhase.begin();
      }
   }

   public DragonPhaseInstance getCurrentPhase() {
      return Objects.requireNonNull(this.currentPhase);
   }

   public <T extends DragonPhaseInstance> T getPhase(EnderDragonPhase<T> $$0) {
      int $$1 = $$0.getId();
      DragonPhaseInstance $$2 = this.phases[$$1];
      if ($$2 == null) {
         $$2 = $$0.createInstance(this.dragon);
         this.phases[$$1] = $$2;
      }

      return (T)$$2;
   }
}
