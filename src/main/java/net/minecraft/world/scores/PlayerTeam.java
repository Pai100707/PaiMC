package net.minecraft.world.scores;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.ShowText;
import org.jspecify.annotations.Nullable;

public class PlayerTeam extends net.minecraft.world.scores.Team {
   private static final int BIT_FRIENDLY_FIRE = 0;
   private static final int BIT_SEE_INVISIBLES = 1;
   private final net.minecraft.world.scores.Scoreboard scoreboard;
   private final String name;
   private final Set<String> players = Sets.newHashSet();
   private Component displayName;
   private Component playerPrefix = CommonComponents.EMPTY;
   private Component playerSuffix = CommonComponents.EMPTY;
   private boolean allowFriendlyFire = true;
   private boolean seeFriendlyInvisibles = true;
   private net.minecraft.world.scores.Team.Visibility nameTagVisibility = net.minecraft.world.scores.Team.Visibility.ALWAYS;
   private net.minecraft.world.scores.Team.Visibility deathMessageVisibility = net.minecraft.world.scores.Team.Visibility.ALWAYS;
   private ChatFormatting color = ChatFormatting.RESET;
   private net.minecraft.world.scores.Team.CollisionRule collisionRule = net.minecraft.world.scores.Team.CollisionRule.ALWAYS;
   private final Style displayNameStyle;

   public PlayerTeam(net.minecraft.world.scores.Scoreboard $$0, String $$1) {
      this.scoreboard = $$0;
      this.name = $$1;
      this.displayName = Component.literal($$1);
      this.displayNameStyle = Style.EMPTY.withInsertion($$1).withHoverEvent(new ShowText(Component.literal($$1)));
   }

   public net.minecraft.world.scores.PlayerTeam.Packed pack() {
      return new net.minecraft.world.scores.PlayerTeam.Packed(
         this.name,
         Optional.of(this.displayName),
         this.color != ChatFormatting.RESET ? Optional.of(this.color) : Optional.empty(),
         this.allowFriendlyFire,
         this.seeFriendlyInvisibles,
         this.playerPrefix,
         this.playerSuffix,
         this.nameTagVisibility,
         this.deathMessageVisibility,
         this.collisionRule,
         List.copyOf(this.players)
      );
   }

   public net.minecraft.world.scores.Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   @Override
   public String getName() {
      return this.name;
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public MutableComponent getFormattedDisplayName() {
      MutableComponent $$0 = ComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle(this.displayNameStyle));
      ChatFormatting $$1 = this.getColor();
      if ($$1 != ChatFormatting.RESET) {
         $$0.withStyle($$1);
      }

      return $$0;
   }

   public void setDisplayName(Component $$0) {
      if ($$0 == null) {
         throw new IllegalArgumentException("Name cannot be null");
      } else {
         this.displayName = $$0;
         this.scoreboard.onTeamChanged(this);
      }
   }

   public void setPlayerPrefix(@Nullable Component $$0) {
      this.playerPrefix = $$0 == null ? CommonComponents.EMPTY : $$0;
      this.scoreboard.onTeamChanged(this);
   }

   public Component getPlayerPrefix() {
      return this.playerPrefix;
   }

   public void setPlayerSuffix(@Nullable Component $$0) {
      this.playerSuffix = $$0 == null ? CommonComponents.EMPTY : $$0;
      this.scoreboard.onTeamChanged(this);
   }

   public Component getPlayerSuffix() {
      return this.playerSuffix;
   }

   @Override
   public Collection<String> getPlayers() {
      return this.players;
   }

   @Override
   public MutableComponent getFormattedName(Component $$0) {
      MutableComponent $$1 = Component.empty().append(this.playerPrefix).append($$0).append(this.playerSuffix);
      ChatFormatting $$2 = this.getColor();
      if ($$2 != ChatFormatting.RESET) {
         $$1.withStyle($$2);
      }

      return $$1;
   }

   public static MutableComponent formatNameForTeam(@Nullable net.minecraft.world.scores.Team $$0, Component $$1) {
      return $$0 == null ? $$1.copy() : $$0.getFormattedName($$1);
   }

   @Override
   public boolean isAllowFriendlyFire() {
      return this.allowFriendlyFire;
   }

   public void setAllowFriendlyFire(boolean $$0) {
      this.allowFriendlyFire = $$0;
      this.scoreboard.onTeamChanged(this);
   }

   @Override
   public boolean canSeeFriendlyInvisibles() {
      return this.seeFriendlyInvisibles;
   }

   public void setSeeFriendlyInvisibles(boolean $$0) {
      this.seeFriendlyInvisibles = $$0;
      this.scoreboard.onTeamChanged(this);
   }

   @Override
   public net.minecraft.world.scores.Team.Visibility getNameTagVisibility() {
      return this.nameTagVisibility;
   }

   @Override
   public net.minecraft.world.scores.Team.Visibility getDeathMessageVisibility() {
      return this.deathMessageVisibility;
   }

   public void setNameTagVisibility(net.minecraft.world.scores.Team.Visibility $$0) {
      this.nameTagVisibility = $$0;
      this.scoreboard.onTeamChanged(this);
   }

   public void setDeathMessageVisibility(net.minecraft.world.scores.Team.Visibility $$0) {
      this.deathMessageVisibility = $$0;
      this.scoreboard.onTeamChanged(this);
   }

   @Override
   public net.minecraft.world.scores.Team.CollisionRule getCollisionRule() {
      return this.collisionRule;
   }

   public void setCollisionRule(net.minecraft.world.scores.Team.CollisionRule $$0) {
      this.collisionRule = $$0;
      this.scoreboard.onTeamChanged(this);
   }

   public int packOptions() {
      int $$0 = 0;
      if (this.isAllowFriendlyFire()) {
         $$0 |= 1;
      }

      if (this.canSeeFriendlyInvisibles()) {
         $$0 |= 2;
      }

      return $$0;
   }

   public void unpackOptions(int $$0) {
      this.setAllowFriendlyFire(($$0 & 1) > 0);
      this.setSeeFriendlyInvisibles(($$0 & 2) > 0);
   }

   public void setColor(ChatFormatting $$0) {
      this.color = $$0;
      this.scoreboard.onTeamChanged(this);
   }

   @Override
   public ChatFormatting getColor() {
      return this.color;
   }

   public record Packed(
      String name,
      Optional<Component> displayName,
      Optional<ChatFormatting> color,
      boolean allowFriendlyFire,
      boolean seeFriendlyInvisibles,
      Component memberNamePrefix,
      Component memberNameSuffix,
      net.minecraft.world.scores.Team.Visibility nameTagVisibility,
      net.minecraft.world.scores.Team.Visibility deathMessageVisibility,
      net.minecraft.world.scores.Team.CollisionRule collisionRule,
      List<String> players
   ) {
      public static final Codec<net.minecraft.world.scores.PlayerTeam.Packed> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.STRING.fieldOf("Name").forGetter(net.minecraft.world.scores.PlayerTeam.Packed::name),
               ComponentSerialization.CODEC.optionalFieldOf("DisplayName").forGetter(net.minecraft.world.scores.PlayerTeam.Packed::displayName),
               ChatFormatting.COLOR_CODEC.optionalFieldOf("TeamColor").forGetter(net.minecraft.world.scores.PlayerTeam.Packed::color),
               Codec.BOOL.optionalFieldOf("AllowFriendlyFire", true).forGetter(net.minecraft.world.scores.PlayerTeam.Packed::allowFriendlyFire),
               Codec.BOOL.optionalFieldOf("SeeFriendlyInvisibles", true).forGetter(net.minecraft.world.scores.PlayerTeam.Packed::seeFriendlyInvisibles),
               ComponentSerialization.CODEC
                  .optionalFieldOf("MemberNamePrefix", CommonComponents.EMPTY)
                  .forGetter(net.minecraft.world.scores.PlayerTeam.Packed::memberNamePrefix),
               ComponentSerialization.CODEC
                  .optionalFieldOf("MemberNameSuffix", CommonComponents.EMPTY)
                  .forGetter(net.minecraft.world.scores.PlayerTeam.Packed::memberNameSuffix),
               net.minecraft.world.scores.Team.Visibility.CODEC
                  .optionalFieldOf("NameTagVisibility", net.minecraft.world.scores.Team.Visibility.ALWAYS)
                  .forGetter(net.minecraft.world.scores.PlayerTeam.Packed::nameTagVisibility),
               net.minecraft.world.scores.Team.Visibility.CODEC
                  .optionalFieldOf("DeathMessageVisibility", net.minecraft.world.scores.Team.Visibility.ALWAYS)
                  .forGetter(net.minecraft.world.scores.PlayerTeam.Packed::deathMessageVisibility),
               net.minecraft.world.scores.Team.CollisionRule.CODEC
                  .optionalFieldOf("CollisionRule", net.minecraft.world.scores.Team.CollisionRule.ALWAYS)
                  .forGetter(net.minecraft.world.scores.PlayerTeam.Packed::collisionRule),
               Codec.STRING.listOf().optionalFieldOf("Players", List.of()).forGetter(net.minecraft.world.scores.PlayerTeam.Packed::players)
            )
            .apply($$0, net.minecraft.world.scores.PlayerTeam.Packed::new)
      );
   }
}
