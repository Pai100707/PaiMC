package net.minecraft.world.level.block.state.properties;

import java.util.List;
import java.util.Optional;

public final class BooleanProperty extends Property<Boolean> {
   private static final List<Boolean> VALUES = List.of(true, false);
   private static final int TRUE_INDEX = 0;
   private static final int FALSE_INDEX = 1;

   private BooleanProperty(String $$0) {
      super($$0, Boolean.class);
   }

   @Override
   public List<Boolean> getPossibleValues() {
      return VALUES;
   }

   public static BooleanProperty create(String $$0) {
      return new BooleanProperty($$0);
   }

   @Override
   public Optional<Boolean> getValue(String $$0) {
      return switch ($$0) {
         case "true" -> Optional.of(true);
         case "false" -> Optional.of(false);
         default -> Optional.empty();
      };
   }

   public String getName(Boolean $$0) {
      return $$0.toString();
   }

   public int getInternalIndex(Boolean $$0) {
      return $$0 ? 0 : 1;
   }
}
