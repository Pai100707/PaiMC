package net.minecraft.server.players;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.datafixers.util.Either;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.StringUtil;

public interface ProfileResolver {
   Optional<GameProfile> fetchByName(String var1);

   Optional<GameProfile> fetchById(UUID var1);

   default Optional<GameProfile> fetchByNameOrId(Either<String, UUID> $$0) {
      return (Optional<GameProfile>)$$0.map(this::fetchByName, this::fetchById);
   }

   public static class Cached implements ProfileResolver {
      private final LoadingCache<String, Optional<GameProfile>> profileCacheByName;
      final LoadingCache<UUID, Optional<GameProfile>> profileCacheById;

      public Cached(final MinecraftSessionService $$0, final UserNameToIdResolver $$1) {
         this.profileCacheById = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10L))
            .maximumSize(256L)
            .build(new CacheLoader<UUID, Optional<GameProfile>>() {
               public Optional<GameProfile> load(UUID $$0x) {
                  ProfileResult $$1x = $$0.fetchProfile($$0, true);
                  return Optional.ofNullable($$1x).map(ProfileResult::profile);
               }
            });
         this.profileCacheByName = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10L))
            .maximumSize(256L)
            .build(new CacheLoader<String, Optional<GameProfile>>() {
               public Optional<GameProfile> load(String $$0) {
                  return $$1.get($$0).flatMap($$0x -> (Optional<? extends GameProfile>)Cached.this.profileCacheById.getUnchecked($$0x.id()));
               }
            });
      }

      @Override
      public Optional<GameProfile> fetchByName(String $$0) {
         return StringUtil.isValidPlayerName($$0) ? (Optional)this.profileCacheByName.getUnchecked($$0) : Optional.empty();
      }

      @Override
      public Optional<GameProfile> fetchById(UUID $$0) {
         return (Optional<GameProfile>)this.profileCacheById.getUnchecked($$0);
      }
   }
}
