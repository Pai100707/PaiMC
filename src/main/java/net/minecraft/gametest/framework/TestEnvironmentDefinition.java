package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;

public interface TestEnvironmentDefinition {
   Codec<TestEnvironmentDefinition> DIRECT_CODEC = BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE
      .byNameCodec()
      .dispatch(TestEnvironmentDefinition::codec, $$0 -> $$0);
   Codec<Holder<TestEnvironmentDefinition>> CODEC = RegistryFileCodec.create(Registries.TEST_ENVIRONMENT, DIRECT_CODEC);

   static MapCodec<? extends TestEnvironmentDefinition> bootstrap(Registry<MapCodec<? extends TestEnvironmentDefinition>> $$0) {
      Registry.register($$0, "all_of", TestEnvironmentDefinition.AllOf.CODEC);
      Registry.register($$0, "game_rules", TestEnvironmentDefinition.SetGameRules.CODEC);
      Registry.register($$0, "time_of_day", TestEnvironmentDefinition.TimeOfDay.CODEC);
      Registry.register($$0, "weather", TestEnvironmentDefinition.Weather.CODEC);
      return (MapCodec<? extends TestEnvironmentDefinition>)Registry.register($$0, "function", TestEnvironmentDefinition.Functions.CODEC);
   }

   void setup(ServerLevel var1);

   default void teardown(ServerLevel $$0) {
   }

   MapCodec<? extends TestEnvironmentDefinition> codec();

   public record AllOf(List<Holder<TestEnvironmentDefinition>> definitions) implements TestEnvironmentDefinition {
      public static final MapCodec<TestEnvironmentDefinition.AllOf> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(TestEnvironmentDefinition.CODEC.listOf().fieldOf("definitions").forGetter(TestEnvironmentDefinition.AllOf::definitions))
            .apply($$0, TestEnvironmentDefinition.AllOf::new)
      );

      public AllOf(TestEnvironmentDefinition... $$0) {
         this(Arrays.stream($$0).<Holder<TestEnvironmentDefinition>>map(Holder::direct).toList());
      }

      @Override
      public void setup(ServerLevel $$0) {
         this.definitions.forEach($$1 -> ((TestEnvironmentDefinition)$$1.value()).setup($$0));
      }

      @Override
      public void teardown(ServerLevel $$0) {
         this.definitions.forEach($$1 -> ((TestEnvironmentDefinition)$$1.value()).teardown($$0));
      }

      @Override
      public MapCodec<TestEnvironmentDefinition.AllOf> codec() {
         return CODEC;
      }
   }

   public record Functions(Optional<Identifier> setupFunction, Optional<Identifier> teardownFunction) implements TestEnvironmentDefinition {
      private static final Logger LOGGER = LogUtils.getLogger();
      public static final MapCodec<TestEnvironmentDefinition.Functions> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Identifier.CODEC.optionalFieldOf("setup").forGetter(TestEnvironmentDefinition.Functions::setupFunction),
               Identifier.CODEC.optionalFieldOf("teardown").forGetter(TestEnvironmentDefinition.Functions::teardownFunction)
            )
            .apply($$0, TestEnvironmentDefinition.Functions::new)
      );

      @Override
      public void setup(ServerLevel $$0) {
         this.setupFunction.ifPresent($$1 -> run($$0, $$1));
      }

      @Override
      public void teardown(ServerLevel $$0) {
         this.teardownFunction.ifPresent($$1 -> run($$0, $$1));
      }

      private static void run(ServerLevel $$0, Identifier $$1) {
         MinecraftServer $$2 = $$0.getServer();
         ServerFunctionManager $$3 = $$2.getFunctions();
         Optional<CommandFunction<CommandSourceStack>> $$4 = $$3.get($$1);
         if ($$4.isPresent()) {
            CommandSourceStack $$5 = $$2.createCommandSourceStack().withPermission(LevelBasedPermissionSet.GAMEMASTER).withSuppressedOutput().withLevel($$0);
            $$3.execute($$4.get(), $$5);
         } else {
            LOGGER.error("Test Batch failed for non-existent function {}", $$1);
         }
      }

      @Override
      public MapCodec<TestEnvironmentDefinition.Functions> codec() {
         return CODEC;
      }
   }

   public record SetGameRules(GameRuleMap gameRulesMap) implements TestEnvironmentDefinition {
      public static final MapCodec<TestEnvironmentDefinition.SetGameRules> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(GameRuleMap.CODEC.fieldOf("rules").forGetter(TestEnvironmentDefinition.SetGameRules::gameRulesMap))
            .apply($$0, TestEnvironmentDefinition.SetGameRules::new)
      );

      @Override
      public void setup(ServerLevel $$0) {
         GameRules $$1 = $$0.getGameRules();
         MinecraftServer $$2 = $$0.getServer();
         $$1.setAll(this.gameRulesMap, $$2);
      }

      @Override
      public void teardown(ServerLevel $$0) {
         this.gameRulesMap.keySet().forEach($$1 -> this.resetRule($$0, $$1));
      }

      private <T> void resetRule(ServerLevel $$0, GameRule<T> $$1) {
         $$0.getGameRules().set($$1, $$1.defaultValue(), $$0.getServer());
      }

      @Override
      public MapCodec<TestEnvironmentDefinition.SetGameRules> codec() {
         return CODEC;
      }
   }

   public record TimeOfDay(int time) implements TestEnvironmentDefinition {
      public static final MapCodec<TestEnvironmentDefinition.TimeOfDay> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("time").forGetter(TestEnvironmentDefinition.TimeOfDay::time))
            .apply($$0, TestEnvironmentDefinition.TimeOfDay::new)
      );

      @Override
      public void setup(ServerLevel $$0) {
         $$0.setDayTime(this.time);
      }

      @Override
      public MapCodec<TestEnvironmentDefinition.TimeOfDay> codec() {
         return CODEC;
      }
   }

   public record Weather(TestEnvironmentDefinition.Weather.Type weather) implements TestEnvironmentDefinition {
      public static final MapCodec<TestEnvironmentDefinition.Weather> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(TestEnvironmentDefinition.Weather.Type.CODEC.fieldOf("weather").forGetter(TestEnvironmentDefinition.Weather::weather))
            .apply($$0, TestEnvironmentDefinition.Weather::new)
      );

      @Override
      public void setup(ServerLevel $$0) {
         this.weather.apply($$0);
      }

      @Override
      public void teardown(ServerLevel $$0) {
         $$0.resetWeatherCycle();
      }

      @Override
      public MapCodec<TestEnvironmentDefinition.Weather> codec() {
         return CODEC;
      }

      public static enum Type implements StringRepresentable {
         CLEAR("clear", 100000, 0, false, false),
         RAIN("rain", 0, 100000, true, false),
         THUNDER("thunder", 0, 100000, true, true);

         public static final Codec<TestEnvironmentDefinition.Weather.Type> CODEC = StringRepresentable.fromEnum(TestEnvironmentDefinition.Weather.Type::values);
         private final String id;
         private final int clearTime;
         private final int rainTime;
         private final boolean raining;
         private final boolean thundering;

         private Type(final String $$0, final int $$1, final int $$2, final boolean $$3, final boolean $$4) {
            this.id = $$0;
            this.clearTime = $$1;
            this.rainTime = $$2;
            this.raining = $$3;
            this.thundering = $$4;
         }

         void apply(ServerLevel $$0) {
            $$0.setWeatherParameters(this.clearTime, this.rainTime, this.raining, this.thundering);
         }

         public String getSerializedName() {
            return this.id;
         }
      }
   }
}
