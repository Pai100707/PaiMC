/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  com.mojang.datafixers.util.Either
 */
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
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.util.StringUtil;

public interface ProfileResolver {
    public Optional<GameProfile> fetchByName(String var1);

    public Optional<GameProfile> fetchById(UUID var1);

    default public Optional<GameProfile> fetchByNameOrId(Either<String, UUID> $$0) {
        return (Optional)$$0.map(this::fetchByName, this::fetchById);
    }

    public static class Cached
    implements ProfileResolver {
        private final LoadingCache<String, Optional<GameProfile>> profileCacheByName;
        final LoadingCache<UUID, Optional<GameProfile>> profileCacheById;

        public Cached(final MinecraftSessionService $$0, final UserNameToIdResolver $$1) {
            this.profileCacheById = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build((CacheLoader)new CacheLoader<UUID, Optional<GameProfile>>(this){

                public Optional<GameProfile> load(UUID $$02) {
                    ProfileResult $$1 = $$0.fetchProfile($$02, true);
                    return Optional.ofNullable($$1).map(ProfileResult::profile);
                }

                public /* synthetic */ Object load(Object object) throws Exception {
                    return this.load((UUID)object);
                }
            });
            this.profileCacheByName = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build((CacheLoader)new CacheLoader<String, Optional<GameProfile>>(){

                public Optional<GameProfile> load(String $$02) {
                    return $$1.get($$02).flatMap($$0 -> (Optional)profileCacheById.getUnchecked((Object)$$0.id()));
                }

                public /* synthetic */ Object load(Object object) throws Exception {
                    return this.load((String)object);
                }
            });
        }

        @Override
        public Optional<GameProfile> fetchByName(String $$0) {
            if (StringUtil.isValidPlayerName($$0)) {
                return (Optional)this.profileCacheByName.getUnchecked((Object)$$0);
            }
            return Optional.empty();
        }

        @Override
        public Optional<GameProfile> fetchById(UUID $$0) {
            return (Optional)this.profileCacheById.getUnchecked((Object)$$0);
        }
    }
}

