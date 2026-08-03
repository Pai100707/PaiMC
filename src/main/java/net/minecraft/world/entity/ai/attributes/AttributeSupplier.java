package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class AttributeSupplier {
   private final Map<Holder<Attribute>, AttributeInstance> instances;

   AttributeSupplier(Map<Holder<Attribute>, AttributeInstance> $$0) {
      this.instances = $$0;
   }

   private AttributeInstance getAttributeInstance(Holder<Attribute> $$0) {
      AttributeInstance $$1 = this.instances.get($$0);
      if ($$1 == null) {
         throw new IllegalArgumentException("Can't find attribute " + $$0.getRegisteredName());
      } else {
         return $$1;
      }
   }

   public double getValue(Holder<Attribute> $$0) {
      return this.getAttributeInstance($$0).getValue();
   }

   public double getBaseValue(Holder<Attribute> $$0) {
      return this.getAttributeInstance($$0).getBaseValue();
   }

   public double getModifierValue(Holder<Attribute> $$0, Identifier $$1) {
      AttributeModifier $$2 = this.getAttributeInstance($$0).getModifier($$1);
      if ($$2 == null) {
         throw new IllegalArgumentException("Can't find modifier " + $$1 + " on attribute " + $$0.getRegisteredName());
      } else {
         return $$2.amount();
      }
   }

   @Nullable
   public AttributeInstance createInstance(Consumer<AttributeInstance> $$0, Holder<Attribute> $$1) {
      AttributeInstance $$2 = this.instances.get($$1);
      if ($$2 == null) {
         return null;
      } else {
         AttributeInstance $$3 = new AttributeInstance($$1, $$0);
         $$3.replaceFrom($$2);
         return $$3;
      }
   }

   public static AttributeSupplier.Builder builder() {
      return new AttributeSupplier.Builder();
   }

   public boolean hasAttribute(Holder<Attribute> $$0) {
      return this.instances.containsKey($$0);
   }

   public boolean hasModifier(Holder<Attribute> $$0, Identifier $$1) {
      AttributeInstance $$2 = this.instances.get($$0);
      return $$2 != null && $$2.getModifier($$1) != null;
   }

   public static class Builder {
      private final com.google.common.collect.ImmutableMap.Builder<Holder<Attribute>, AttributeInstance> builder = ImmutableMap.builder();
      private boolean instanceFrozen;

      private AttributeInstance create(Holder<Attribute> $$0) {
         AttributeInstance $$1 = new AttributeInstance($$0, $$1x -> {
            if (this.instanceFrozen) {
               throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + $$0.getRegisteredName());
            }
         });
         this.builder.put($$0, $$1);
         return $$1;
      }

      public AttributeSupplier.Builder add(Holder<Attribute> $$0) {
         this.create($$0);
         return this;
      }

      public AttributeSupplier.Builder add(Holder<Attribute> $$0, double $$1) {
         AttributeInstance $$2 = this.create($$0);
         $$2.setBaseValue($$1);
         return this;
      }

      public AttributeSupplier build() {
         this.instanceFrozen = true;
         return new AttributeSupplier(this.builder.buildKeepingLast());
      }
   }
}
