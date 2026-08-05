/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class OminousBannerRarityFix
extends DataFix {
    public OminousBannerRarityFix(Schema $$0) {
        super($$0, false);
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type $$1 = this.getInputSchema().getType(References.ITEM_STACK);
        TaggedChoice.TaggedChoiceType $$2 = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder $$3 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder $$42 = $$0.findField("components");
        OpticFinder $$5 = $$1.findField("components");
        OpticFinder $$6 = $$42.type().findField("minecraft:item_name");
        OpticFinder $$7 = DSL.typeFinder((Type)this.getInputSchema().getType(References.TEXT_COMPONENT));
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("Ominous Banner block entity common rarity to uncommon rarity fix", $$0, $$4 -> {
            Object $$5 = ((Pair)$$4.get($$2.finder())).getFirst();
            return $$5.equals("minecraft:banner") ? this.fix((Typed<?>)$$4, (OpticFinder<?>)$$42, (OpticFinder<?>)$$6, (OpticFinder<Pair<String, String>>)$$7) : $$4;
        }), (TypeRewriteRule)this.fixTypeEverywhereTyped("Ominous Banner item stack common rarity to uncommon rarity fix", $$1, $$4 -> {
            String $$5 = $$4.getOptional($$3).map(Pair::getSecond).orElse("");
            return $$5.equals("minecraft:white_banner") ? this.fix((Typed<?>)$$4, (OpticFinder<?>)$$5, (OpticFinder<?>)$$6, (OpticFinder<Pair<String, String>>)$$7) : $$4;
        }));
    }

    private Typed<?> fix(Typed<?> $$0, OpticFinder<?> $$1, OpticFinder<?> $$22, OpticFinder<Pair<String, String>> $$3) {
        return $$0.updateTyped($$1, $$2 -> {
            boolean $$3 = $$2.getOptionalTyped($$22).flatMap($$1 -> $$1.getOptional($$3)).map(Pair::getSecond).flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter($$0 -> $$0.equals("block.minecraft.ominous_banner")).isPresent();
            if ($$3) {
                return $$2.updateTyped($$22, $$1 -> $$1.set($$3, (Object)Pair.of((Object)References.TEXT_COMPONENT.typeName(), (Object)LegacyComponentDataFixUtils.createTranslatableComponentJson("block.minecraft.ominous_banner")))).update(DSL.remainderFinder(), $$0 -> $$0.set("minecraft:rarity", $$0.createString("uncommon")));
            }
            return $$2;
        });
    }
}

