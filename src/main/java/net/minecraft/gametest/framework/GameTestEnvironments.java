package net.minecraft.gametest.framework;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface GameTestEnvironments {
   String DEFAULT = "default";
   ResourceKey<TestEnvironmentDefinition> DEFAULT_KEY = create("default");

   private static ResourceKey<TestEnvironmentDefinition> create(String $$0) {
      return ResourceKey.create(Registries.TEST_ENVIRONMENT, Identifier.withDefaultNamespace($$0));
   }

   static void bootstrap(BootstrapContext<TestEnvironmentDefinition> $$0) {
      $$0.register(DEFAULT_KEY, new TestEnvironmentDefinition.AllOf(List.of()));
   }
}
