/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.fixes.References;

public class OptionsKeyLwjgl3Fix
extends DataFix {
    public static final String KEY_UNKNOWN = "key.unknown";
    private static final Int2ObjectMap<String> MAP = (Int2ObjectMap)DataFixUtils.make((Object)new Int2ObjectOpenHashMap(), $$0 -> {
        $$0.put(0, (Object)KEY_UNKNOWN);
        $$0.put(11, (Object)"key.0");
        $$0.put(2, (Object)"key.1");
        $$0.put(3, (Object)"key.2");
        $$0.put(4, (Object)"key.3");
        $$0.put(5, (Object)"key.4");
        $$0.put(6, (Object)"key.5");
        $$0.put(7, (Object)"key.6");
        $$0.put(8, (Object)"key.7");
        $$0.put(9, (Object)"key.8");
        $$0.put(10, (Object)"key.9");
        $$0.put(30, (Object)"key.a");
        $$0.put(40, (Object)"key.apostrophe");
        $$0.put(48, (Object)"key.b");
        $$0.put(43, (Object)"key.backslash");
        $$0.put(14, (Object)"key.backspace");
        $$0.put(46, (Object)"key.c");
        $$0.put(58, (Object)"key.caps.lock");
        $$0.put(51, (Object)"key.comma");
        $$0.put(32, (Object)"key.d");
        $$0.put(211, (Object)"key.delete");
        $$0.put(208, (Object)"key.down");
        $$0.put(18, (Object)"key.e");
        $$0.put(207, (Object)"key.end");
        $$0.put(28, (Object)"key.enter");
        $$0.put(13, (Object)"key.equal");
        $$0.put(1, (Object)"key.escape");
        $$0.put(33, (Object)"key.f");
        $$0.put(59, (Object)"key.f1");
        $$0.put(68, (Object)"key.f10");
        $$0.put(87, (Object)"key.f11");
        $$0.put(88, (Object)"key.f12");
        $$0.put(100, (Object)"key.f13");
        $$0.put(101, (Object)"key.f14");
        $$0.put(102, (Object)"key.f15");
        $$0.put(103, (Object)"key.f16");
        $$0.put(104, (Object)"key.f17");
        $$0.put(105, (Object)"key.f18");
        $$0.put(113, (Object)"key.f19");
        $$0.put(60, (Object)"key.f2");
        $$0.put(61, (Object)"key.f3");
        $$0.put(62, (Object)"key.f4");
        $$0.put(63, (Object)"key.f5");
        $$0.put(64, (Object)"key.f6");
        $$0.put(65, (Object)"key.f7");
        $$0.put(66, (Object)"key.f8");
        $$0.put(67, (Object)"key.f9");
        $$0.put(34, (Object)"key.g");
        $$0.put(41, (Object)"key.grave.accent");
        $$0.put(35, (Object)"key.h");
        $$0.put(199, (Object)"key.home");
        $$0.put(23, (Object)"key.i");
        $$0.put(210, (Object)"key.insert");
        $$0.put(36, (Object)"key.j");
        $$0.put(37, (Object)"key.k");
        $$0.put(82, (Object)"key.keypad.0");
        $$0.put(79, (Object)"key.keypad.1");
        $$0.put(80, (Object)"key.keypad.2");
        $$0.put(81, (Object)"key.keypad.3");
        $$0.put(75, (Object)"key.keypad.4");
        $$0.put(76, (Object)"key.keypad.5");
        $$0.put(77, (Object)"key.keypad.6");
        $$0.put(71, (Object)"key.keypad.7");
        $$0.put(72, (Object)"key.keypad.8");
        $$0.put(73, (Object)"key.keypad.9");
        $$0.put(78, (Object)"key.keypad.add");
        $$0.put(83, (Object)"key.keypad.decimal");
        $$0.put(181, (Object)"key.keypad.divide");
        $$0.put(156, (Object)"key.keypad.enter");
        $$0.put(141, (Object)"key.keypad.equal");
        $$0.put(55, (Object)"key.keypad.multiply");
        $$0.put(74, (Object)"key.keypad.subtract");
        $$0.put(38, (Object)"key.l");
        $$0.put(203, (Object)"key.left");
        $$0.put(56, (Object)"key.left.alt");
        $$0.put(26, (Object)"key.left.bracket");
        $$0.put(29, (Object)"key.left.control");
        $$0.put(42, (Object)"key.left.shift");
        $$0.put(219, (Object)"key.left.win");
        $$0.put(50, (Object)"key.m");
        $$0.put(12, (Object)"key.minus");
        $$0.put(49, (Object)"key.n");
        $$0.put(69, (Object)"key.num.lock");
        $$0.put(24, (Object)"key.o");
        $$0.put(25, (Object)"key.p");
        $$0.put(209, (Object)"key.page.down");
        $$0.put(201, (Object)"key.page.up");
        $$0.put(197, (Object)"key.pause");
        $$0.put(52, (Object)"key.period");
        $$0.put(183, (Object)"key.print.screen");
        $$0.put(16, (Object)"key.q");
        $$0.put(19, (Object)"key.r");
        $$0.put(205, (Object)"key.right");
        $$0.put(184, (Object)"key.right.alt");
        $$0.put(27, (Object)"key.right.bracket");
        $$0.put(157, (Object)"key.right.control");
        $$0.put(54, (Object)"key.right.shift");
        $$0.put(220, (Object)"key.right.win");
        $$0.put(31, (Object)"key.s");
        $$0.put(70, (Object)"key.scroll.lock");
        $$0.put(39, (Object)"key.semicolon");
        $$0.put(53, (Object)"key.slash");
        $$0.put(57, (Object)"key.space");
        $$0.put(20, (Object)"key.t");
        $$0.put(15, (Object)"key.tab");
        $$0.put(22, (Object)"key.u");
        $$0.put(200, (Object)"key.up");
        $$0.put(47, (Object)"key.v");
        $$0.put(17, (Object)"key.w");
        $$0.put(45, (Object)"key.x");
        $$0.put(21, (Object)"key.y");
        $$0.put(44, (Object)"key.z");
    });

    public OptionsKeyLwjgl3Fix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsKeyLwjgl3Fix", this.getInputSchema().getType(References.OPTIONS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.getMapValues().map($$1 -> $$0.createMap($$1.entrySet().stream().map($$0 -> {
            if (((Dynamic)$$0.getKey()).asString("").startsWith("key_")) {
                Object $$1 = Integer.parseInt(((Dynamic)$$0.getValue()).asString(""));
                if ($$1 < 0) {
                    String $$6;
                    int $$2 = $$1 + 100;
                    if ($$2 == 0) {
                        String $$3 = "key.mouse.left";
                    } else if ($$2 == 1) {
                        String $$4 = "key.mouse.right";
                    } else if ($$2 == 2) {
                        String $$5 = "key.mouse.middle";
                    } else {
                        $$6 = "key.mouse." + ($$2 + 1);
                    }
                    return Pair.of((Object)((Dynamic)$$0.getKey()), (Object)((Dynamic)$$0.getValue()).createString($$6));
                }
                String $$7 = (String)MAP.getOrDefault($$1, (Object)KEY_UNKNOWN);
                return Pair.of((Object)((Dynamic)$$0.getKey()), (Object)((Dynamic)$$0.getValue()).createString($$7));
            }
            return Pair.of((Object)((Dynamic)$$0.getKey()), (Object)((Dynamic)$$0.getValue()));
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse($$0)));
    }
}

