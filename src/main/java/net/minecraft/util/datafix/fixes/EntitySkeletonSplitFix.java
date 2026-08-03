package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntitySkeletonSplitFix extends SimpleEntityRenameFix {
   public EntitySkeletonSplitFix(Schema $$0, boolean $$1) {
      super("EntitySkeletonSplitFix", $$0, $$1);
   }

   @Override
   protected Pair<String, Dynamic<?>> getNewNameAndTag(String $$0, Dynamic<?> $$1) {
      if (Objects.equals($$0, "Skeleton")) {
         int $$2 = $$1.get("SkeletonType").asInt(0);
         if ($$2 == 1) {
            $$0 = "WitherSkeleton";
         } else if ($$2 == 2) {
            $$0 = "Stray";
         }
      }

      return Pair.of($$0, $$1);
   }
}
