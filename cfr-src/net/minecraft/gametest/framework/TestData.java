/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.gametest.framework;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Rotation;

public record TestData<EnvironmentType>(EnvironmentType environment, Identifier structure, int maxTicks, int setupTicks, boolean required, Rotation rotation, boolean manualOnly, int maxAttempts, int requiredSuccesses, boolean skyAccess) {
    public static final MapCodec<TestData<Holder<TestEnvironmentDefinition>>> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)TestEnvironmentDefinition.CODEC.fieldOf("environment").forGetter(TestData::environment), (App)Identifier.CODEC.fieldOf("structure").forGetter(TestData::structure), (App)ExtraCodecs.POSITIVE_INT.fieldOf("max_ticks").forGetter(TestData::maxTicks), (App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("setup_ticks", (Object)0).forGetter(TestData::setupTicks), (App)Codec.BOOL.optionalFieldOf("required", (Object)true).forGetter(TestData::required), (App)Rotation.CODEC.optionalFieldOf("rotation", (Object)Rotation.NONE).forGetter(TestData::rotation), (App)Codec.BOOL.optionalFieldOf("manual_only", (Object)false).forGetter(TestData::manualOnly), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_attempts", (Object)1).forGetter(TestData::maxAttempts), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("required_successes", (Object)1).forGetter(TestData::requiredSuccesses), (App)Codec.BOOL.optionalFieldOf("sky_access", (Object)false).forGetter(TestData::skyAccess)).apply((Applicative)$$0, TestData::new));

    public TestData(EnvironmentType $$0, Identifier $$1, int $$2, int $$3, boolean $$4, Rotation $$5) {
        this($$0, $$1, $$2, $$3, $$4, $$5, false, 1, 1, false);
    }

    public TestData(EnvironmentType $$0, Identifier $$1, int $$2, int $$3, boolean $$4) {
        this($$0, $$1, $$2, $$3, $$4, Rotation.NONE);
    }

    public <T> TestData<T> map(Function<EnvironmentType, T> $$0) {
        return new TestData<T>($$0.apply(this.environment), this.structure, this.maxTicks, this.setupTicks, this.required, this.rotation, this.manualOnly, this.maxAttempts, this.requiredSuccesses, this.skyAccess);
    }
}

