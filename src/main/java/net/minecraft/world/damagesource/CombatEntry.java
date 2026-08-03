package net.minecraft.world.damagesource;

import org.jspecify.annotations.Nullable;

public record CombatEntry(
   net.minecraft.world.damagesource.DamageSource source, float damage, @Nullable net.minecraft.world.damagesource.FallLocation fallLocation, float fallDistance
) {
}
