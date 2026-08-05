package net.minecraft.world.damagesource;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class DamageSources {
   private final Registry<net.minecraft.world.damagesource.DamageType> damageTypes;
   private final net.minecraft.world.damagesource.DamageSource inFire;
   private final net.minecraft.world.damagesource.DamageSource campfire;
   private final net.minecraft.world.damagesource.DamageSource lightningBolt;
   private final net.minecraft.world.damagesource.DamageSource onFire;
   private final net.minecraft.world.damagesource.DamageSource lava;
   private final net.minecraft.world.damagesource.DamageSource hotFloor;
   private final net.minecraft.world.damagesource.DamageSource inWall;
   private final net.minecraft.world.damagesource.DamageSource cramming;
   private final net.minecraft.world.damagesource.DamageSource drown;
   private final net.minecraft.world.damagesource.DamageSource starve;
   private final net.minecraft.world.damagesource.DamageSource cactus;
   private final net.minecraft.world.damagesource.DamageSource fall;
   private final net.minecraft.world.damagesource.DamageSource enderPearl;
   private final net.minecraft.world.damagesource.DamageSource flyIntoWall;
   private final net.minecraft.world.damagesource.DamageSource fellOutOfWorld;
   private final net.minecraft.world.damagesource.DamageSource generic;
   private final net.minecraft.world.damagesource.DamageSource magic;
   private final net.minecraft.world.damagesource.DamageSource wither;
   private final net.minecraft.world.damagesource.DamageSource dragonBreath;
   private final net.minecraft.world.damagesource.DamageSource dryOut;
   private final net.minecraft.world.damagesource.DamageSource sweetBerryBush;
   private final net.minecraft.world.damagesource.DamageSource freeze;
   private final net.minecraft.world.damagesource.DamageSource stalagmite;
   private final net.minecraft.world.damagesource.DamageSource outsideBorder;
   private final net.minecraft.world.damagesource.DamageSource genericKill;

   public DamageSources(RegistryAccess $$0) {
      this.damageTypes = $$0.lookupOrThrow(Registries.DAMAGE_TYPE);
      this.inFire = this.source(net.minecraft.world.damagesource.DamageTypes.IN_FIRE);
      this.campfire = this.source(net.minecraft.world.damagesource.DamageTypes.CAMPFIRE);
      this.lightningBolt = this.source(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT);
      this.onFire = this.source(net.minecraft.world.damagesource.DamageTypes.ON_FIRE);
      this.lava = this.source(net.minecraft.world.damagesource.DamageTypes.LAVA);
      this.hotFloor = this.source(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR);
      this.inWall = this.source(net.minecraft.world.damagesource.DamageTypes.IN_WALL);
      this.cramming = this.source(net.minecraft.world.damagesource.DamageTypes.CRAMMING);
      this.drown = this.source(net.minecraft.world.damagesource.DamageTypes.DROWN);
      this.starve = this.source(net.minecraft.world.damagesource.DamageTypes.STARVE);
      this.cactus = this.source(net.minecraft.world.damagesource.DamageTypes.CACTUS);
      this.fall = this.source(net.minecraft.world.damagesource.DamageTypes.FALL);
      this.enderPearl = this.source(net.minecraft.world.damagesource.DamageTypes.ENDER_PEARL);
      this.flyIntoWall = this.source(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL);
      this.fellOutOfWorld = this.source(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD);
      this.generic = this.source(net.minecraft.world.damagesource.DamageTypes.GENERIC);
      this.magic = this.source(net.minecraft.world.damagesource.DamageTypes.MAGIC);
      this.wither = this.source(net.minecraft.world.damagesource.DamageTypes.WITHER);
      this.dragonBreath = this.source(net.minecraft.world.damagesource.DamageTypes.DRAGON_BREATH);
      this.dryOut = this.source(net.minecraft.world.damagesource.DamageTypes.DRY_OUT);
      this.sweetBerryBush = this.source(net.minecraft.world.damagesource.DamageTypes.SWEET_BERRY_BUSH);
      this.freeze = this.source(net.minecraft.world.damagesource.DamageTypes.FREEZE);
      this.stalagmite = this.source(net.minecraft.world.damagesource.DamageTypes.STALAGMITE);
      this.outsideBorder = this.source(net.minecraft.world.damagesource.DamageTypes.OUTSIDE_BORDER);
      this.genericKill = this.source(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL);
   }

   private net.minecraft.world.damagesource.DamageSource source(ResourceKey<net.minecraft.world.damagesource.DamageType> $$0) {
      return new net.minecraft.world.damagesource.DamageSource(this.damageTypes.getOrThrow($$0));
   }

   private net.minecraft.world.damagesource.DamageSource source(ResourceKey<net.minecraft.world.damagesource.DamageType> $$0, Entity $$1) {
      return new net.minecraft.world.damagesource.DamageSource(this.damageTypes.getOrThrow($$0), $$1);
   }

   private net.minecraft.world.damagesource.DamageSource source(
      ResourceKey<net.minecraft.world.damagesource.DamageType> $$0, Entity $$1, Entity $$2
   ) {
      return new net.minecraft.world.damagesource.DamageSource(this.damageTypes.getOrThrow($$0), $$1, $$2);
   }

   public net.minecraft.world.damagesource.DamageSource inFire() {
      return this.inFire;
   }

   public net.minecraft.world.damagesource.DamageSource campfire() {
      return this.campfire;
   }

   public net.minecraft.world.damagesource.DamageSource lightningBolt() {
      return this.lightningBolt;
   }

   public net.minecraft.world.damagesource.DamageSource onFire() {
      return this.onFire;
   }

   public net.minecraft.world.damagesource.DamageSource lava() {
      return this.lava;
   }

   public net.minecraft.world.damagesource.DamageSource hotFloor() {
      return this.hotFloor;
   }

   public net.minecraft.world.damagesource.DamageSource inWall() {
      return this.inWall;
   }

   public net.minecraft.world.damagesource.DamageSource cramming() {
      return this.cramming;
   }

   public net.minecraft.world.damagesource.DamageSource drown() {
      return this.drown;
   }

   public net.minecraft.world.damagesource.DamageSource starve() {
      return this.starve;
   }

   public net.minecraft.world.damagesource.DamageSource cactus() {
      return this.cactus;
   }

   public net.minecraft.world.damagesource.DamageSource fall() {
      return this.fall;
   }

   public net.minecraft.world.damagesource.DamageSource enderPearl() {
      return this.enderPearl;
   }

   public net.minecraft.world.damagesource.DamageSource flyIntoWall() {
      return this.flyIntoWall;
   }

   public net.minecraft.world.damagesource.DamageSource fellOutOfWorld() {
      return this.fellOutOfWorld;
   }

   public net.minecraft.world.damagesource.DamageSource generic() {
      return this.generic;
   }

   public net.minecraft.world.damagesource.DamageSource magic() {
      return this.magic;
   }

   public net.minecraft.world.damagesource.DamageSource wither() {
      return this.wither;
   }

   public net.minecraft.world.damagesource.DamageSource dragonBreath() {
      return this.dragonBreath;
   }

   public net.minecraft.world.damagesource.DamageSource dryOut() {
      return this.dryOut;
   }

   public net.minecraft.world.damagesource.DamageSource sweetBerryBush() {
      return this.sweetBerryBush;
   }

   public net.minecraft.world.damagesource.DamageSource freeze() {
      return this.freeze;
   }

   public net.minecraft.world.damagesource.DamageSource stalagmite() {
      return this.stalagmite;
   }

   public net.minecraft.world.damagesource.DamageSource fallingBlock(Entity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.FALLING_BLOCK, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource anvil(Entity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.FALLING_ANVIL, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource fallingStalactite(Entity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.FALLING_STALACTITE, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource sting(LivingEntity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.STING, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource mobAttack(LivingEntity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource noAggroMobAttack(LivingEntity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource playerAttack(Player $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource arrow(AbstractArrow $$0, Entity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.ARROW, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource trident(Entity $$0, Entity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.TRIDENT, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource mobProjectile(Entity $$0, LivingEntity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.MOB_PROJECTILE, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource spit(Entity $$0, LivingEntity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.SPIT, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource windCharge(Entity $$0, LivingEntity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.WIND_CHARGE, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource fireworks(FireworkRocketEntity $$0, Entity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.FIREWORKS, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource fireball(Fireball $$0, Entity $$1) {
      return $$1 == null
         ? this.source(net.minecraft.world.damagesource.DamageTypes.UNATTRIBUTED_FIREBALL, $$0)
         : this.source(net.minecraft.world.damagesource.DamageTypes.FIREBALL, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource witherSkull(WitherSkull $$0, Entity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.WITHER_SKULL, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource thrown(Entity $$0, Entity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.THROWN, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource indirectMagic(Entity $$0, Entity $$1) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC, $$0, $$1);
   }

   public net.minecraft.world.damagesource.DamageSource thorns(Entity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.THORNS, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource explosion(Explosion $$0) {
      return $$0 != null ? this.explosion($$0.getDirectSourceEntity(), $$0.getIndirectSourceEntity()) : this.explosion(null, null);
   }

   public net.minecraft.world.damagesource.DamageSource explosion(Entity $$0, Entity $$1) {
      return this.source(
         $$1 != null && $$0 != null ? net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION : net.minecraft.world.damagesource.DamageTypes.EXPLOSION,
         $$0,
         $$1
      );
   }

   public net.minecraft.world.damagesource.DamageSource sonicBoom(Entity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.SONIC_BOOM, $$0);
   }

   public net.minecraft.world.damagesource.DamageSource badRespawnPointExplosion(Vec3 $$0) {
      return new net.minecraft.world.damagesource.DamageSource(this.damageTypes.getOrThrow(net.minecraft.world.damagesource.DamageTypes.BAD_RESPAWN_POINT), $$0);
   }

   public net.minecraft.world.damagesource.DamageSource outOfBorder() {
      return this.outsideBorder;
   }

   public net.minecraft.world.damagesource.DamageSource genericKill() {
      return this.genericKill;
   }

   public net.minecraft.world.damagesource.DamageSource mace(Entity $$0) {
      return this.source(net.minecraft.world.damagesource.DamageTypes.MACE_SMASH, $$0);
   }
}
