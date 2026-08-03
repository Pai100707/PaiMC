package net.minecraft.gametest.framework;

import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record GeneratedTest(
   Map<Identifier, TestData<ResourceKey<TestEnvironmentDefinition>>> tests,
   ResourceKey<Consumer<GameTestHelper>> functionKey,
   Consumer<GameTestHelper> function
) {
   public GeneratedTest(Map<Identifier, TestData<ResourceKey<TestEnvironmentDefinition>>> $$0, Identifier $$1, Consumer<GameTestHelper> $$2) {
      this($$0, ResourceKey.create(Registries.TEST_FUNCTION, $$1), $$2);
   }

   public GeneratedTest(Identifier $$0, TestData<ResourceKey<TestEnvironmentDefinition>> $$1, Consumer<GameTestHelper> $$2) {
      this(Map.of($$0, $$1), $$0, $$2);
   }
}
