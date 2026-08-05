package net.minecraft.world.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class DamageSource {
   private final Holder<net.minecraft.world.damagesource.DamageType> type;
   
   private final Entity causingEntity;
   
   private final Entity directEntity;
   
   private final Vec3 damageSourcePosition;

   @Override
   public String toString() {
      return "DamageSource (" + this.type().msgId() + ")";
   }

   public float getFoodExhaustion() {
      return this.type().exhaustion();
   }

   public boolean isDirect() {
      return this.causingEntity == this.directEntity;
   }

   private DamageSource(Holder<net.minecraft.world.damagesource.DamageType> $$0, Entity $$1, Entity $$2, Vec3 $$3) {
      this.type = $$0;
      this.causingEntity = $$2;
      this.directEntity = $$1;
      this.damageSourcePosition = $$3;
   }

   public DamageSource(Holder<net.minecraft.world.damagesource.DamageType> $$0, Entity $$1, Entity $$2) {
      this($$0, $$1, $$2, null);
   }

   public DamageSource(Holder<net.minecraft.world.damagesource.DamageType> $$0, Vec3 $$1) {
      this($$0, null, null, $$1);
   }

   public DamageSource(Holder<net.minecraft.world.damagesource.DamageType> $$0, Entity $$1) {
      this($$0, $$1, $$1);
   }

   public DamageSource(Holder<net.minecraft.world.damagesource.DamageType> $$0) {
      this($$0, null, null, null);
   }

   
   public Entity getDirectEntity() {
      return this.directEntity;
   }

   
   public Entity getEntity() {
      return this.causingEntity;
   }

   
   public ItemStack getWeaponItem() {
      return this.directEntity != null ? this.directEntity.getWeaponItem() : null;
   }

   public Component getLocalizedDeathMessage(LivingEntity $$0) {
      String $$1 = "death.attack." + this.type().msgId();
      if (this.causingEntity == null && this.directEntity == null) {
         LivingEntity $$5 = $$0.getKillCredit();
         String $$6 = $$1 + ".player";
         return $$5 != null
            ? Component.translatable($$6, new Object[]{$$0.getDisplayName(), $$5.getDisplayName()})
            : Component.translatable($$1, new Object[]{$$0.getDisplayName()});
      } else {
         Component $$2 = this.causingEntity == null ? this.directEntity.getDisplayName() : this.causingEntity.getDisplayName();
         ItemStack $$4 = this.causingEntity instanceof LivingEntity $$3 ? $$3.getMainHandItem() : ItemStack.EMPTY;
         return !$$4.isEmpty() && $$4.has(DataComponents.CUSTOM_NAME)
            ? Component.translatable($$1 + ".item", new Object[]{$$0.getDisplayName(), $$2, $$4.getDisplayName()})
            : Component.translatable($$1, new Object[]{$$0.getDisplayName(), $$2});
      }
   }

   public String getMsgId() {
      return this.type().msgId();
   }

   public boolean scalesWithDifficulty() {
      return switch (this.type().scaling()) {
         case NEVER -> false;
         case WHEN_CAUSED_BY_LIVING_NON_PLAYER -> this.causingEntity instanceof LivingEntity && !(this.causingEntity instanceof Player);
         case ALWAYS -> true;
      };
   }

   public boolean isCreativePlayer() {
      return this.getEntity() instanceof Player $$0 && $$0.getAbilities().instabuild;
   }

   
   public Vec3 getSourcePosition() {
      if (this.damageSourcePosition != null) {
         return this.damageSourcePosition;
      } else {
         return this.directEntity != null ? this.directEntity.position() : null;
      }
   }

   
   public Vec3 sourcePositionRaw() {
      return this.damageSourcePosition;
   }

   public boolean is(TagKey<net.minecraft.world.damagesource.DamageType> $$0) {
      return this.type.is($$0);
   }

   public boolean is(ResourceKey<net.minecraft.world.damagesource.DamageType> $$0) {
      return this.type.is($$0);
   }

   public net.minecraft.world.damagesource.DamageType type() {
      return (net.minecraft.world.damagesource.DamageType)this.type.value();
   }

   public Holder<net.minecraft.world.damagesource.DamageType> typeHolder() {
      return this.type;
   }
}
