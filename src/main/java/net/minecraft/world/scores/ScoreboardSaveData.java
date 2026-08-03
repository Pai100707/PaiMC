package net.minecraft.world.scores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class ScoreboardSaveData extends SavedData {
   public static final SavedDataType<net.minecraft.world.scores.ScoreboardSaveData> TYPE = new SavedDataType(
      "scoreboard",
      net.minecraft.world.scores.ScoreboardSaveData::new,
      net.minecraft.world.scores.ScoreboardSaveData.Packed.CODEC
         .xmap(net.minecraft.world.scores.ScoreboardSaveData::new, net.minecraft.world.scores.ScoreboardSaveData::getData),
      DataFixTypes.SAVED_DATA_SCOREBOARD
   );
   private net.minecraft.world.scores.ScoreboardSaveData.Packed data;

   private ScoreboardSaveData() {
      this(net.minecraft.world.scores.ScoreboardSaveData.Packed.EMPTY);
   }

   public ScoreboardSaveData(net.minecraft.world.scores.ScoreboardSaveData.Packed $$0) {
      this.data = $$0;
   }

   public net.minecraft.world.scores.ScoreboardSaveData.Packed getData() {
      return this.data;
   }

   public void setData(net.minecraft.world.scores.ScoreboardSaveData.Packed $$0) {
      if (!$$0.equals(this.data)) {
         this.data = $$0;
         this.setDirty();
      }
   }

   public record Packed(
      List<net.minecraft.world.scores.Objective.Packed> objectives,
      List<net.minecraft.world.scores.Scoreboard.PackedScore> scores,
      Map<net.minecraft.world.scores.DisplaySlot, String> displaySlots,
      List<net.minecraft.world.scores.PlayerTeam.Packed> teams
   ) {
      public static final net.minecraft.world.scores.ScoreboardSaveData.Packed EMPTY = new net.minecraft.world.scores.ScoreboardSaveData.Packed(
         List.of(), List.of(), Map.of(), List.of()
      );
      public static final Codec<net.minecraft.world.scores.ScoreboardSaveData.Packed> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               net.minecraft.world.scores.Objective.Packed.CODEC
                  .listOf()
                  .optionalFieldOf("Objectives", List.of())
                  .forGetter(net.minecraft.world.scores.ScoreboardSaveData.Packed::objectives),
               net.minecraft.world.scores.Scoreboard.PackedScore.CODEC
                  .listOf()
                  .optionalFieldOf("PlayerScores", List.of())
                  .forGetter(net.minecraft.world.scores.ScoreboardSaveData.Packed::scores),
               Codec.unboundedMap(net.minecraft.world.scores.DisplaySlot.CODEC, Codec.STRING)
                  .optionalFieldOf("DisplaySlots", Map.of())
                  .forGetter(net.minecraft.world.scores.ScoreboardSaveData.Packed::displaySlots),
               net.minecraft.world.scores.PlayerTeam.Packed.CODEC
                  .listOf()
                  .optionalFieldOf("Teams", List.of())
                  .forGetter(net.minecraft.world.scores.ScoreboardSaveData.Packed::teams)
            )
            .apply($$0, net.minecraft.world.scores.ScoreboardSaveData.Packed::new)
      );
   }
}
