package net.minecraft.world.damagesource;


public record CombatEntry(
   net.minecraft.world.damagesource.DamageSource source, float damage, net.minecraft.world.damagesource.FallLocation fallLocation, float fallDistance
) {
}
