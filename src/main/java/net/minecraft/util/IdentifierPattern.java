package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;

public class IdentifierPattern {
   public static final Codec<net.minecraft.util.IdentifierPattern> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.util.ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter($$0x -> $$0x.namespacePattern),
            net.minecraft.util.ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter($$0x -> $$0x.pathPattern)
         )
         .apply($$0, net.minecraft.util.IdentifierPattern::new)
   );
   private final Optional<Pattern> namespacePattern;
   private final Predicate<String> namespacePredicate;
   private final Optional<Pattern> pathPattern;
   private final Predicate<String> pathPredicate;
   private final Predicate<Identifier> locationPredicate;

   private IdentifierPattern(Optional<Pattern> $$0, Optional<Pattern> $$1) {
      this.namespacePattern = $$0;
      this.namespacePredicate = $$0.map(Pattern::asPredicate).orElse($$0x -> true);
      this.pathPattern = $$1;
      this.pathPredicate = $$1.map(Pattern::asPredicate).orElse($$0x -> true);
      this.locationPredicate = $$0x -> this.namespacePredicate.test($$0x.getNamespace()) && this.pathPredicate.test($$0x.getPath());
   }

   public Predicate<String> namespacePredicate() {
      return this.namespacePredicate;
   }

   public Predicate<String> pathPredicate() {
      return this.pathPredicate;
   }

   public Predicate<Identifier> locationPredicate() {
      return this.locationPredicate;
   }
}
