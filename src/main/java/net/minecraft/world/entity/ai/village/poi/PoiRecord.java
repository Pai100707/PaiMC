package net.minecraft.world.entity.ai.village.poi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.VisibleForDebug;

public class PoiRecord {
   private final BlockPos pos;
   private final Holder<PoiType> poiType;
   private int freeTickets;
   private final Runnable setDirty;

   PoiRecord(BlockPos $$0, Holder<PoiType> $$1, int $$2, Runnable $$3) {
      this.pos = $$0.immutable();
      this.poiType = $$1;
      this.freeTickets = $$2;
      this.setDirty = $$3;
   }

   public PoiRecord(BlockPos $$0, Holder<PoiType> $$1, Runnable $$2) {
      this($$0, $$1, ((PoiType)$$1.value()).maxTickets(), $$2);
   }

   public PoiRecord.Packed pack() {
      return new PoiRecord.Packed(this.pos, this.poiType, this.freeTickets);
   }

   @Deprecated
   @VisibleForDebug
   public int getFreeTickets() {
      return this.freeTickets;
   }

   protected boolean acquireTicket() {
      if (this.freeTickets <= 0) {
         return false;
      } else {
         this.freeTickets--;
         this.setDirty.run();
         return true;
      }
   }

   protected boolean releaseTicket() {
      if (this.freeTickets >= ((PoiType)this.poiType.value()).maxTickets()) {
         return false;
      } else {
         this.freeTickets++;
         this.setDirty.run();
         return true;
      }
   }

   public boolean hasSpace() {
      return this.freeTickets > 0;
   }

   public boolean isOccupied() {
      return this.freeTickets != ((PoiType)this.poiType.value()).maxTickets();
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Holder<PoiType> getPoiType() {
      return this.poiType;
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return $$0 != null && this.getClass() == $$0.getClass() ? Objects.equals(this.pos, ((PoiRecord)$$0).pos) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.pos.hashCode();
   }

   public record Packed(BlockPos pos, Holder<PoiType> poiType, int freeTickets) {
      public static final Codec<PoiRecord.Packed> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               BlockPos.CODEC.fieldOf("pos").forGetter(PoiRecord.Packed::pos),
               RegistryFixedCodec.create(Registries.POINT_OF_INTEREST_TYPE).fieldOf("type").forGetter(PoiRecord.Packed::poiType),
               Codec.INT.fieldOf("free_tickets").orElse(0).forGetter(PoiRecord.Packed::freeTickets)
            )
            .apply($$0, PoiRecord.Packed::new)
      );

      public PoiRecord unpack(Runnable $$0) {
         return new PoiRecord(this.pos, this.poiType, this.freeTickets, $$0);
      }
   }
}
