package net.minecraft.world.item.component;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin.Patch;

public abstract sealed class ResolvableProfile implements TooltipProvider permits ResolvableProfile.Static, ResolvableProfile.Dynamic {
   private static final Codec<ResolvableProfile> FULL_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.mapEither(ExtraCodecs.STORED_GAME_PROFILE, ResolvableProfile.Partial.MAP_CODEC).forGetter(ResolvableProfile::unpack),
            Patch.MAP_CODEC.forGetter(ResolvableProfile::skinPatch)
         )
         .apply($$0, ResolvableProfile::create)
   );
   public static final Codec<ResolvableProfile> CODEC = Codec.withAlternative(FULL_CODEC, ExtraCodecs.PLAYER_NAME, ResolvableProfile::createUnresolved);
   public static final StreamCodec<ByteBuf, ResolvableProfile> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.either(ByteBufCodecs.GAME_PROFILE, ResolvableProfile.Partial.STREAM_CODEC),
      ResolvableProfile::unpack,
      Patch.STREAM_CODEC,
      ResolvableProfile::skinPatch,
      ResolvableProfile::create
   );
   protected final GameProfile partialProfile;
   protected final Patch skinPatch;

   private static ResolvableProfile create(Either<GameProfile, ResolvableProfile.Partial> $$0, Patch $$1) {
      return (ResolvableProfile)$$0.map(
         $$1x -> new ResolvableProfile.Static(Either.left($$1x), $$1),
         $$1x -> (ResolvableProfile)($$1x.properties.isEmpty() && $$1x.id.isPresent() != $$1x.name.isPresent()
            ? $$1x.name
               .<ResolvableProfile>map($$1xx -> new ResolvableProfile.Dynamic(Either.left($$1xx), $$1))
               .orElseGet(() -> new ResolvableProfile.Dynamic(Either.right($$1x.id.get()), $$1))
            : new ResolvableProfile.Static(Either.right($$1x), $$1))
      );
   }

   public static ResolvableProfile createResolved(GameProfile $$0) {
      return new ResolvableProfile.Static(Either.left($$0), Patch.EMPTY);
   }

   public static ResolvableProfile createUnresolved(String $$0) {
      return new ResolvableProfile.Dynamic(Either.left($$0), Patch.EMPTY);
   }

   public static ResolvableProfile createUnresolved(UUID $$0) {
      return new ResolvableProfile.Dynamic(Either.right($$0), Patch.EMPTY);
   }

   protected abstract Either<GameProfile, ResolvableProfile.Partial> unpack();

   protected ResolvableProfile(GameProfile $$0, Patch $$1) {
      this.partialProfile = $$0;
      this.skinPatch = $$1;
   }

   public abstract CompletableFuture<GameProfile> resolveProfile(ProfileResolver var1);

   public GameProfile partialProfile() {
      return this.partialProfile;
   }

   public Patch skinPatch() {
      return this.skinPatch;
   }

   static GameProfile createPartialProfile(Optional<String> $$0, Optional<UUID> $$1, PropertyMap $$2) {
      String $$3 = $$0.orElse("");
      UUID $$4 = $$1.orElseGet(() -> $$0.map(UUIDUtil::createOfflinePlayerUUID).orElse(Util.NIL_UUID));
      return new GameProfile($$4, $$3, $$2);
   }

   public abstract Optional<String> name();

   public static final class Dynamic extends ResolvableProfile {
      private static final Component DYNAMIC_TOOLTIP = Component.translatable("component.profile.dynamic").withStyle(ChatFormatting.GRAY);
      private final Either<String, UUID> nameOrId;

      Dynamic(Either<String, UUID> $$0, Patch $$1) {
         super(ResolvableProfile.createPartialProfile($$0.left(), $$0.right(), PropertyMap.EMPTY), $$1);
         this.nameOrId = $$0;
      }

      @Override
      public Optional<String> name() {
         return this.nameOrId.left();
      }

      @Override
      public boolean equals(Object $$0) {
         return this == $$0 || $$0 instanceof ResolvableProfile.Dynamic $$1 && this.nameOrId.equals($$1.nameOrId) && this.skinPatch.equals($$1.skinPatch);
      }

      @Override
      public int hashCode() {
         int $$0 = 31 + this.nameOrId.hashCode();
         return 31 * $$0 + this.skinPatch.hashCode();
      }

      @Override
      protected Either<GameProfile, ResolvableProfile.Partial> unpack() {
         return Either.right(new ResolvableProfile.Partial(this.nameOrId.left(), this.nameOrId.right(), PropertyMap.EMPTY));
      }

      @Override
      public CompletableFuture<GameProfile> resolveProfile(ProfileResolver $$0) {
         return CompletableFuture.supplyAsync(() -> $$0.fetchByNameOrId(this.nameOrId).orElse(this.partialProfile), Util.nonCriticalIoPool());
      }

      @Override
      public void addToTooltip(
         net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
      ) {
         $$1.accept(DYNAMIC_TOOLTIP);
      }
   }

   protected record Partial(Optional<String> name, Optional<UUID> id, PropertyMap properties) {
      public static final ResolvableProfile.Partial EMPTY = new ResolvableProfile.Partial(Optional.empty(), Optional.empty(), PropertyMap.EMPTY);
      static final MapCodec<ResolvableProfile.Partial> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               ExtraCodecs.PLAYER_NAME.optionalFieldOf("name").forGetter(ResolvableProfile.Partial::name),
               UUIDUtil.CODEC.optionalFieldOf("id").forGetter(ResolvableProfile.Partial::id),
               ExtraCodecs.PROPERTY_MAP.optionalFieldOf("properties", PropertyMap.EMPTY).forGetter(ResolvableProfile.Partial::properties)
            )
            .apply($$0, ResolvableProfile.Partial::new)
      );
      public static final StreamCodec<ByteBuf, ResolvableProfile.Partial> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.PLAYER_NAME.apply(ByteBufCodecs::optional),
         ResolvableProfile.Partial::name,
         UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional),
         ResolvableProfile.Partial::id,
         ByteBufCodecs.GAME_PROFILE_PROPERTIES,
         ResolvableProfile.Partial::properties,
         ResolvableProfile.Partial::new
      );

      private GameProfile createProfile() {
         return ResolvableProfile.createPartialProfile(this.name, this.id, this.properties);
      }
   }

   public static final class Static extends ResolvableProfile {
      public static final ResolvableProfile.Static EMPTY = new ResolvableProfile.Static(Either.right(ResolvableProfile.Partial.EMPTY), Patch.EMPTY);
      private final Either<GameProfile, ResolvableProfile.Partial> contents;

      Static(Either<GameProfile, ResolvableProfile.Partial> $$0, Patch $$1) {
         super((GameProfile)$$0.map($$0x -> $$0x, ResolvableProfile.Partial::createProfile), $$1);
         this.contents = $$0;
      }

      @Override
      public CompletableFuture<GameProfile> resolveProfile(ProfileResolver $$0) {
         return CompletableFuture.completedFuture(this.partialProfile);
      }

      @Override
      protected Either<GameProfile, ResolvableProfile.Partial> unpack() {
         return this.contents;
      }

      @Override
      public Optional<String> name() {
         return (Optional<String>)this.contents.map($$0 -> Optional.of($$0.name()), $$0 -> $$0.name);
      }

      @Override
      public boolean equals(Object $$0) {
         return this == $$0 || $$0 instanceof ResolvableProfile.Static $$1 && this.contents.equals($$1.contents) && this.skinPatch.equals($$1.skinPatch);
      }

      @Override
      public int hashCode() {
         int $$0 = 31 + this.contents.hashCode();
         return 31 * $$0 + this.skinPatch.hashCode();
      }

      @Override
      public void addToTooltip(
         net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
      ) {
      }
   }
}
