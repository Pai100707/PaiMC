package net.minecraft.network.chat.contents;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.ObjectInfos;

public record ObjectContents(ObjectInfo contents) implements ComponentContents {
   private static final String PLACEHOLDER = Character.toString('￼');
   public static final MapCodec<ObjectContents> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ObjectInfos.CODEC.forGetter(ObjectContents::contents)).apply($$0, ObjectContents::new)
   );

   @Override
   public MapCodec<ObjectContents> codec() {
      return MAP_CODEC;
   }

   @Override
   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
      return $$0.accept(this.contents.description());
   }

   @Override
   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
      return $$0.accept($$1.withFont(this.contents.fontDescription()), PLACEHOLDER);
   }
}
