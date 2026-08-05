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
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntitySpawnerItemVariantComponentFix
extends DataFix {
    public EntitySpawnerItemVariantComponentFix(Schema $$0) {
        super($$0, false);
    }

    public final TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$1 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder $$22 = $$0.findField("components");
        return this.fixTypeEverywhereTyped("ItemStack bucket_entity_data variants to separate components", $$0, $$2 -> {
            String $$3;
            return switch ($$3 = $$2.getOptional($$1).map(Pair::getSecond).orElse("")) {
                case "minecraft:salmon_bucket" -> $$2.updateTyped($$22, EntitySpawnerItemVariantComponentFix::fixSalmonBucket);
                case "minecraft:axolotl_bucket" -> $$2.updateTyped($$22, EntitySpawnerItemVariantComponentFix::fixAxolotlBucket);
                case "minecraft:tropical_fish_bucket" -> $$2.updateTyped($$22, EntitySpawnerItemVariantComponentFix::fixTropicalFishBucket);
                case "minecraft:painting" -> $$2.updateTyped($$22, $$0 -> Util.writeAndReadTypedOrThrow($$0, $$0.getType(), EntitySpawnerItemVariantComponentFix::fixPainting));
                default -> $$2;
            };
        });
    }

    private static String getBaseColor(int $$0) {
        return ExtraDataFixUtils.dyeColorIdToName($$0 >> 16 & 0xFF);
    }

    private static String getPatternColor(int $$0) {
        return ExtraDataFixUtils.dyeColorIdToName($$0 >> 24 & 0xFF);
    }

    private static String getPattern(int $$0) {
        return switch ($$0 & 0xFFFF) {
            default -> "kob";
            case 256 -> "sunstreak";
            case 512 -> "snooper";
            case 768 -> "dasher";
            case 1024 -> "brinely";
            case 1280 -> "spotty";
            case 1 -> "flopper";
            case 257 -> "stripey";
            case 513 -> "glitter";
            case 769 -> "blockfish";
            case 1025 -> "betty";
            case 1281 -> "clayfish";
        };
    }

    private static <T> Dynamic<T> fixTropicalFishBucket(Dynamic<T> $$02, Dynamic<T> $$1) {
        Optional $$2 = $$1.get("BucketVariantTag").asNumber().result();
        if ($$2.isEmpty()) {
            return $$02;
        }
        int $$3 = ((Number)$$2.get()).intValue();
        String $$4 = EntitySpawnerItemVariantComponentFix.getPattern($$3);
        String $$5 = EntitySpawnerItemVariantComponentFix.getBaseColor($$3);
        String $$6 = EntitySpawnerItemVariantComponentFix.getPatternColor($$3);
        return $$02.update("minecraft:bucket_entity_data", $$0 -> $$0.remove("BucketVariantTag")).set("minecraft:tropical_fish/pattern", $$02.createString($$4)).set("minecraft:tropical_fish/base_color", $$02.createString($$5)).set("minecraft:tropical_fish/pattern_color", $$02.createString($$6));
    }

    private static <T> Dynamic<T> fixAxolotlBucket(Dynamic<T> $$02, Dynamic<T> $$1) {
        Optional $$2 = $$1.get("Variant").asNumber().result();
        if ($$2.isEmpty()) {
            return $$02;
        }
        String $$3 = switch (((Number)$$2.get()).intValue()) {
            default -> "lucy";
            case 1 -> "wild";
            case 2 -> "gold";
            case 3 -> "cyan";
            case 4 -> "blue";
        };
        return $$02.update("minecraft:bucket_entity_data", $$0 -> $$0.remove("Variant")).set("minecraft:axolotl/variant", $$02.createString($$3));
    }

    private static <T> Dynamic<T> fixSalmonBucket(Dynamic<T> $$02, Dynamic<T> $$1) {
        Optional $$2 = $$1.get("type").result();
        if ($$2.isEmpty()) {
            return $$02;
        }
        return $$02.update("minecraft:bucket_entity_data", $$0 -> $$0.remove("type")).set("minecraft:salmon/size", (Dynamic)$$2.get());
    }

    private static <T> Dynamic<T> fixPainting(Dynamic<T> $$02) {
        Optional $$1 = $$02.get("minecraft:entity_data").result();
        if ($$1.isEmpty()) {
            return $$02;
        }
        if (((Dynamic)$$1.get()).get("id").asString().result().filter($$0 -> $$0.equals("minecraft:painting")).isEmpty()) {
            return $$02;
        }
        Optional $$2 = ((Dynamic)$$1.get()).get("variant").result();
        Dynamic $$3 = ((Dynamic)$$1.get()).remove("variant");
        $$02 = $$3.remove("id").equals((Object)$$3.emptyMap()) ? $$02.remove("minecraft:entity_data") : $$02.set("minecraft:entity_data", $$3);
        if ($$2.isPresent()) {
            $$02 = $$02.set("minecraft:painting/variant", (Dynamic)$$2.get());
        }
        return $$02;
    }

    @FunctionalInterface
    static interface Fixer
    extends Function<Typed<?>, Typed<?>> {
        @Override
        default public Typed<?> apply(Typed<?> $$0) {
            return $$0.update(DSL.remainderFinder(), this::fixRemainder);
        }

        default public <T> Dynamic<T> fixRemainder(Dynamic<T> $$0) {
            return $$0.get("minecraft:bucket_entity_data").result().map($$1 -> this.fixRemainder($$0, (Dynamic)$$1)).orElse($$0);
        }

        public <T> Dynamic<T> fixRemainder(Dynamic<T> var1, Dynamic<T> var2);
    }
}

