package net.minecraft.world.level.storage.loot.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SetNameFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec<SetNameFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               ComponentSerialization.CODEC.optionalFieldOf("name").forGetter($$0x -> $$0x.name),
               LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter($$0x -> $$0x.resolutionContext),
               SetNameFunction.Target.CODEC.optionalFieldOf("target", SetNameFunction.Target.CUSTOM_NAME).forGetter($$0x -> $$0x.target)
            )
         )
         .apply($$0, SetNameFunction::new)
   );
   private final Optional<Component> name;
   private final Optional<LootContext.EntityTarget> resolutionContext;
   private final SetNameFunction.Target target;

   private SetNameFunction(List<LootItemCondition> $$0, Optional<Component> $$1, Optional<LootContext.EntityTarget> $$2, SetNameFunction.Target $$3) {
      super($$0);
      this.name = $$1;
      this.resolutionContext = $$2;
      this.target = $$3;
   }

   @Override
   public LootItemFunctionType<SetNameFunction> getType() {
      return LootItemFunctions.SET_NAME;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.resolutionContext.<Set<ContextKey<?>>>map($$0 -> Set.of($$0.contextParam())).orElse(Set.of());
   }

   public static UnaryOperator<Component> createResolver(LootContext $$0, LootContext.EntityTarget $$1) {
      if ($$1 != null) {
         Entity $$2 = $$0.getOptionalParameter($$1.contextParam());
         if ($$2 != null) {
            CommandSourceStack $$3 = $$2.createCommandSourceStackForNameResolution($$0.getLevel()).withPermission(LevelBasedPermissionSet.GAMEMASTER);
            return $$2x -> {
               try {
                  return ComponentUtils.updateForEntity($$3, $$2x, $$2, 0);
               } catch (CommandSyntaxException var4) {
                  LOGGER.warn("Failed to resolve text component", var4);
                  return $$2x;
               }
            };
         }
      }

      return $$0x -> $$0x;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      this.name.ifPresent($$2 -> $$0.set(this.target.component(), createResolver($$1, this.resolutionContext.orElse(null)).apply($$2)));
      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> setName(Component $$0, SetNameFunction.Target $$1) {
      return simpleBuilder($$2 -> new SetNameFunction($$2, Optional.of($$0), Optional.empty(), $$1));
   }

   public static LootItemConditionalFunction.Builder<?> setName(Component $$0, SetNameFunction.Target $$1, LootContext.EntityTarget $$2) {
      return simpleBuilder($$3 -> new SetNameFunction($$3, Optional.of($$0), Optional.of($$2), $$1));
   }

   public static enum Target implements StringRepresentable {
      CUSTOM_NAME("custom_name"),
      ITEM_NAME("item_name");

      public static final Codec<SetNameFunction.Target> CODEC = StringRepresentable.fromEnum(SetNameFunction.Target::values);
      private final String name;

      private Target(final String $$0) {
         this.name = $$0;
      }

      public String getSerializedName() {
         return this.name;
      }

      public DataComponentType<Component> component() {
         return switch (this) {
            case CUSTOM_NAME -> DataComponents.CUSTOM_NAME;
            case ITEM_NAME -> DataComponents.ITEM_NAME;
         };
      }
   }
}
