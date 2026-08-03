package net.minecraft.world.level.block.state.predicate;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public class BlockStatePredicate implements Predicate<BlockState> {
   public static final Predicate<BlockState> ANY = $$0 -> true;
   private final StateDefinition<Block, BlockState> definition;
   private final Map<Property<?>, Predicate<Object>> properties = Maps.newHashMap();

   private BlockStatePredicate(StateDefinition<Block, BlockState> $$0) {
      this.definition = $$0;
   }

   public static BlockStatePredicate forBlock(Block $$0) {
      return new BlockStatePredicate($$0.getStateDefinition());
   }

   public boolean test(@Nullable BlockState $$0) {
      if ($$0 != null && $$0.getBlock().equals(this.definition.getOwner())) {
         if (this.properties.isEmpty()) {
            return true;
         } else {
            for (Entry<Property<?>, Predicate<Object>> $$1 : this.properties.entrySet()) {
               if (!this.applies($$0, $$1.getKey(), $$1.getValue())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   protected <T extends Comparable<T>> boolean applies(BlockState $$0, Property<T> $$1, Predicate<Object> $$2) {
      T $$3 = $$0.getValue($$1);
      return $$2.test($$3);
   }

   public <V extends Comparable<V>> BlockStatePredicate where(Property<V> $$0, Predicate<Object> $$1) {
      if (!this.definition.getProperties().contains($$0)) {
         throw new IllegalArgumentException(this.definition + " cannot support property " + $$0);
      } else {
         this.properties.put($$0, $$1);
         return this;
      }
   }
}
