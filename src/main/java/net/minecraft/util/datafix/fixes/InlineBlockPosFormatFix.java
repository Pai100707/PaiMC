package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class InlineBlockPosFormatFix extends DataFix {
   public InlineBlockPosFormatFix(Schema $$0) {
      super($$0, false);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<?> $$0 = this.entityFinder("minecraft:vex");
      OpticFinder<?> $$1 = this.entityFinder("minecraft:phantom");
      OpticFinder<?> $$2 = this.entityFinder("minecraft:turtle");
      List<OpticFinder<?>> $$3 = List.of(
         this.entityFinder("minecraft:item_frame"),
         this.entityFinder("minecraft:glow_item_frame"),
         this.entityFinder("minecraft:painting"),
         this.entityFinder("minecraft:leash_knot")
      );
      return TypeRewriteRule.seq(
         this.fixTypeEverywhereTyped(
            "InlineBlockPosFormatFix - player", this.getInputSchema().getType(References.PLAYER), $$0x -> $$0x.update(DSL.remainderFinder(), this::fixPlayer)
         ),
         this.fixTypeEverywhereTyped(
            "InlineBlockPosFormatFix - entity",
            this.getInputSchema().getType(References.ENTITY),
            $$4 -> {
               $$4 = $$4.update(DSL.remainderFinder(), this::fixLivingEntity)
                  .updateTyped($$0, $$0xx -> $$0xx.update(DSL.remainderFinder(), this::fixVex))
                  .updateTyped($$1, $$0xx -> $$0xx.update(DSL.remainderFinder(), this::fixPhantom))
                  .updateTyped($$2, $$0xx -> $$0xx.update(DSL.remainderFinder(), this::fixTurtle));

               for (OpticFinder<?> $$5 : $$3) {
                  $$4 = $$4.updateTyped($$5, $$0xx -> $$0xx.update(DSL.remainderFinder(), this::fixBlockAttached));
               }

               return $$4;
            }
         )
      );
   }

   private OpticFinder<?> entityFinder(String $$0) {
      return DSL.namedChoice($$0, this.getInputSchema().getChoiceType(References.ENTITY, $$0));
   }

   private Dynamic<?> fixPlayer(Dynamic<?> $$0) {
      $$0 = this.fixLivingEntity($$0);
      Optional<Number> $$1 = $$0.get("SpawnX").asNumber().result();
      Optional<Number> $$2 = $$0.get("SpawnY").asNumber().result();
      Optional<Number> $$3 = $$0.get("SpawnZ").asNumber().result();
      if ($$1.isPresent() && $$2.isPresent() && $$3.isPresent()) {
         Dynamic<?> $$4 = $$0.createMap(
            Map.of($$0.createString("pos"), ExtraDataFixUtils.createBlockPos($$0, $$1.get().intValue(), $$2.get().intValue(), $$3.get().intValue()))
         );
         $$4 = Dynamic.copyField($$0, "SpawnAngle", $$4, "angle");
         $$4 = Dynamic.copyField($$0, "SpawnDimension", $$4, "dimension");
         $$4 = Dynamic.copyField($$0, "SpawnForced", $$4, "forced");
         $$0 = $$0.remove("SpawnX").remove("SpawnY").remove("SpawnZ").remove("SpawnAngle").remove("SpawnDimension").remove("SpawnForced");
         $$0 = $$0.set("respawn", $$4);
      }

      Optional<? extends Dynamic<?>> $$5 = $$0.get("enteredNetherPosition").result();
      if ($$5.isPresent()) {
         $$0 = $$0.remove("enteredNetherPosition")
            .set(
               "entered_nether_pos",
               $$0.createList(
                  Stream.of(
                     $$0.createDouble($$5.get().get("x").asDouble(0.0)),
                     $$0.createDouble($$5.get().get("y").asDouble(0.0)),
                     $$0.createDouble($$5.get().get("z").asDouble(0.0))
                  )
               )
            );
      }

      return $$0;
   }

   private Dynamic<?> fixLivingEntity(Dynamic<?> $$0) {
      return ExtraDataFixUtils.fixInlineBlockPos($$0, "SleepingX", "SleepingY", "SleepingZ", "sleeping_pos");
   }

   private Dynamic<?> fixVex(Dynamic<?> $$0) {
      return ExtraDataFixUtils.fixInlineBlockPos($$0.renameField("LifeTicks", "life_ticks"), "BoundX", "BoundY", "BoundZ", "bound_pos");
   }

   private Dynamic<?> fixPhantom(Dynamic<?> $$0) {
      return ExtraDataFixUtils.fixInlineBlockPos($$0.renameField("Size", "size"), "AX", "AY", "AZ", "anchor_pos");
   }

   private Dynamic<?> fixTurtle(Dynamic<?> $$0) {
      $$0 = $$0.remove("TravelPosX").remove("TravelPosY").remove("TravelPosZ");
      $$0 = ExtraDataFixUtils.fixInlineBlockPos($$0, "HomePosX", "HomePosY", "HomePosZ", "home_pos");
      return $$0.renameField("HasEgg", "has_egg");
   }

   private Dynamic<?> fixBlockAttached(Dynamic<?> $$0) {
      return ExtraDataFixUtils.fixInlineBlockPos($$0, "TileX", "TileY", "TileZ", "block_pos");
   }
}
