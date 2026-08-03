package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;

public class EntityTippedArrowFix extends SimplestEntityRenameFix {
   public EntityTippedArrowFix(Schema $$0, boolean $$1) {
      super("EntityTippedArrowFix", $$0, $$1);
   }

   @Override
   protected String rename(String $$0) {
      return Objects.equals($$0, "TippedArrow") ? "Arrow" : $$0;
   }
}
