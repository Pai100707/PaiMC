/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.player.Input;

public record InputPredicate(Optional<Boolean> forward, Optional<Boolean> backward, Optional<Boolean> left, Optional<Boolean> right, Optional<Boolean> jump, Optional<Boolean> sneak, Optional<Boolean> sprint) {
    public static final Codec<InputPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.BOOL.optionalFieldOf("forward").forGetter(InputPredicate::forward), (App)Codec.BOOL.optionalFieldOf("backward").forGetter(InputPredicate::backward), (App)Codec.BOOL.optionalFieldOf("left").forGetter(InputPredicate::left), (App)Codec.BOOL.optionalFieldOf("right").forGetter(InputPredicate::right), (App)Codec.BOOL.optionalFieldOf("jump").forGetter(InputPredicate::jump), (App)Codec.BOOL.optionalFieldOf("sneak").forGetter(InputPredicate::sneak), (App)Codec.BOOL.optionalFieldOf("sprint").forGetter(InputPredicate::sprint)).apply((Applicative)$$0, InputPredicate::new));

    public boolean matches(Input $$0) {
        return this.matches(this.forward, $$0.forward()) && this.matches(this.backward, $$0.backward()) && this.matches(this.left, $$0.left()) && this.matches(this.right, $$0.right()) && this.matches(this.jump, $$0.jump()) && this.matches(this.sneak, $$0.shift()) && this.matches(this.sprint, $$0.sprint());
    }

    private boolean matches(Optional<Boolean> $$0, boolean $$12) {
        return $$0.map($$1 -> $$1 == $$12).orElse(true);
    }
}

