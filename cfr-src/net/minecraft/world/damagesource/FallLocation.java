/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.damagesource;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public record FallLocation(String id) {
    public static final FallLocation GENERIC = new FallLocation("generic");
    public static final FallLocation LADDER = new FallLocation("ladder");
    public static final FallLocation VINES = new FallLocation("vines");
    public static final FallLocation WEEPING_VINES = new FallLocation("weeping_vines");
    public static final FallLocation TWISTING_VINES = new FallLocation("twisting_vines");
    public static final FallLocation SCAFFOLDING = new FallLocation("scaffolding");
    public static final FallLocation OTHER_CLIMBABLE = new FallLocation("other_climbable");
    public static final FallLocation WATER = new FallLocation("water");

    public static FallLocation blockToFallLocation(BlockState $$0) {
        if ($$0.is(Blocks.LADDER) || $$0.is(BlockTags.TRAPDOORS)) {
            return LADDER;
        }
        if ($$0.is(Blocks.VINE)) {
            return VINES;
        }
        if ($$0.is(Blocks.WEEPING_VINES) || $$0.is(Blocks.WEEPING_VINES_PLANT)) {
            return WEEPING_VINES;
        }
        if ($$0.is(Blocks.TWISTING_VINES) || $$0.is(Blocks.TWISTING_VINES_PLANT)) {
            return TWISTING_VINES;
        }
        if ($$0.is(Blocks.SCAFFOLDING)) {
            return SCAFFOLDING;
        }
        return OTHER_CLIMBABLE;
    }

    public static @Nullable FallLocation getCurrentFallLocation(LivingEntity $$0) {
        Optional<BlockPos> $$1 = $$0.getLastClimbablePos();
        if ($$1.isPresent()) {
            BlockState $$2 = $$0.level().getBlockState($$1.get());
            return FallLocation.blockToFallLocation($$2);
        }
        if ($$0.isInWater()) {
            return WATER;
        }
        return null;
    }

    public String languageKey() {
        return "death.fell.accident." + this.id;
    }
}

