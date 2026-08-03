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

public class OptionsKeyLwjgl3Fix extends DataFix {
   public static final String KEY_UNKNOWN = "key.unknown";
   private static final Int2ObjectMap<String> MAP = (Int2ObjectMap<String>)DataFixUtils.make(new Int2ObjectOpenHashMap(), $$0 -> {
      $$0.put(0, "key.unknown");
      $$0.put(11, "key.0");
      $$0.put(2, "key.1");
      $$0.put(3, "key.2");
      $$0.put(4, "key.3");
      $$0.put(5, "key.4");
      $$0.put(6, "key.5");
      $$0.put(7, "key.6");
      $$0.put(8, "key.7");
      $$0.put(9, "key.8");
      $$0.put(10, "key.9");
      $$0.put(30, "key.a");
      $$0.put(40, "key.apostrophe");
      $$0.put(48, "key.b");
      $$0.put(43, "key.backslash");
      $$0.put(14, "key.backspace");
      $$0.put(46, "key.c");
      $$0.put(58, "key.caps.lock");
      $$0.put(51, "key.comma");
      $$0.put(32, "key.d");
      $$0.put(211, "key.delete");
      $$0.put(208, "key.down");
      $$0.put(18, "key.e");
      $$0.put(207, "key.end");
      $$0.put(28, "key.enter");
      $$0.put(13, "key.equal");
      $$0.put(1, "key.escape");
      $$0.put(33, "key.f");
      $$0.put(59, "key.f1");
      $$0.put(68, "key.f10");
      $$0.put(87, "key.f11");
      $$0.put(88, "key.f12");
      $$0.put(100, "key.f13");
      $$0.put(101, "key.f14");
      $$0.put(102, "key.f15");
      $$0.put(103, "key.f16");
      $$0.put(104, "key.f17");
      $$0.put(105, "key.f18");
      $$0.put(113, "key.f19");
      $$0.put(60, "key.f2");
      $$0.put(61, "key.f3");
      $$0.put(62, "key.f4");
      $$0.put(63, "key.f5");
      $$0.put(64, "key.f6");
      $$0.put(65, "key.f7");
      $$0.put(66, "key.f8");
      $$0.put(67, "key.f9");
      $$0.put(34, "key.g");
      $$0.put(41, "key.grave.accent");
      $$0.put(35, "key.h");
      $$0.put(199, "key.home");
      $$0.put(23, "key.i");
      $$0.put(210, "key.insert");
      $$0.put(36, "key.j");
      $$0.put(37, "key.k");
      $$0.put(82, "key.keypad.0");
      $$0.put(79, "key.keypad.1");
      $$0.put(80, "key.keypad.2");
      $$0.put(81, "key.keypad.3");
      $$0.put(75, "key.keypad.4");
      $$0.put(76, "key.keypad.5");
      $$0.put(77, "key.keypad.6");
      $$0.put(71, "key.keypad.7");
      $$0.put(72, "key.keypad.8");
      $$0.put(73, "key.keypad.9");
      $$0.put(78, "key.keypad.add");
      $$0.put(83, "key.keypad.decimal");
      $$0.put(181, "key.keypad.divide");
      $$0.put(156, "key.keypad.enter");
      $$0.put(141, "key.keypad.equal");
      $$0.put(55, "key.keypad.multiply");
      $$0.put(74, "key.keypad.subtract");
      $$0.put(38, "key.l");
      $$0.put(203, "key.left");
      $$0.put(56, "key.left.alt");
      $$0.put(26, "key.left.bracket");
      $$0.put(29, "key.left.control");
      $$0.put(42, "key.left.shift");
      $$0.put(219, "key.left.win");
      $$0.put(50, "key.m");
      $$0.put(12, "key.minus");
      $$0.put(49, "key.n");
      $$0.put(69, "key.num.lock");
      $$0.put(24, "key.o");
      $$0.put(25, "key.p");
      $$0.put(209, "key.page.down");
      $$0.put(201, "key.page.up");
      $$0.put(197, "key.pause");
      $$0.put(52, "key.period");
      $$0.put(183, "key.print.screen");
      $$0.put(16, "key.q");
      $$0.put(19, "key.r");
      $$0.put(205, "key.right");
      $$0.put(184, "key.right.alt");
      $$0.put(27, "key.right.bracket");
      $$0.put(157, "key.right.control");
      $$0.put(54, "key.right.shift");
      $$0.put(220, "key.right.win");
      $$0.put(31, "key.s");
      $$0.put(70, "key.scroll.lock");
      $$0.put(39, "key.semicolon");
      $$0.put(53, "key.slash");
      $$0.put(57, "key.space");
      $$0.put(20, "key.t");
      $$0.put(15, "key.tab");
      $$0.put(22, "key.u");
      $$0.put(200, "key.up");
      $$0.put(47, "key.v");
      $$0.put(17, "key.w");
      $$0.put(45, "key.x");
      $$0.put(21, "key.y");
      $$0.put(44, "key.z");
   });

   public OptionsKeyLwjgl3Fix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "OptionsKeyLwjgl3Fix",
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> $$0x.getMapValues().map($$1 -> $$0x.createMap($$1.entrySet().stream().map($$0xxx -> {
            if (((Dynamic)$$0xxx.getKey()).asString("").startsWith("key_")) {
               int $$1x = Integer.parseInt(((Dynamic)$$0xxx.getValue()).asString(""));
               if ($$1x < 0) {
                  int $$2 = $$1x + 100;
                  String $$3;
                  if ($$2 == 0) {
                     $$3 = "key.mouse.left";
                  } else if ($$2 == 1) {
                     $$3 = "key.mouse.right";
                  } else if ($$2 == 2) {
                     $$3 = "key.mouse.middle";
                  } else {
                     $$3 = "key.mouse." + ($$2 + 1);
                  }

                  return Pair.of((Dynamic)$$0xxx.getKey(), ((Dynamic)$$0xxx.getValue()).createString($$3));
               } else {
                  String $$7 = (String)MAP.getOrDefault($$1x, "key.unknown");
                  return Pair.of((Dynamic)$$0xxx.getKey(), ((Dynamic)$$0xxx.getValue()).createString($$7));
               }
            } else {
               return Pair.of((Dynamic)$$0xxx.getKey(), (Dynamic)$$0xxx.getValue());
            }
         }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse($$0x))
      );
   }
}
