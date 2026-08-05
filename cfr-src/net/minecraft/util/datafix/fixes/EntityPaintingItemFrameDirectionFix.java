/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class EntityPaintingItemFrameDirectionFix
extends DataFix {
    private static final int[][] DIRECTIONS = new int[][]{{0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}};

    public EntityPaintingItemFrameDirectionFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    private Dynamic<?> doFix(Dynamic<?> $$0, boolean $$1, boolean $$2) {
        if (($$1 || $$2) && $$0.get("Facing").asNumber().result().isEmpty()) {
            int $$5;
            if ($$0.get("Direction").asNumber().result().isPresent()) {
                int $$3 = $$0.get("Direction").asByte((byte)0) % DIRECTIONS.length;
                int[] $$4 = DIRECTIONS[$$3];
                $$0 = $$0.set("TileX", $$0.createInt($$0.get("TileX").asInt(0) + $$4[0]));
                $$0 = $$0.set("TileY", $$0.createInt($$0.get("TileY").asInt(0) + $$4[1]));
                $$0 = $$0.set("TileZ", $$0.createInt($$0.get("TileZ").asInt(0) + $$4[2]));
                $$0 = $$0.remove("Direction");
                if ($$2 && $$0.get("ItemRotation").asNumber().result().isPresent()) {
                    $$0 = $$0.set("ItemRotation", $$0.createByte((byte)($$0.get("ItemRotation").asByte((byte)0) * 2)));
                }
            } else {
                $$5 = $$0.get("Dir").asByte((byte)0) % DIRECTIONS.length;
                $$0 = $$0.remove("Dir");
            }
            $$0 = $$0.set("Facing", $$0.createByte((byte)$$5));
        }
        return $$0;
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getChoiceType(References.ENTITY, "Painting");
        OpticFinder $$1 = DSL.namedChoice((String)"Painting", (Type)$$0);
        Type $$22 = this.getInputSchema().getChoiceType(References.ENTITY, "ItemFrame");
        OpticFinder $$3 = DSL.namedChoice((String)"ItemFrame", (Type)$$22);
        Type $$4 = this.getInputSchema().getType(References.ENTITY);
        TypeRewriteRule $$5 = this.fixTypeEverywhereTyped("EntityPaintingFix", $$4, $$2 -> $$2.updateTyped($$1, $$0, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> this.doFix((Dynamic<?>)$$0, true, false))));
        TypeRewriteRule $$6 = this.fixTypeEverywhereTyped("EntityItemFrameFix", $$4, $$2 -> $$2.updateTyped($$3, $$22, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> this.doFix((Dynamic<?>)$$0, false, true))));
        return TypeRewriteRule.seq((TypeRewriteRule)$$5, (TypeRewriteRule)$$6);
    }
}

