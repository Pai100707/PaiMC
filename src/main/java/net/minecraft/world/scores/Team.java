package net.minecraft.world.scores;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import org.jspecify.annotations.Nullable;

public abstract class Team {
   public boolean isAlliedTo(@Nullable net.minecraft.world.scores.Team $$0) {
      return $$0 == null ? false : this == $$0;
   }

   public abstract String getName();

   public abstract MutableComponent getFormattedName(Component var1);

   public abstract boolean canSeeFriendlyInvisibles();

   public abstract boolean isAllowFriendlyFire();

   public abstract net.minecraft.world.scores.Team.Visibility getNameTagVisibility();

   public abstract ChatFormatting getColor();

   public abstract Collection<String> getPlayers();

   public abstract net.minecraft.world.scores.Team.Visibility getDeathMessageVisibility();

   public abstract net.minecraft.world.scores.Team.CollisionRule getCollisionRule();

   public static enum CollisionRule implements StringRepresentable {
      ALWAYS("always", 0),
      NEVER("never", 1),
      PUSH_OTHER_TEAMS("pushOtherTeams", 2),
      PUSH_OWN_TEAM("pushOwnTeam", 3);

      public static final Codec<net.minecraft.world.scores.Team.CollisionRule> CODEC = StringRepresentable.fromEnum(
         net.minecraft.world.scores.Team.CollisionRule::values
      );
      private static final IntFunction<net.minecraft.world.scores.Team.CollisionRule> BY_ID = ByIdMap.continuous(
         $$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO
      );
      public static final StreamCodec<ByteBuf, net.minecraft.world.scores.Team.CollisionRule> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
      public final String name;
      public final int id;

      private CollisionRule(final String $$0, final int $$1) {
         this.name = $$0;
         this.id = $$1;
      }

      public Component getDisplayName() {
         return Component.translatable("team.collision." + this.name);
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   public static enum Visibility implements StringRepresentable {
      ALWAYS("always", 0),
      NEVER("never", 1),
      HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
      HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

      public static final Codec<net.minecraft.world.scores.Team.Visibility> CODEC = StringRepresentable.fromEnum(
         net.minecraft.world.scores.Team.Visibility::values
      );
      private static final IntFunction<net.minecraft.world.scores.Team.Visibility> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
      public static final StreamCodec<ByteBuf, net.minecraft.world.scores.Team.Visibility> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
      public final String name;
      public final int id;

      private Visibility(final String $$0, final int $$1) {
         this.name = $$0;
         this.id = $$1;
      }

      public Component getDisplayName() {
         return Component.translatable("team.visibility." + this.name);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
