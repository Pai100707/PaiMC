/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.level.GameType;

public record GameTypePredicate(List<GameType> types) {
    public static final GameTypePredicate ANY = GameTypePredicate.of(GameType.values());
    public static final GameTypePredicate SURVIVAL_LIKE = GameTypePredicate.of(GameType.SURVIVAL, GameType.ADVENTURE);
    public static final Codec<GameTypePredicate> CODEC = GameType.CODEC.listOf().xmap(GameTypePredicate::new, GameTypePredicate::types);

    public static GameTypePredicate of(GameType ... $$0) {
        return new GameTypePredicate(Arrays.stream($$0).toList());
    }

    public boolean matches(GameType $$0) {
        return this.types.contains($$0);
    }
}

