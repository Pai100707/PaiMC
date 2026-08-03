package net.minecraft.gametest;

import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTestMainUtil;
import net.minecraft.obfuscate.DontObfuscate;

public class Main {
   @DontObfuscate
   public static void main(String[] $$0) throws Exception {
      SharedConstants.tryDetectVersion();
      GameTestMainUtil.runGameTestServer($$0, $$0x -> {});
   }
}
