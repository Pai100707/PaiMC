/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
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
    public static final Codec<TestEnvironmentDefinition> DIRECT_CODEC = BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE.byNameCodec().dispatch(TestEnvironmentDefinition::codec, $$0 -> $$0);
    public static final Codec<Holder<TestEnvironmentDefinition>> CODEC = RegistryFileCodec.create(Registries.TEST_ENVIRONMENT, DIRECT_CODEC);

    public static MapCodec<? extends TestEnvironmentDefinition> bootstrap(Registry<MapCodec<? extends TestEnvironmentDefinition>> $$0) {
        Registry.register($$0, "all_of", AllOf.CODEC);
        Registry.register($$0, "game_rules", SetGameRules.CODEC);
        Registry.register($$0, "time_of_day", TimeOfDay.CODEC);
        Registry.register($$0, "weather", Weather.CODEC);
        return Registry.register($$0, "function", Functions.CODEC);
    }

    public void setup(ServerLevel var1);

    default public void teardown(ServerLevel $$0) {
    }

    public MapCodec<? extends TestEnvironmentDefinition> codec();

    public record AllOf(List<Holder<TestEnvironmentDefinition>> definitions) implements TestEnvironmentDefinition
    {
        public static final MapCodec<AllOf> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)CODEC.listOf().fieldOf("definitions").forGetter(AllOf::definitions)).apply((Applicative)$$0, AllOf::new));

        public AllOf(TestEnvironmentDefinition ... $$0) {
            this(Arrays.stream($$0).map(Holder::direct).toList());
        }

        @Override
        public void setup(ServerLevel $$0) {
            this.definitions.forEach($$1 -> ((TestEnvironmentDefinition)$$1.value()).setup($$0));
        }

        @Override
        public void teardown(ServerLevel $$0) {
            this.definitions.forEach($$1 -> ((TestEnvironmentDefinition)$$1.value()).teardown($$0));
        }

        public MapCodec<AllOf> codec() {
            return CODEC;
        }
    }

    public record SetGameRules(GameRuleMap gameRulesMap) implements TestEnvironmentDefinition
    {
        public static final MapCodec<SetGameRules> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)GameRuleMap.CODEC.fieldOf("rules").forGetter(SetGameRules::gameRulesMap)).apply((Applicative)$$0, SetGameRules::new));

        @Override
        public void setup(ServerLevel $$0) {
            GameRules $$1 = $$0.getGameRules();
            MinecraftServer $$2 = $$0.getServer();
            $$1.setAll(this.gameRulesMap, $$2);
        }

        @Override
        public void teardown(ServerLevel $$0) {
            this.gameRulesMap.keySet().forEach($$1 -> this.resetRule($$0, (GameRule)$$1));
        }

        private <T> void resetRule(ServerLevel $$0, GameRule<T> $$1) {
            $$0.getGameRules().set($$1, $$1.defaultValue(), $$0.getServer());
        }

        public MapCodec<SetGameRules> codec() {
            return CODEC;
        }
    }

    public record TimeOfDay(int time) implements TestEnvironmentDefinition
    {
        public static final MapCodec<TimeOfDay> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("time").forGetter(TimeOfDay::time)).apply((Applicative)$$0, TimeOfDay::new));

        @Override
        public void setup(ServerLevel $$0) {
            $$0.setDayTime(this.time);
        }

        public MapCodec<TimeOfDay> codec() {
            return CODEC;
        }
    }

    public record Weather(Type weather) implements TestEnvironmentDefinition
    {
        public static final MapCodec<Weather> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Type.CODEC.fieldOf("weather").forGetter(Weather::weather)).apply((Applicative)$$0, Weather::new));

        @Override
        public void setup(ServerLevel $$0) {
            this.weather.apply($$0);
        }

        @Override
        public void teardown(ServerLevel $$0) {
            $$0.resetWeatherCycle();
        }

        public MapCodec<Weather> codec() {
            return CODEC;
        }

        public static enum Type implements StringRepresentable
        {
            CLEAR("clear", 100000, 0, false, false),
            RAIN("rain", 0, 100000, true, false),
            THUNDER("thunder", 0, 100000, true, true);

            public static final Codec<Type> CODEC;
            private final String id;
            private final int clearTime;
            private final int rainTime;
            private final boolean raining;
            private final boolean thundering;

            private Type(String $$0, int $$1, int $$2, boolean $$3, boolean $$4) {
                this.id = $$0;
                this.clearTime = $$1;
                this.rainTime = $$2;
                this.raining = $$3;
                this.thundering = $$4;
            }

            void apply(ServerLevel $$0) {
                $$0.setWeatherParameters(this.clearTime, this.rainTime, this.raining, this.thundering);
            }

            @Override
            public String getSerializedName() {
                return this.id;
            }

            static {
                CODEC = StringRepresentable.fromEnum(Type::values);
            }
        }
    }

    public record Functions(Optional<Identifier> setupFunction, Optional<Identifier> teardownFunction) implements TestEnvironmentDefinition
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<Functions> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Identifier.CODEC.optionalFieldOf("setup").forGetter(Functions::setupFunction), (App)Identifier.CODEC.optionalFieldOf("teardown").forGetter(Functions::teardownFunction)).apply((Applicative)$$0, Functions::new));

        @Override
        public void setup(ServerLevel $$0) {
            this.setupFunction.ifPresent($$1 -> Functions.run($$0, $$1));
        }

        @Override
        public void teardown(ServerLevel $$0) {
            this.teardownFunction.ifPresent($$1 -> Functions.run($$0, $$1));
        }

        private static void run(ServerLevel $$0, Identifier $$1) {
            MinecraftServer $$2 = $$0.getServer();
            ServerFunctionManager $$3 = $$2.getFunctions();
            Optional<CommandFunction<CommandSourceStack>> $$4 = $$3.get($$1);
            if ($$4.isPresent()) {
                CommandSourceStack $$5 = $$2.createCommandSourceStack().withPermission(LevelBasedPermissionSet.GAMEMASTER).withSuppressedOutput().withLevel($$0);
                $$3.execute($$4.get(), $$5);
            } else {
                LOGGER.error("Test Batch failed for non-existent function {}", (Object)$$1);
            }
        }

        public MapCodec<Functions> codec() {
            return CODEC;
        }
    }
}

