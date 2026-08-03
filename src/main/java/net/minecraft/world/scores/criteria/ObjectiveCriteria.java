package net.minecraft.world.scores.criteria;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.StringRepresentable.EnumCodec;

public class ObjectiveCriteria {
   private static final Map<String, ObjectiveCriteria> CUSTOM_CRITERIA = Maps.newHashMap();
   private static final Map<String, ObjectiveCriteria> CRITERIA_CACHE = Maps.newHashMap();
   public static final Codec<ObjectiveCriteria> CODEC = Codec.STRING
      .comapFlatMap(
         $$0 -> byName($$0).<DataResult>map(DataResult::success).orElse(DataResult.error(() -> "No scoreboard criteria with name: " + $$0)),
         ObjectiveCriteria::getName
      );
   public static final ObjectiveCriteria DUMMY = registerCustom("dummy");
   public static final ObjectiveCriteria TRIGGER = registerCustom("trigger");
   public static final ObjectiveCriteria DEATH_COUNT = registerCustom("deathCount");
   public static final ObjectiveCriteria KILL_COUNT_PLAYERS = registerCustom("playerKillCount");
   public static final ObjectiveCriteria KILL_COUNT_ALL = registerCustom("totalKillCount");
   public static final ObjectiveCriteria HEALTH = registerCustom("health", true, ObjectiveCriteria.RenderType.HEARTS);
   public static final ObjectiveCriteria FOOD = registerCustom("food", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria AIR = registerCustom("air", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria ARMOR = registerCustom("armor", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria EXPERIENCE = registerCustom("xp", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria LEVEL = registerCustom("level", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria[] TEAM_KILL = new ObjectiveCriteria[]{
      registerCustom("teamkill." + ChatFormatting.BLACK.getName()),
      registerCustom("teamkill." + ChatFormatting.DARK_BLUE.getName()),
      registerCustom("teamkill." + ChatFormatting.DARK_GREEN.getName()),
      registerCustom("teamkill." + ChatFormatting.DARK_AQUA.getName()),
      registerCustom("teamkill." + ChatFormatting.DARK_RED.getName()),
      registerCustom("teamkill." + ChatFormatting.DARK_PURPLE.getName()),
      registerCustom("teamkill." + ChatFormatting.GOLD.getName()),
      registerCustom("teamkill." + ChatFormatting.GRAY.getName()),
      registerCustom("teamkill." + ChatFormatting.DARK_GRAY.getName()),
      registerCustom("teamkill." + ChatFormatting.BLUE.getName()),
      registerCustom("teamkill." + ChatFormatting.GREEN.getName()),
      registerCustom("teamkill." + ChatFormatting.AQUA.getName()),
      registerCustom("teamkill." + ChatFormatting.RED.getName()),
      registerCustom("teamkill." + ChatFormatting.LIGHT_PURPLE.getName()),
      registerCustom("teamkill." + ChatFormatting.YELLOW.getName()),
      registerCustom("teamkill." + ChatFormatting.WHITE.getName())
   };
   public static final ObjectiveCriteria[] KILLED_BY_TEAM = new ObjectiveCriteria[]{
      registerCustom("killedByTeam." + ChatFormatting.BLACK.getName()),
      registerCustom("killedByTeam." + ChatFormatting.DARK_BLUE.getName()),
      registerCustom("killedByTeam." + ChatFormatting.DARK_GREEN.getName()),
      registerCustom("killedByTeam." + ChatFormatting.DARK_AQUA.getName()),
      registerCustom("killedByTeam." + ChatFormatting.DARK_RED.getName()),
      registerCustom("killedByTeam." + ChatFormatting.DARK_PURPLE.getName()),
      registerCustom("killedByTeam." + ChatFormatting.GOLD.getName()),
      registerCustom("killedByTeam." + ChatFormatting.GRAY.getName()),
      registerCustom("killedByTeam." + ChatFormatting.DARK_GRAY.getName()),
      registerCustom("killedByTeam." + ChatFormatting.BLUE.getName()),
      registerCustom("killedByTeam." + ChatFormatting.GREEN.getName()),
      registerCustom("killedByTeam." + ChatFormatting.AQUA.getName()),
      registerCustom("killedByTeam." + ChatFormatting.RED.getName()),
      registerCustom("killedByTeam." + ChatFormatting.LIGHT_PURPLE.getName()),
      registerCustom("killedByTeam." + ChatFormatting.YELLOW.getName()),
      registerCustom("killedByTeam." + ChatFormatting.WHITE.getName())
   };
   private final String name;
   private final boolean readOnly;
   private final ObjectiveCriteria.RenderType renderType;

   private static ObjectiveCriteria registerCustom(String $$0, boolean $$1, ObjectiveCriteria.RenderType $$2) {
      ObjectiveCriteria $$3 = new ObjectiveCriteria($$0, $$1, $$2);
      CUSTOM_CRITERIA.put($$0, $$3);
      return $$3;
   }

   private static ObjectiveCriteria registerCustom(String $$0) {
      return registerCustom($$0, false, ObjectiveCriteria.RenderType.INTEGER);
   }

   protected ObjectiveCriteria(String $$0) {
      this($$0, false, ObjectiveCriteria.RenderType.INTEGER);
   }

   protected ObjectiveCriteria(String $$0, boolean $$1, ObjectiveCriteria.RenderType $$2) {
      this.name = $$0;
      this.readOnly = $$1;
      this.renderType = $$2;
      CRITERIA_CACHE.put($$0, this);
   }

   public static Set<String> getCustomCriteriaNames() {
      return ImmutableSet.copyOf(CUSTOM_CRITERIA.keySet());
   }

   public static Optional<ObjectiveCriteria> byName(String $$0) {
      ObjectiveCriteria $$1 = CRITERIA_CACHE.get($$0);
      if ($$1 != null) {
         return Optional.of($$1);
      } else {
         int $$2 = $$0.indexOf(58);
         return $$2 < 0
            ? Optional.empty()
            : BuiltInRegistries.STAT_TYPE
               .getOptional(Identifier.bySeparator($$0.substring(0, $$2), '.'))
               .flatMap($$2x -> getStat($$2x, Identifier.bySeparator($$0.substring($$2 + 1), '.')));
      }
   }

   private static <T> Optional<ObjectiveCriteria> getStat(StatType<T> $$0, Identifier $$1) {
      return $$0.getRegistry().getOptional($$1).map($$0::get);
   }

   public String getName() {
      return this.name;
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }

   public ObjectiveCriteria.RenderType getDefaultRenderType() {
      return this.renderType;
   }

   public static enum RenderType implements StringRepresentable {
      INTEGER("integer"),
      HEARTS("hearts");

      private final String id;
      public static final EnumCodec<ObjectiveCriteria.RenderType> CODEC = StringRepresentable.fromEnum(ObjectiveCriteria.RenderType::values);

      private RenderType(final String $$0) {
         this.id = $$0;
      }

      public String getId() {
         return this.id;
      }

      public String getSerializedName() {
         return this.id;
      }

      public static ObjectiveCriteria.RenderType byId(String $$0) {
         return (ObjectiveCriteria.RenderType)CODEC.byName($$0, INTEGER);
      }
   }
}
