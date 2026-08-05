/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntitySignDoubleSidedEditableTextFix
extends NamedEntityWriteReadFix {
    public static final List<String> FIELDS_TO_DROP = List.of("Text1", "Text2", "Text3", "Text4", "FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4", "Color", "GlowingText");
    public static final String FILTERED_CORRECT = "_filtered_correct";
    private static final String DEFAULT_COLOR = "black";

    public BlockEntitySignDoubleSidedEditableTextFix(Schema $$0, String $$1, String $$2) {
        super($$0, true, $$1, References.BLOCK_ENTITY, $$2);
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
        $$0 = $$0.set("front_text", BlockEntitySignDoubleSidedEditableTextFix.fixFrontTextTag($$0)).set("back_text", BlockEntitySignDoubleSidedEditableTextFix.createDefaultText($$0)).set("is_waxed", $$0.createBoolean(false)).set(FILTERED_CORRECT, $$0.createBoolean(true));
        for (String $$1 : FIELDS_TO_DROP) {
            $$0 = $$0.remove($$1);
        }
        return $$0;
    }

    private static <T> Dynamic<T> fixFrontTextTag(Dynamic<T> $$0) {
        Dynamic $$12 = LegacyComponentDataFixUtils.createEmptyComponent($$0.getOps());
        List<Dynamic> $$22 = BlockEntitySignDoubleSidedEditableTextFix.getLines($$0, "Text").map($$1 -> $$1.orElse($$12)).toList();
        Dynamic $$3 = $$0.emptyMap().set("messages", $$0.createList($$22.stream())).set("color", $$0.get("Color").result().orElse($$0.createString(DEFAULT_COLOR))).set("has_glowing_text", $$0.get("GlowingText").result().orElse($$0.createBoolean(false)));
        List<Optional<Dynamic<T>>> $$4 = BlockEntitySignDoubleSidedEditableTextFix.getLines($$0, "FilteredText").toList();
        if ($$4.stream().anyMatch(Optional::isPresent)) {
            $$3 = $$3.set("filtered_messages", $$0.createList(Streams.mapWithIndex($$4.stream(), ($$1, $$2) -> {
                Dynamic $$3 = (Dynamic)$$22.get((int)$$2);
                return $$1.orElse($$3);
            })));
        }
        return $$3;
    }

    private static <T> Stream<Optional<Dynamic<T>>> getLines(Dynamic<T> $$0, String $$1) {
        return Stream.of($$0.get($$1 + "1").result(), $$0.get($$1 + "2").result(), $$0.get($$1 + "3").result(), $$0.get($$1 + "4").result());
    }

    private static <T> Dynamic<T> createDefaultText(Dynamic<T> $$0) {
        return $$0.emptyMap().set("messages", BlockEntitySignDoubleSidedEditableTextFix.createEmptyLines($$0)).set("color", $$0.createString(DEFAULT_COLOR)).set("has_glowing_text", $$0.createBoolean(false));
    }

    private static <T> Dynamic<T> createEmptyLines(Dynamic<T> $$0) {
        Dynamic $$1 = LegacyComponentDataFixUtils.createEmptyComponent($$0.getOps());
        return $$0.createList(Stream.of($$1, $$1, $$1, $$1));
    }
}

