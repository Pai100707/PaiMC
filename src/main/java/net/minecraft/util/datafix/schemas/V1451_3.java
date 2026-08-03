package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V1451_3 extends NamespacedSchema {
   public V1451_3(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param1) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      //
      // Bytecode:
      // 00: aload 0
      // 01: aload 1
      // 02: invokespecial net/minecraft/util/datafix/schemas/NamespacedSchema.registerEntities (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/Map;
      // 05: astore 2
      // 06: aload 1
      // 07: aload 2
      // 08: ldc "minecraft:egg"
      // 0a: invokevirtual com/mojang/datafixers/schemas/Schema.registerSimple (Ljava/util/Map;Ljava/lang/String;)V
      // 0d: aload 1
      // 0e: aload 2
      // 0f: ldc "minecraft:ender_pearl"
      // 11: invokevirtual com/mojang/datafixers/schemas/Schema.registerSimple (Ljava/util/Map;Ljava/lang/String;)V
      // 14: aload 1
      // 15: aload 2
      // 16: ldc "minecraft:fireball"
      // 18: invokevirtual com/mojang/datafixers/schemas/Schema.registerSimple (Ljava/util/Map;Ljava/lang/String;)V
      // 1b: aload 1
      // 1c: aload 2
      // 1d: ldc "minecraft:potion"
      // 1f: aload 1
      // 20: invokedynamic apply (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Function; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ (Ljava/lang/Object;)Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$0 (Lcom/mojang/datafixers/schemas/Schema;Ljava/lang/String;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, (Ljava/lang/String;)Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // 25: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Function;)V
      // 28: aload 1
      // 29: aload 2
      // 2a: ldc "minecraft:small_fireball"
      // 2c: invokevirtual com/mojang/datafixers/schemas/Schema.registerSimple (Ljava/util/Map;Ljava/lang/String;)V
      // 2f: aload 1
      // 30: aload 2
      // 31: ldc "minecraft:snowball"
      // 33: invokevirtual com/mojang/datafixers/schemas/Schema.registerSimple (Ljava/util/Map;Ljava/lang/String;)V
      // 36: aload 1
      // 37: aload 2
      // 38: ldc "minecraft:wither_skull"
      // 3a: invokevirtual com/mojang/datafixers/schemas/Schema.registerSimple (Ljava/util/Map;Ljava/lang/String;)V
      // 3d: aload 1
      // 3e: aload 2
      // 3f: ldc "minecraft:xp_bottle"
      // 41: invokevirtual com/mojang/datafixers/schemas/Schema.registerSimple (Ljava/util/Map;Ljava/lang/String;)V
      // 44: aload 1
      // 45: aload 2
      // 46: ldc "minecraft:arrow"
      // 48: aload 1
      // 49: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$1 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // 4e: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // 51: aload 1
      // 52: aload 2
      // 53: ldc "minecraft:enderman"
      // 55: aload 1
      // 56: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$2 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // 5b: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // 5e: aload 1
      // 5f: aload 2
      // 60: ldc "minecraft:falling_block"
      // 62: aload 1
      // 63: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$3 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // 68: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // 6b: aload 1
      // 6c: aload 2
      // 6d: ldc "minecraft:spectral_arrow"
      // 6f: aload 1
      // 70: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$4 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // 75: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // 78: aload 1
      // 79: aload 2
      // 7a: ldc "minecraft:chest_minecart"
      // 7c: aload 1
      // 7d: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$5 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // 82: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // 85: aload 1
      // 86: aload 2
      // 87: ldc "minecraft:commandblock_minecart"
      // 89: aload 1
      // 8a: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$6 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // 8f: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // 92: aload 1
      // 93: aload 2
      // 94: ldc "minecraft:furnace_minecart"
      // 96: aload 1
      // 97: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$7 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // 9c: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // 9f: aload 1
      // a0: aload 2
      // a1: ldc "minecraft:hopper_minecart"
      // a3: aload 1
      // a4: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$8 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // a9: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // ac: aload 1
      // ad: aload 2
      // ae: ldc "minecraft:minecart"
      // b0: aload 1
      // b1: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$9 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // b6: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // b9: aload 1
      // ba: aload 2
      // bb: ldc "minecraft:spawner_minecart"
      // bd: aload 1
      // be: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$10 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // c3: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // c6: aload 1
      // c7: aload 2
      // c8: ldc "minecraft:tnt_minecart"
      // ca: aload 1
      // cb: invokedynamic get (Lcom/mojang/datafixers/schemas/Schema;)Ljava/util/function/Supplier; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()Ljava/lang/Object;, net/minecraft/util/datafix/schemas/V1451_3.lambda$registerEntities$11 (Lcom/mojang/datafixers/schemas/Schema;)Lcom/mojang/datafixers/types/templates/TypeTemplate;, ()Lcom/mojang/datafixers/types/templates/TypeTemplate; ]
      // d0: invokevirtual com/mojang/datafixers/schemas/Schema.register (Ljava/util/Map;Ljava/lang/String;Ljava/util/function/Supplier;)V
      // d3: aload 2
      // d4: areturn
   }
}
