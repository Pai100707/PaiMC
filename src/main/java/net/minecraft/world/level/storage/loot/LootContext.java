package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.StringRepresentable.EnumCodec;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
   private final LootParams params;
   private final RandomSource random;
   private final Provider lootDataResolver;
   private final Set<LootContext.VisitedEntry<?>> visitedElements = Sets.newLinkedHashSet();

   LootContext(LootParams $$0, RandomSource $$1, Provider $$2) {
      this.params = $$0;
      this.random = $$1;
      this.lootDataResolver = $$2;
   }

   public boolean hasParameter(ContextKey<?> $$0) {
      return this.params.contextMap().has($$0);
   }

   public <T> T getParameter(ContextKey<T> $$0) {
      return (T)this.params.contextMap().getOrThrow($$0);
   }

   
   public <T> T getOptionalParameter(ContextKey<T> $$0) {
      return (T)this.params.contextMap().getOptional($$0);
   }

   public void addDynamicDrops(Identifier $$0, Consumer<ItemStack> $$1) {
      this.params.addDynamicDrops($$0, $$1);
   }

   public boolean hasVisitedElement(LootContext.VisitedEntry<?> $$0) {
      return this.visitedElements.contains($$0);
   }

   public boolean pushVisitedElement(LootContext.VisitedEntry<?> $$0) {
      return this.visitedElements.add($$0);
   }

   public void popVisitedElement(LootContext.VisitedEntry<?> $$0) {
      this.visitedElements.remove($$0);
   }

   public Provider getResolver() {
      return this.lootDataResolver;
   }

   public RandomSource getRandom() {
      return this.random;
   }

   public float getLuck() {
      return this.params.getLuck();
   }

   public ServerLevel getLevel() {
      return this.params.getLevel();
   }

   public static LootContext.VisitedEntry<LootTable> createVisitedEntry(LootTable $$0) {
      return new LootContext.VisitedEntry<>(LootDataType.TABLE, $$0);
   }

   public static LootContext.VisitedEntry<LootItemCondition> createVisitedEntry(LootItemCondition $$0) {
      return new LootContext.VisitedEntry<>(LootDataType.PREDICATE, $$0);
   }

   public static LootContext.VisitedEntry<LootItemFunction> createVisitedEntry(LootItemFunction $$0) {
      return new LootContext.VisitedEntry<>(LootDataType.MODIFIER, $$0);
   }

   public static enum BlockEntityTarget implements StringRepresentable, LootContextArg.SimpleGetter<BlockEntity> {
      BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

      private final String name;
      private final ContextKey<? extends BlockEntity> param;

      private BlockEntityTarget(final String $$0, final ContextKey<? extends BlockEntity> $$1) {
         this.name = $$0;
         this.param = $$1;
      }

      @Override
      public ContextKey<? extends BlockEntity> contextParam() {
         return this.param;
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   public static class Builder {
      private final LootParams params;
      
      private RandomSource random;

      public Builder(LootParams $$0) {
         this.params = $$0;
      }

      public LootContext.Builder withOptionalRandomSeed(long $$0) {
         if ($$0 != 0L) {
            this.random = RandomSource.create($$0);
         }

         return this;
      }

      public LootContext.Builder withOptionalRandomSource(RandomSource $$0) {
         this.random = $$0;
         return this;
      }

      public ServerLevel getLevel() {
         return this.params.getLevel();
      }

      public LootContext create(Optional<Identifier> $$0) {
         ServerLevel $$1 = this.getLevel();
         MinecraftServer $$2 = $$1.getServer();
         RandomSource $$3 = Optional.ofNullable(this.random).or(() -> $$0.map($$1::getRandomSequence)).orElseGet($$1::getRandom);
         return new LootContext(this.params, $$3, $$2.reloadableRegistries().lookup());
      }
   }

   public static enum EntityTarget implements StringRepresentable, LootContextArg.SimpleGetter<Entity> {
      THIS("this", LootContextParams.THIS_ENTITY),
      ATTACKER("attacker", LootContextParams.ATTACKING_ENTITY),
      DIRECT_ATTACKER("direct_attacker", LootContextParams.DIRECT_ATTACKING_ENTITY),
      ATTACKING_PLAYER("attacking_player", LootContextParams.LAST_DAMAGE_PLAYER),
      TARGET_ENTITY("target_entity", LootContextParams.TARGET_ENTITY),
      INTERACTING_ENTITY("interacting_entity", LootContextParams.INTERACTING_ENTITY);

      public static final EnumCodec<LootContext.EntityTarget> CODEC = StringRepresentable.fromEnum(LootContext.EntityTarget::values);
      private final String name;
      private final ContextKey<? extends Entity> param;

      private EntityTarget(final String $$0, final ContextKey<? extends Entity> $$1) {
         this.name = $$0;
         this.param = $$1;
      }

      @Override
      public ContextKey<? extends Entity> contextParam() {
         return this.param;
      }

      public static LootContext.EntityTarget getByName(String $$0) {
         LootContext.EntityTarget $$1 = (LootContext.EntityTarget)CODEC.byName($$0);
         if ($$1 != null) {
            return $$1;
         } else {
            throw new IllegalArgumentException("Invalid entity target " + $$0);
         }
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   public static enum ItemStackTarget implements StringRepresentable, LootContextArg.SimpleGetter<ItemStack> {
      TOOL("tool", LootContextParams.TOOL);

      private final String name;
      private final ContextKey<? extends ItemStack> param;

      private ItemStackTarget(final String $$0, final ContextKey<? extends ItemStack> $$1) {
         this.name = $$0;
         this.param = $$1;
      }

      @Override
      public ContextKey<? extends ItemStack> contextParam() {
         return this.param;
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   public record VisitedEntry<T>(LootDataType<T> type, T value) {
   }
}
