package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.server.packs.repository.KnownPack;

public record RegistrationInfo(Optional<KnownPack> knownPackInfo, Lifecycle lifecycle) {
   public static final net.minecraft.core.RegistrationInfo BUILT_IN = new net.minecraft.core.RegistrationInfo(Optional.empty(), Lifecycle.stable());
}
