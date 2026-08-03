package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.BundleContents.Mutable;

public interface ContainerComponentManipulators {
   ContainerComponentManipulator<ItemContainerContents> CONTAINER = new ContainerComponentManipulator<ItemContainerContents>() {
      @Override
      public DataComponentType<ItemContainerContents> type() {
         return DataComponents.CONTAINER;
      }

      public Stream<ItemStack> getContents(ItemContainerContents $$0) {
         return $$0.stream();
      }

      public ItemContainerContents empty() {
         return ItemContainerContents.EMPTY;
      }

      public ItemContainerContents setContents(ItemContainerContents $$0, Stream<ItemStack> $$1) {
         return ItemContainerContents.fromItems($$1.toList());
      }
   };
   ContainerComponentManipulator<BundleContents> BUNDLE_CONTENTS = new ContainerComponentManipulator<BundleContents>() {
      @Override
      public DataComponentType<BundleContents> type() {
         return DataComponents.BUNDLE_CONTENTS;
      }

      public BundleContents empty() {
         return BundleContents.EMPTY;
      }

      public Stream<ItemStack> getContents(BundleContents $$0) {
         return $$0.itemCopyStream();
      }

      public BundleContents setContents(BundleContents $$0, Stream<ItemStack> $$1) {
         Mutable $$2 = new Mutable($$0).clearItems();
         $$1.forEach($$2::tryInsert);
         return $$2.toImmutable();
      }
   };
   ContainerComponentManipulator<ChargedProjectiles> CHARGED_PROJECTILES = new ContainerComponentManipulator<ChargedProjectiles>() {
      @Override
      public DataComponentType<ChargedProjectiles> type() {
         return DataComponents.CHARGED_PROJECTILES;
      }

      public ChargedProjectiles empty() {
         return ChargedProjectiles.EMPTY;
      }

      public Stream<ItemStack> getContents(ChargedProjectiles $$0) {
         return $$0.getItems().stream();
      }

      public ChargedProjectiles setContents(ChargedProjectiles $$0, Stream<ItemStack> $$1) {
         return ChargedProjectiles.of($$1.toList());
      }
   };
   Map<DataComponentType<?>, ContainerComponentManipulator<?>> ALL_MANIPULATORS = Stream.of(CONTAINER, BUNDLE_CONTENTS, CHARGED_PROJECTILES)
      .collect(Collectors.toMap(ContainerComponentManipulator::type, $$0 -> (ContainerComponentManipulator<?>)$$0));
   Codec<ContainerComponentManipulator<?>> CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().comapFlatMap($$0 -> {
      ContainerComponentManipulator<?> $$1 = ALL_MANIPULATORS.get($$0);
      return $$1 != null ? DataResult.success($$1) : DataResult.error(() -> "No items in component");
   }, ContainerComponentManipulator::type);
}
