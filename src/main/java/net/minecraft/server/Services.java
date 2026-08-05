package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.util.SignatureValidator;

public record Services(
   MinecraftSessionService sessionService,
   ServicesKeySet servicesKeySet,
   GameProfileRepository profileRepository,
   UserNameToIdResolver nameToIdCache,
   ProfileResolver profileResolver
) {
   private static final String USERID_CACHE_FILE = "usercache.json";

   public static net.minecraft.server.Services create(YggdrasilAuthenticationService $$0, File $$1) {
      MinecraftSessionService $$2 = $$0.createMinecraftSessionService();
      GameProfileRepository $$3 = $$0.createProfileRepository();
      UserNameToIdResolver $$4 = new CachedUserNameToIdResolver($$3, new File($$1, "usercache.json"));
      ProfileResolver $$5 = new ProfileResolver.Cached($$2, $$4);
      return new net.minecraft.server.Services($$2, $$0.getServicesKeySet(), $$3, $$4, $$5);
   }

   
   public SignatureValidator profileKeySignatureValidator() {
      return SignatureValidator.from(this.servicesKeySet, ServicesKeyType.PROFILE_KEY);
   }

   public boolean canValidateProfileKeys() {
      return !this.servicesKeySet.keys(ServicesKeyType.PROFILE_KEY).isEmpty();
   }
}
